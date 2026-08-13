package com.dimpulse.app.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dimpulse.app.DimPulseApp
import com.dimpulse.app.data.model.AlertImportanceFilter
import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.data.model.FlashStyle
import com.dimpulse.app.data.model.GlobalFlashSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

class DimFlashNotificationListener : NotificationListenerService() {

    private val tag = "DimPulse_NLS"
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var connectedTimestampMs = 0L
    private val lastTriggerTimePerPackage = ConcurrentHashMap<String, Long>()

    override fun onListenerConnected() {
        super.onListenerConnected()
        connectedTimestampMs = System.currentTimeMillis()
        Log.i(tag, "DimFlashNotificationListener connected successfully at $connectedTimestampMs")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val now = System.currentTimeMillis()
        // Drop any stale backlog notifications from before the listener connected
        if (sbn.postTime < connectedTimestampMs - 1000L) {
            Log.d(tag, "Dropping stale notification from ${sbn.packageName}")
            return
        }

        val app = application as? DimPulseApp ?: return
        val globalSettings = app.repository.globalSettings.value

        // 1. Master enable switch
        if (!globalSettings.masterEnabled) {
            return
        }

        // 2. Ongoing / foreground service / group summary filter
        val notification = sbn.notification ?: return
        if (sbn.isOngoing || (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) {
            return
        }
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            return // Skip summary header to avoid double flash
        }

        // 3. Screen state filter
        if (globalSettings.onlyWhenScreenOff) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager?.isInteractive == true) {
                return
            }
        }

        val packageName = sbn.packageName ?: return

        // 4. Per-app configuration check
        val appConfig = app.repository.getConfigForPackage(packageName)
        if (appConfig != null && !appConfig.isEnabled) {
            return
        }

        // 5. Silent vs Alerting / Loud Importance Filter
        val alertFilter = appConfig?.alertImportanceFilter ?: globalSettings.alertImportanceFilter
        if (alertFilter == AlertImportanceFilter.NONE) {
            return
        }

        if (alertFilter == AlertImportanceFilter.ONLY_ALERTING) {
            val isAlerting = isNotificationAlerting(sbn)
            if (!isAlerting) {
                Log.d(tag, "Dropping silent/background notification from $packageName (Channel importance is silent)")
                return
            }
        }

        // 6. Do Not Disturb (DND) filter
        if (globalSettings.respectDnd) {
            val bypassDnd = appConfig?.bypassDnd ?: false
            if (!bypassDnd && isDndActive()) {
                return
            }
        }

        // 7. Quiet Hours filter
        if (globalSettings.quietHoursEnabled && isInQuietHours(globalSettings)) {
            return
        }

        // 8. User-Configurable Burst Rate Limit / Cooldown Debounce
        val cooldownSec = appConfig?.cooldownSeconds ?: globalSettings.cooldownSeconds
        if (cooldownSec > 0) {
            val lastTime = lastTriggerTimePerPackage[packageName] ?: 0L
            if (now - lastTime < cooldownSec * 1000L) {
                Log.d(tag, "Debouncing rapid notification burst for $packageName (Rate limit: ${cooldownSec}s)")
                return
            }
            lastTriggerTimePerPackage[packageName] = now
        }

        // 9. Orientation & Table / Pocket Gating & Pulse Dispatch
        serviceScope.launch {
            val orientationMode = appConfig?.triggerOrientation ?: globalSettings.triggerOrientation
            val allowed = app.proximityHelper.shouldAllowFlash(orientationMode)

            if (!allowed) {
                Log.d(tag, "Flash suppressed by orientation/pocket filter for mode $orientationMode")
                return@launch
            }

            val breathingDuration = globalSettings.breathingDurationMs
            val onDuration = appConfig?.customOnDurationMs ?: 100L
            val offDuration = appConfig?.customOffDurationMs ?: 120L
            val flashPattern = FlashPattern(
                style = flashStyle,
                repeatCount = repeatCount,
                breathingDurationMs = breathingDuration,
                onDurationMs = onDuration,
                offDurationMs = offDuration
            )

            Log.i(tag, "Dispatching ambient LED pulse for $packageName: $flashStyle x$repeatCount at level $strengthLevel (${breathingDuration}ms)")
            app.pulseEngine.triggerPattern(flashPattern, strengthLevel)

            // Optional repeat nag for missed alerts if configured
            if (repeatIntervalSec > 0) {
                launch {
                    delay(repeatIntervalSec * 1000L)
                    val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                    if (powerManager?.isInteractive == false) {
                        app.pulseEngine.triggerPattern(flashPattern, strengthLevel)
                    }
                }
            }
        }
    }

    private fun isNotificationAlerting(sbn: StatusBarNotification): Boolean {
        return try {
            val ranking = Ranking()
            val rankingMap = currentRanking
            if (rankingMap != null && rankingMap.getRanking(sbn.key, ranking)) {
                val importance = ranking.importance
                val channel = ranking.channel
                val channelImportance = channel?.importance ?: NotificationManager.IMPORTANCE_DEFAULT
                importance >= NotificationManager.IMPORTANCE_DEFAULT && channelImportance >= NotificationManager.IMPORTANCE_DEFAULT
            } else {
                val channelId = sbn.notification.channelId
                val hasSoundOrVibrate = sbn.notification.sound != null ||
                        sbn.notification.vibrate != null ||
                        (sbn.notification.defaults and (Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)) != 0
                hasSoundOrVibrate || channelId != null
            }
        } catch (e: Exception) {
            Log.w(tag, "Error checking notification importance: ${e.message}")
            true
        }
    }

    private fun isDndActive(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        val filter = notificationManager.currentInterruptionFilter
        return filter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                filter == NotificationManager.INTERRUPTION_FILTER_NONE ||
                filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY
    }

    private fun isInQuietHours(settings: GlobalFlashSettings): Boolean {
        val calendar = Calendar.getInstance()
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        val start = settings.quietHoursStartMinutes
        val end = settings.quietHoursEndMinutes

        return if (start < end) {
            currentMinutes in start until end
        } else {
            currentMinutes >= start || currentMinutes < end
        }
    }
}
