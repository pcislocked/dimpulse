package com.dimpulse.app.engine

import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.data.model.PatternType
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
                        withTimeout(5000L) { // Safety watchdog: 5-second hard cutoff
                            when (pattern.type) {
                                PatternType.SINGLE_PULSE -> {
                                    runDiscrete(
                                        count = 1,
                                        strengthLevel = strengthLevel,
                                        onDuration = pattern.onDurationMs,
                                        offDuration = 0L
                                    )
                                }
                                PatternType.DOUBLE_PULSE -> {
                                    runDiscrete(
                                        count = 2,
                                        strengthLevel = strengthLevel,
                                        onDuration = pattern.onDurationMs,
                                        offDuration = pattern.offDurationMs
                                    )
                                }
                                PatternType.TRIPLE_PULSE -> {
                                    runDiscrete(
                                        count = 3,
                                        strengthLevel = strengthLevel,
                                        onDuration = pattern.onDurationMs,
                                        offDuration = pattern.offDurationMs
                                    )
                                }
                                PatternType.BREATHING -> {
                                    runBreathing(
                                        maxLevel = strengthLevel,
                                        cycles = pattern.repeatCount.coerceAtLeast(1),
                                        steps = pattern.breathingSteps,
                                        stepDelayMs = pattern.breathingStepDelayMs
                                    )
                                }
                                PatternType.RAPID_STROBE -> {
                                    runDiscrete(
                                        count = 4,
                                        strengthLevel = strengthLevel,
                                        onDuration = 40L,
                                        offDuration = 50L
                                    )
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

    private suspend fun runDiscrete(
        count: Int,
        strengthLevel: Int,
        onDuration: Long,
        offDuration: Long
    ) {
        for (i in 0 until count) {
            flashController.setFlashStrength(strengthLevel)
            delay(onDuration)
            flashController.turnOff()
            if (i < count - 1 && offDuration > 0) {
                delay(offDuration)
            }
        }
    }

    private suspend fun runBreathing(
        maxLevel: Int,
        cycles: Int,
        steps: Int = 16,
        stepDelayMs: Long = 25L
    ) {
        for (c in 0 until cycles) {
            if (maxLevel <= 1) {
                // If maximum level is 1 (or binary only), ramp time using PWM-like discrete micro pulses
                flashController.setFlashStrength(1)
                delay(180L)
                flashController.turnOff()
                delay(100L)
                flashController.setFlashStrength(1)
                delay(180L)
                flashController.turnOff()
            } else {
                // Sinusoidal ramp: L(k) = round(1 + ((maxLevel - 1) / 2) * (1 - cos(2*PI*k / steps)))
                for (k in 0..steps) {
                    val angle = 2.0 * PI * k / steps
                    val factor = (1.0 - cos(angle)) / 2.0
                    val currentStrength = (1 + (maxLevel - 1) * factor).roundToInt()

                    flashController.setFlashStrength(currentStrength)
                    delay(stepDelayMs)
                }
            }
            if (c < cycles - 1) {
                delay(150L)
            }
        }
    }

    fun stop() {
        scope.launch {
            mutex.withLock {
                activeJob?.cancelAndJoin()
                flashController.turnOff()
            }
        }
    }
}
