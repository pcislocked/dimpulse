package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FlashPattern(
    val style: FlashStyle = FlashStyle.BREATHING,
    val repeatCount: Int = 1,
    val onDurationMs: Long = 100L,
    val offDurationMs: Long = 120L,
    val breathingDurationMs: Long = 350L,
    val breathingSteps: Int = 16
) {
    companion object {
        fun defaultFor(
            style: FlashStyle,
            repeatCount: Int = 1,
            breathingDurationMs: Long = 350L,
            onDurationMs: Long = 100L,
            offDurationMs: Long = 120L
        ): FlashPattern {
            return FlashPattern(
                style = style,
                repeatCount = repeatCount.coerceIn(1, 4),
                onDurationMs = onDurationMs,
                offDurationMs = offDurationMs,
                breathingDurationMs = breathingDurationMs,
                breathingSteps = 16
            )
        }
    }
}
