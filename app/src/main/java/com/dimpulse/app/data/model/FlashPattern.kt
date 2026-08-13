package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FlashPattern(
    val type: PatternType = PatternType.DOUBLE_PULSE,
    val onDurationMs: Long = 100L,
    val offDurationMs: Long = 120L,
    val repeatCount: Int = 1,
    val breathingSteps: Int = 16,
    val breathingStepDelayMs: Long = 25L
) {
    companion object {
        fun defaultFor(type: PatternType): FlashPattern {
            return when (type) {
                PatternType.SINGLE_PULSE -> FlashPattern(
                    type = type,
                    onDurationMs = 120L,
                    offDurationMs = 0L,
                    repeatCount = 1
                )
                PatternType.DOUBLE_PULSE -> FlashPattern(
                    type = type,
                    onDurationMs = 100L,
                    offDurationMs = 120L,
                    repeatCount = 2
                )
                PatternType.TRIPLE_PULSE -> FlashPattern(
                    type = type,
                    onDurationMs = 90L,
                    offDurationMs = 100L,
                    repeatCount = 3
                )
                PatternType.BREATHING -> FlashPattern(
                    type = type,
                    breathingSteps = 16,
                    breathingStepDelayMs = 25L,
                    repeatCount = 1
                )
                PatternType.RAPID_STROBE -> FlashPattern(
                    type = type,
                    onDurationMs = 40L,
                    offDurationMs = 50L,
                    repeatCount = 4
                )
            }
        }
    }
}
