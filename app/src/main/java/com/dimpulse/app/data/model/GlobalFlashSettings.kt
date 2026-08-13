package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GlobalFlashSettings(
    val masterEnabled: Boolean = true,
    val defaultProfilePreset: LightProfilePreset = LightProfilePreset.BREATHING,
    val defaultRepeatCount: Int = 1,
    val defaultStrength: Int = 1,
    val defaultFadeInMs: Long = 200L,
    val defaultStayOnMs: Long = 30L,
    val defaultFadeOutMs: Long = 200L,
    val defaultGapMs: Long = 120L,
    val onlyWhenScreenOff: Boolean = true,
    val triggerOrientation: TriggerOrientation = TriggerOrientation.EXCEPT_IN_POCKET,
    val alertImportanceFilter: AlertImportanceFilter = AlertImportanceFilter.ONLY_ALERTING,
    val respectDnd: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartMinutes: Int = 22 * 60, // 22:00
    val quietHoursEndMinutes: Int = 7 * 60,    // 07:00
    val repeatIntervalSeconds: Int = 0,
    val cooldownSeconds: Int = 3
)
