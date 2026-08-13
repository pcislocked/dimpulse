package com.dimpulse.app.engine

import com.dimpulse.app.data.model.FlashPattern
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

    fun stop() {
        scope.launch {
            mutex.withLock {
                activeJob?.cancelAndJoin()
                flashController.turnOff()
            }
        }
    }

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
                        withTimeout(8000L) { // Safety watchdog: 8-second hard cutoff
                            val targetLevel = strengthLevel.coerceAtLeast(1)
                            val repeats = pattern.repeatCount.coerceIn(1, 4)

                            for (r in 0 until repeats) {
                                executeSinglePulse(
                                    maxLevel = targetLevel,
                                    fadeInMs = pattern.fadeInMs.coerceAtLeast(0L),
                                    stayOnMs = pattern.stayOnMs.coerceAtLeast(0L),
                                    fadeOutMs = pattern.fadeOutMs.coerceAtLeast(0L)
                                )

                                if (r < repeats - 1) {
                                    flashController.turnOff()
                                    val gap = pattern.gapMs.coerceAtLeast(20L)
                                    delay(gap)
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

    private suspend fun executeSinglePulse(
        maxLevel: Int,
        fadeInMs: Long,
        stayOnMs: Long,
        fadeOutMs: Long
    ) {
        // If device has only binary torch or level 1, simple square pulse
        if (maxLevel <= 1) {
            flashController.setFlashStrength(1)
            val totalOn = (fadeInMs + stayOnMs + fadeOutMs).coerceIn(40L, 400L)
            delay(totalOn)
            flashController.turnOff()
            return
        }

        // 1. Fade-In Ramp
        if (fadeInMs > 0) {
            val steps = ((fadeInMs / 15L).toInt()).coerceIn(4, 16)
            val stepDelay = (fadeInMs / steps).coerceAtLeast(10L)
            for (i in 0..steps) {
                val progress = i.toDouble() / steps.toDouble()
                // Cosine easing: 0 -> 1
                val factor = 0.5 * (1.0 - cos(progress * PI))
                val currentLevel = (1 + factor * (maxLevel - 1)).roundToInt().coerceIn(1, maxLevel)
                flashController.setFlashStrength(currentLevel)
                delay(stepDelay)
            }
        } else {
            flashController.setFlashStrength(maxLevel)
        }

        // 2. Stay-On Peak Hold
        if (stayOnMs > 0) {
            flashController.setFlashStrength(maxLevel)
            delay(stayOnMs)
        }

        // 3. Fade-Out Decay
        if (fadeOutMs > 0) {
            val steps = ((fadeOutMs / 15L).toInt()).coerceIn(4, 16)
            val stepDelay = (fadeOutMs / steps).coerceAtLeast(10L)
            for (i in 1..steps) {
                val progress = i.toDouble() / steps.toDouble()
                val factor = 0.5 * (1.0 + cos(progress * PI))
                val currentLevel = (1 + factor * (maxLevel - 1)).roundToInt().coerceIn(1, maxLevel)
                flashController.setFlashStrength(currentLevel)
                delay(stepDelay)
            }
        }

        flashController.turnOff()
    }
}
