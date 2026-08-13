package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class LightProfilePreset(
    val title: String,
    val description: String,
    val defaultFadeInMs: Long,
    val defaultStayOnMs: Long,
    val defaultFadeOutMs: Long,
    val defaultGapMs: Long
) {
    BREATHING(
        title = "Breathing Glow",
        description = "Smooth ambient rise and gentle fall",
        defaultFadeInMs = 200L,
        defaultStayOnMs = 30L,
        defaultFadeOutMs = 200L,
        defaultGapMs = 120L
    ),
    CRISP_PULSE(
        title = "Crisp Pulse",
        description = "Instant sharp digital click",
        defaultFadeInMs = 0L,
        defaultStayOnMs = 100L,
        defaultFadeOutMs = 0L,
        defaultGapMs = 120L
    ),
    FADE_OUT(
        title = "Soft Fade-Out",
        description = "Instant attack with smooth analog decay",
        defaultFadeInMs = 25L,
        defaultStayOnMs = 35L,
        defaultFadeOutMs = 280L,
        defaultGapMs = 120L
    ),
    FADE_IN(
        title = "Snappy Rise",
        description = "Gradual swelling rise with sharp cutoff",
        defaultFadeInMs = 280L,
        defaultStayOnMs = 35L,
        defaultFadeOutMs = 0L,
        defaultGapMs = 120L
    ),
    CUSTOM(
        title = "Custom Shape",
        description = "User-defined millisecond timings",
        defaultFadeInMs = 150L,
        defaultStayOnMs = 80L,
        defaultFadeOutMs = 150L,
        defaultGapMs = 100L
    )
}
