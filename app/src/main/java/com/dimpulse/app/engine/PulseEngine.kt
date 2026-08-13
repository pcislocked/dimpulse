package com.dimpulse.app.engine

import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.data.model.FlashStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

class PulseEngine(
    private val flashController: FlashController,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private var activeJob: Job? = null
    private val mutex = Mutex()

    fun triggerPattern(
        pattern: FlashPattern,
        strengthLevel: Int,
        onComplete: (() -> Unit)? = null
    ) {
        scope.launch {
            mutex.withLock {
                activeJob?.cancelAndJoin()
                activeJob = launch {
                    try {
                        withTimeout(6000L) { // Safety watchdog: 6-second hard cutoff
                            val repeats = pattern.repeatCount.coerceIn(1, 4)
                            for (r in 0 until repeats) {
                                when (pattern.style) {
                                    FlashStyle.BREATHING -> {
                                        runBreathing(
                                            maxLevel = strengthLevel,
                                            steps = pattern.breathingSteps,
                                            stepDelayMs = pattern.breathingStepDelayMs
                                        )
                                    }
                                    FlashStyle.CRISP_PULSE -> {
                                        runCrisp(
                                            strengthLevel = strengthLevel,
                                            onDurationMs = pattern.onDurationMs
                                        )
                                    }
                                    FlashStyle.FADE_OUT -> {
                                        runFadeOut(
                                            maxLevel = strengthLevel,
                                            steps = pattern.breathingSteps
                                        )
                                    }
                                    FlashStyle.FADE_IN -> {
                                        runFadeIn(
                                            maxLevel = strengthLevel,
                                            steps = pattern.breathingSteps
                                        )
                                    }
                                }

                                if (r < repeats - 1) {
                                    flashController.turnOff()
                                    delay(pattern.offDurationMs)
                                }
                            }
                        }
                    } finally {
                        flashController.turnOff()
                        onComplete?.invoke()
                    }
                }
            }
        }
    }

    private suspend fun runCrisp(
        strengthLevel: Int,
        onDurationMs: Long = 100L
    ) {
        flashController.setFlashStrength(strengthLevel)
        delay(onDurationMs)
        flashController.turnOff()
    }

    private suspend fun runBreathing(
        maxLevel: Int,
        steps: Int = 16,
        stepDelayMs: Long = 25L
    ) {
        if (maxLevel <= 1) {
            flashController.setFlashStrength(1)
            delay(140L)
            flashController.turnOff()
            return
        }

        // Half sinusoidal ramp up and down
        for (i in 0..steps) {
            val progress = i.toDouble() / steps.toDouble()
            val factor = 0.5 * (1.0 - cos(progress * 2.0 * PI))
            val currentLevel = (1 + factor * (maxLevel - 1)).roundToInt().coerceIn(1, maxLevel)
            flashController.setFlashStrength(currentLevel)
            delay(stepDelayMs)
        }
        flashController.turnOff()
    }

    private suspend fun runFadeOut(
        maxLevel: Int,
        steps: Int = 12
    ) {
        if (maxLevel <= 1) {
            flashController.setFlashStrength(1)
            delay(120L)
            flashController.turnOff()
            return
        }

        // Fast attack to maxLevel
        flashController.setFlashStrength(maxLevel)
        delay(35L)

        // Smooth gradual decay down to 1
        for (i in 1..steps) {
            val progress = i.toDouble() / steps.toDouble()
            val factor = 1.0 - progress
            val currentLevel = (1 + factor * (maxLevel - 1)).roundToInt().coerceIn(1, maxLevel)
            flashController.setFlashStrength(currentLevel)
            delay(20L)
        }
        flashController.turnOff()
    }

    private suspend fun runFadeIn(
        maxLevel: Int,
        steps: Int = 12
    ) {
        if (maxLevel <= 1) {
            flashController.setFlashStrength(1)
            delay(120L)
            flashController.turnOff()
            return
        }

        // Smooth gradual rise from 1 to maxLevel
        for (i in 0..steps) {
            val progress = i.toDouble() / steps.toDouble()
            val currentLevel = (1 + progress * (maxLevel - 1)).roundToInt().coerceIn(1, maxLevel)
            flashController.setFlashStrength(currentLevel)
            delay(20L)
        }
        delay(35L)
        flashController.turnOff()
    }
}
