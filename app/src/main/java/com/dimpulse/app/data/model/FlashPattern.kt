package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FlashPattern(
    val preset: LightProfilePreset = LightProfilePreset.BREATHING,
    val repeatCount: Int = 1,
    val fadeInMs: Long = 200L,
    val stayOnMs: Long = 30L,
    val fadeOutMs: Long = 200L,
    val gapMs: Long = 120L
) {
    companion object {
        fun fromPreset(
            preset: LightProfilePreset,
            repeatCount: Int = 1,
            customFadeInMs: Long? = null,
            customStayOnMs: Long? = null,
            customFadeOutMs: Long? = null,
            customGapMs: Long? = null
        ): FlashPattern {
            return FlashPattern(
                preset = preset,
                repeatCount = repeatCount.coerceIn(1, 4),
                fadeInMs = customFadeInMs ?: preset.defaultFadeInMs,
                stayOnMs = customStayOnMs ?: preset.defaultStayOnMs,
                fadeOutMs = customFadeOutMs ?: preset.defaultFadeOutMs,
                gapMs = customGapMs ?: preset.defaultGapMs
            )
        }
    }
}
