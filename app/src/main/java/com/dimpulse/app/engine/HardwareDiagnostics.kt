package com.dimpulse.app.engine

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build

data class FlashHardwareInfo(
    val hasFlash: Boolean = false,
    val cameraId: String? = null,
    val maxStrengthLevel: Int = 1,
    val defaultStrengthLevel: Int = 1,
    val supportsGranularDimming: Boolean = false,
    val errorMessage: String? = null
)

class HardwareDiagnostics(private val context: Context) {

    fun probeFlashHardware(): FlashHardwareInfo {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return FlashHardwareInfo(errorMessage = "CameraManager service not found")

        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)

                // We prioritize the rear back-facing camera flash
                if (hasFlash && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    var maxLevel = 1
                    var defaultLevel = 1

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        maxLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                        defaultLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL) ?: 1
                    }

                    return FlashHardwareInfo(
                        hasFlash = true,
                        cameraId = id,
                        maxStrengthLevel = maxLevel,
                        defaultStrengthLevel = defaultLevel,
                        supportsGranularDimming = maxLevel > 1
                    )
                }
            }

            // If no back flash found, try any available flash
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (hasFlash) {
                    var maxLevel = 1
                    var defaultLevel = 1
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        maxLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                        defaultLevel = characteristics.get(CameraCharacteristics.FLASH_INFO_STRENGTH_DEFAULT_LEVEL) ?: 1
                    }
                    return FlashHardwareInfo(
                        hasFlash = true,
                        cameraId = id,
                        maxStrengthLevel = maxLevel,
                        defaultStrengthLevel = defaultLevel,
                        supportsGranularDimming = maxLevel > 1
                    )
                }
            }

            return FlashHardwareInfo(
                hasFlash = false,
                errorMessage = "No camera LED flash detected on this device"
            )
        } catch (e: Exception) {
            return FlashHardwareInfo(
                hasFlash = false,
                errorMessage = "Error probing camera hardware: ${e.message}"
            )
        }
    }
}
