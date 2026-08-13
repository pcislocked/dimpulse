package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GlobalFlashSettings(
    val masterEnabled: Boolean = true,
    val defaultPattern: PatternType = PatternType.DOUBLE_PULSE,
    val defaultStrength: Int = 1,
    val onlyWhenScreenOff: Boolean = true,
    val proximitySensorEnabled: Boolean = true,
    val respectDnd: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartMinutes: Int = 22 * 60, // 22:00 (1320 mins)
    val quietHoursEndMinutes: Int = 7 * 60     // 07:00 (420 mins)
)
