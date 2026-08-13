package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class FlashStyle(
    val title: String,
    val description: String
) {
    BREATHING(
        title = "Breathing Glow",
        description = "Smooth sinusoidal ramp up and gentle fall"
    ),
    CRISP_PULSE(
        title = "Crisp Pulse",
        description = "Instant solid digital flash"
    ),
    FADE_OUT(
        title = "Fade-Out Decay",
        description = "Fast attack with gradual smooth decay"
    ),
    FADE_IN(
        title = "Fade-In Rise",
        description = "Gentle gradual rise with instant cutoff"
    )
}
