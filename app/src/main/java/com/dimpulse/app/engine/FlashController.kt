package com.dimpulse.app.engine

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log

class FlashController(
    private val context: Context,
    private val hardwareInfo: FlashHardwareInfo
) {
    private val tag = "DimPulse_FlashCtrl"
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = hardwareInfo.cameraId

    @Volatile
    private var isTorchOnByOtherApp = false

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(camId: String, enabled: Boolean) {
            if (camId == cameraId) {
                // If turned on externally while we are not driving it
                isTorchOnByOtherApp = enabled
            }
        }

        override fun onTorchModeUnavailable(camId: String) {
            if (camId == cameraId) {
                Log.w(tag, "Camera torch unavailable (e.g. camera in use)")
            }
        }
    }

    init {
        try {
            cameraManager.registerTorchCallback(torchCallback, null)
        } catch (e: Exception) {
            Log.e(tag, "Failed to register torch callback: ${e.message}")
        }
    }

    fun isExternalTorchActive(): Boolean = isTorchOnByOtherApp

    fun setFlashStrength(level: Int) {
        val targetCamId = cameraId ?: return
        val safeLevel = level.coerceIn(1, hardwareInfo.maxStrengthLevel)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hardwareInfo.supportsGranularDimming) {
                cameraManager.turnOnTorchWithStrengthLevel(targetCamId, safeLevel)
            } else {
                cameraManager.setTorchMode(targetCamId, true)
            }
        } catch (e: CameraAccessException) {
            Log.w(tag, "CameraAccessException setting torch strength: ${e.message}")
        } catch (e: Exception) {
            Log.e(tag, "Exception setting torch strength: ${e.message}")
        }
    }

    fun turnOff() {
        val targetCamId = cameraId ?: return
        try {
            cameraManager.setTorchMode(targetCamId, false)
        } catch (e: CameraAccessException) {
            Log.w(tag, "CameraAccessException turning off torch: ${e.message}")
        } catch (e: Exception) {
            Log.e(tag, "Exception turning off torch: ${e.message}")
        }
    }

    fun cleanup() {
        try {
            cameraManager.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {
            Log.e(tag, "Failed to unregister torch callback: ${e.message}")
        }
    }
}
