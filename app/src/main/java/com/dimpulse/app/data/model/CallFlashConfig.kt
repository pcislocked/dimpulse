package com.dimpulse.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class CallFlashLoopMode(val title: String, val description: String) {
    CONTINUOUS_LOOP("Continuous Cadence", "Loops flash sequence continuously while phone is ringing"),
    ONE_TIME("One-Time Burst", "Flashes once when call first rings")
}

@Serializable
data class CallFlashConfig(
    val isEnabled: Boolean = true,
    val loopMode: CallFlashLoopMode = CallFlashLoopMode.CONTINUOUS_LOOP,
    val profilePreset: LightProfilePreset = LightProfilePreset.BREATHING,
    val repeatCount: Int = 2,
    val strengthLevel: Int = 1,
    val fadeInMs: Long = 100L,
    val stayOnMs: Long = 40L,
    val fadeOutMs: Long = 140L,
    val gapMs: Long = 100L,
    val sequenceIntervalMs: Long = 1500L, // Delay between sequences e.g. .x.x.x ... .x.x.x
    val bypassDnd: Boolean = true
)
