package com.dimpulse.app.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dimpulse.app.DimPulseApp
import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.data.model.GlobalFlashSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

class DimFlashNotificationListener : NotificationListenerService() {

    private val tag = "DimPulse_NLS"
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var serviceConnectedTime = 0L
    private val lastTriggerTimestamp = ConcurrentHashMap<String, Long>()

    // Noisy system internal packages that should not trigger flash unless explicitly configured
    private val defaultIgnoredPackages = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.gms",
        "com.google.android.googlequicksearchbox",
        "com.google.android.as",
        "com.android.providers.downloads"
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        serviceConnectedTime = System.currentTimeMillis()
        Log.i(tag, "DimFlashNotificationListener connected. Stale notifications before $serviceConnectedTime will be ignored.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val now = System.currentTimeMillis()

        // 1. Drop Stale Notifications from initial batch / before service connection
        if (serviceConnectedTime == 0L || sbn.postTime < (serviceConnectedTime - 1000L)) {
            return
        }

        // 2. Drop old notifications (> 3 seconds old)
        if (now - sbn.postTime > 3000L) {
            return
        }

        val app = application as? DimPulseApp ?: return
        val globalSettings = app.repository.globalSettings.value

        // 3. Master enable switch
        if (!globalSettings.masterEnabled) {
            return
        }

        val packageName = sbn.packageName ?: return

        // 4. Ongoing / foreground service / group summary filter
        val notification = sbn.notification ?: return
        if (sbn.isOngoing || (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) {
            return
        }
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            return
        }

        // 5. Default ignored background system daemons (unless user explicitly added a custom rule)
        val appConfig = app.repository.getConfigForPackage(packageName)
        if (appConfig == null && defaultIgnoredPackages.contains(packageName)) {
            return
        }

        // 6. Per-app enabled switch
        if (appConfig != null && !appConfig.isEnabled) {
            return
        }

        // 7. Screen state filter (Only flash when screen is OFF)
        if (globalSettings.onlyWhenScreenOff) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager?.isInteractive == true) {
                return
            }
        }

        // 8. Debounce / Cooldown filter (prevent rapid re-triggering within 3.5 seconds for same app)
        val lastTrigger = lastTriggerTimestamp[packageName] ?: 0L
        if (now - lastTrigger < 3500L) {
            Log.d(tag, "Debounced notification flash for $packageName")
            return
        }

        // 9. Do Not Disturb (DND) filter
        if (globalSettings.respectDnd) {
            val bypassDnd = appConfig?.bypassDnd ?: false
            if (!bypassDnd && isDndActive()) {
                return
            }
        }

        // 10. Quiet Hours filter
        if (globalSettings.quietHoursEnabled && isInQuietHours(globalSettings)) {
            return
        }

        // Mark trigger time
        lastTriggerTimestamp[packageName] = now

        // 11. Orientation & Table / Pocket Gating & Pulse Dispatch
        serviceScope.launch {
            val orientationMode = appConfig?.triggerOrientation ?: globalSettings.triggerOrientation
            val allowed = app.proximityHelper.shouldAllowFlash(orientationMode)

            if (!allowed) {
                Log.d(tag, "Flash suppressed by orientation/pocket filter for mode $orientationMode")
                return@launch
            }

            // Determine pattern and intensity
            val patternType = appConfig?.patternType ?: globalSettings.defaultPattern
            val strengthLevel = appConfig?.strengthLevel ?: globalSettings.defaultStrength
            val repeatCount = appConfig?.repeatCount ?: 1
            val flashPattern = FlashPattern.defaultFor(patternType).copy(repeatCount = repeatCount)

            Log.i(tag, "Dispatching ambient LED pulse for $packageName: $patternType at level $strengthLevel")
            app.pulseEngine.triggerPattern(flashPattern, strengthLevel)
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
