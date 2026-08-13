package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class PatternType(
    val title: String,
    val description: String,
    val recommendedCount: Int = 1
) {
    SINGLE_PULSE(
        title = "Single Pulse",
        description = "One brief ambient flash (120ms)"
    ),
    DOUBLE_PULSE(
        title = "Double Pulse",
        description = "Two distinct gentle blinks"
    ),
    TRIPLE_PULSE(
        title = "Triple Pulse",
        description = "Three rhythmic pulses for medium priority"
    ),
    BREATHING(
        title = "Breathing Glow",
        description = "Organic sinusoidal rise and fall ramp (~400ms)"
    ),
    RAPID_STROBE(
        title = "Urgency Strobe",
        description = "Four rapid micro-bursts for high priority alerts"
    )
}
