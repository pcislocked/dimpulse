package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppFlashConfig(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val patternType: PatternType = PatternType.DOUBLE_PULSE,
    val strengthLevel: Int = 1,
    val repeatCount: Int = 1,
    val bypassDnd: Boolean = false,
    val customOnDurationMs: Long = 100L,
    val customOffDurationMs: Long = 120L
)
