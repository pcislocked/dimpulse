package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppFlashConfig(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val profilePreset: LightProfilePreset? = null,
    val repeatCount: Int? = null,
    val strengthLevel: Int? = null,
    val fadeInMs: Long? = null,
    val stayOnMs: Long? = null,
    val fadeOutMs: Long? = null,
    val gapMs: Long? = null,
    val repeatIntervalSeconds: Int = 0,
    val cooldownSeconds: Int? = null,
    val bypassDnd: Boolean = false,
    val triggerOrientation: TriggerOrientation? = null,
    val alertImportanceFilter: AlertImportanceFilter? = null
)
