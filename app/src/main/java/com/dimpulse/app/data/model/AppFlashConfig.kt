package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppFlashConfig(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val patternType: PatternType = PatternType.BREATHING,
    val strengthLevel: Int = 1,
    val repeatCount: Int = 1,
    val repeatIntervalSeconds: Int = 0,
    val cooldownSeconds: Int? = null, // null = inherit global
    val bypassDnd: Boolean = false,
    val triggerOrientation: TriggerOrientation? = null,
    val alertImportanceFilter: AlertImportanceFilter? = null,
    val customOnDurationMs: Long = 100L,
    val customOffDurationMs: Long = 120L
)
