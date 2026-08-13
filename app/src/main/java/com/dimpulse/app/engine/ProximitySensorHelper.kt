package com.dimpulse.app.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.dimpulse.app.data.model.TriggerOrientation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

data class SensorSnapshot(
    val isProximityCovered: Boolean = false,
    val isFlatFaceDown: Boolean = false,
    val isFlatFaceUp: Boolean = false,
    val isVerticalTilt: Boolean = false
)

class ProximitySensorHelper(private val context: Context) {
    private val tag = "DimPulse_Sensors"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val mainHandler = Handler(Looper.getMainLooper())

    suspend fun getSensorSnapshot(timeoutMs: Long = 180L): SensorSnapshot {
        if (sensorManager == null) {
            return SensorSnapshot()
        }

        return try {
            withContext(Dispatchers.Main) {
                withTimeoutOrNull(timeoutMs) {
                    callbackFlow {
                        var proxCovered = false
                        var accelSampled = false
                        var zVal = 0f
                        var xVal = 0f
                        var yVal = 0f

                        val listener = object : SensorEventListener {
                            override fun onSensorChanged(event: SensorEvent?) {
                                if (event == null) return

                                if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                                    val distance = event.values.getOrNull(0) ?: Float.MAX_VALUE
                                    val maxRange = proximitySensor?.maximumRange ?: 5f
                                    proxCovered = distance < 1.0f || distance < maxRange
                                } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                                    xVal = event.values.getOrNull(0) ?: 0f
                                    yVal = event.values.getOrNull(1) ?: 0f
                                    zVal = event.values.getOrNull(2) ?: 0f
                                    accelSampled = true
                                }

                                if (accelSampled) {
                                    val isFaceDown = zVal < -6.5f && abs(xVal) < 5.0f && abs(yVal) < 5.0f
                                    val isFaceUp = zVal > 6.5f && abs(xVal) < 5.0f && abs(yVal) < 5.0f
                                    val isVertical = abs(yVal) > 6.5f && abs(zVal) < 5.5f

                                    trySend(
                                        SensorSnapshot(
                                            isProximityCovered = proxCovered,
                                            isFlatFaceDown = isFaceDown,
                                            isFlatFaceUp = isFaceUp,
                                            isVerticalTilt = isVertical
                                        )
                                    )
                                }
                            }

                            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                        }

                        try {
                            proximitySensor?.let {
                                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_FASTEST, mainHandler)
                            }
                            accelerometer?.let {
                                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_FASTEST, mainHandler)
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Error registering sensor listener: ${e.message}")
                            trySend(SensorSnapshot())
                        }

                        awaitClose {
                            try {
                                sensorManager.unregisterListener(listener)
                            } catch (e: Exception) {
                                Log.e(tag, "Error unregistering sensor listener: ${e.message}")
                            }
                        }
                    }.first()
                } ?: SensorSnapshot()
            }
        } catch (e: Exception) {
            Log.e(tag, "Sensor snapshot failed gracefully: ${e.message}")
            SensorSnapshot()
        }
    }

    suspend fun shouldAllowFlash(mode: TriggerOrientation): Boolean {
        return try {
            val snapshot = getSensorSnapshot()

            when (mode) {
                TriggerOrientation.ANY_ORIENTATION -> true

                TriggerOrientation.ONLY_FACE_DOWN -> {
                    snapshot.isFlatFaceDown
                }

                TriggerOrientation.EXCEPT_IN_POCKET -> {
                    if (snapshot.isFlatFaceDown || snapshot.isFlatFaceUp) {
                        true
                    } else if (snapshot.isVerticalTilt && snapshot.isProximityCovered) {
                        false
                    } else {
                        true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "shouldAllowFlash fallback to true: ${e.message}")
            true
        }
    }
}
