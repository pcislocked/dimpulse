package com.dimpulse.app.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class ProximitySensorHelper(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    suspend fun isCoveredOrFaceDown(timeoutMs: Long = 150L): Boolean {
        if (sensorManager == null || proximitySensor == null) {
            return false // No sensor present, do not block notifications
        }

        return withTimeoutOrNull(timeoutMs) {
            callbackFlow {
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event?.sensor?.type == Sensor.TYPE_PROXIMITY) {
                            val distance = event.values.getOrNull(0) ?: Float.MAX_VALUE
                            val maxRange = proximitySensor.maximumRange
                            // Proximity sensors typically return 0 for covered/near, or maxRange for unblocked
                            val isCovered = distance < 1.0f || distance < maxRange
                            trySend(isCovered)
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }

                sensorManager.registerListener(
                    listener,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )

                awaitClose {
                    sensorManager.unregisterListener(listener)
                }
            }.first()
        } ?: false // Default to unblocked if timeout occurs
    }
}
