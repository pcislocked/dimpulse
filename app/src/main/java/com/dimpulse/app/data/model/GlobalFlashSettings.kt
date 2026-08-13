package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GlobalFlashSettings(
    val masterEnabled: Boolean = true,
    val defaultPattern: PatternType = PatternType.BREATHING,
    val defaultStrength: Int = 1,
    val onlyWhenScreenOff: Boolean = true,
    val triggerOrientation: TriggerOrientation = TriggerOrientation.EXCEPT_IN_POCKET,
    val respectDnd: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartMinutes: Int = 22 * 60, // 22:00
    val quietHoursEndMinutes: Int = 7 * 60,    // 07:00
    val breathingDurationMs: Long = 400L,
    val repeatIntervalSeconds: Int = 0 // 0 = flash once, 10 = repeat every 10s
)
