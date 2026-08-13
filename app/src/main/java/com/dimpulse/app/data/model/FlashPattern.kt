package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FlashPattern(
    val style: FlashStyle = FlashStyle.BREATHING,
    val repeatCount: Int = 1,
    val onDurationMs: Long = 100L,
    val offDurationMs: Long = 120L,
    val breathingSteps: Int = 16,
    val breathingStepDelayMs: Long = 25L
) {
    companion object {
        fun defaultFor(style: FlashStyle, repeatCount: Int = 1): FlashPattern {
            return FlashPattern(
                style = style,
                repeatCount = repeatCount.coerceIn(1, 4),
                onDurationMs = 100L,
                offDurationMs = 120L,
                breathingSteps = 16,
                breathingStepDelayMs = 25L
            )
        }
    }
}
