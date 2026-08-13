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

class DimFlashNotificationListener : NotificationListenerService() {

    private val tag = "DimPulse_NLS"
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(tag, "DimFlashNotificationListener connected successfully")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

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

        // 3. Screen state filter (Suppress if user is actively using device)
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

        // 5. Do Not Disturb (DND) filter
        if (globalSettings.respectDnd) {
            val bypassDnd = appConfig?.bypassDnd ?: false
            if (!bypassDnd && isDndActive()) {
                return
            }
        }

        // 6. Quiet Hours filter
        if (globalSettings.quietHoursEnabled && isInQuietHours(globalSettings)) {
            return
        }

        // 7. Sensor Gating (Pocket & Face-Down) & Pulse Dispatch
        serviceScope.launch {
            if (globalSettings.proximitySensorEnabled) {
                val isCovered = app.proximityHelper.isCoveredOrFaceDown(150L)
                if (isCovered) {
                    Log.d(tag, "Notification flash suppressed: proximity sensor occluded (pocket/face-down)")
                    return@launch
                }
            }

            // Determine pattern and intensity
            val patternType = appConfig?.patternType ?: globalSettings.defaultPattern
            val strengthLevel = appConfig?.strengthLevel ?: globalSettings.defaultStrength
            val flashPattern = FlashPattern.defaultFor(patternType)

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
            // Spans overnight (e.g. 22:00 to 07:00)
            currentMinutes >= start || currentMinutes < end
        }
    }
}
