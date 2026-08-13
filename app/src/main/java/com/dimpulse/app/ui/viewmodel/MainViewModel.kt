package com.dimpulse.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimpulse.app.DimPulseApp
import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.data.model.GlobalFlashSettings
import com.dimpulse.app.engine.FlashHardwareInfo
import com.dimpulse.app.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = DimPulseApp.instance.repository
    private val pulseEngine = DimPulseApp.instance.pulseEngine
    val hardwareInfo: FlashHardwareInfo = DimPulseApp.instance.hardwareInfo

    val globalSettings: StateFlow<GlobalFlashSettings> = repository.globalSettings

    private val _isNotificationAccessGranted = MutableStateFlow(false)
    val isNotificationAccessGranted: StateFlow<Boolean> = _isNotificationAccessGranted.asStateFlow()

    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    private val _isTestingPulse = MutableStateFlow(false)
    val isTestingPulse: StateFlow<Boolean> = _isTestingPulse.asStateFlow()

    fun refreshPermissions(context: Context) {
        _isNotificationAccessGranted.value = PermissionUtils.isNotificationListenerGranted(context)
        _isBatteryOptimizationIgnored.value = PermissionUtils.isBatteryOptimizationIgnored(context)
    }

    fun setMasterEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateGlobalSettings { it.copy(masterEnabled = enabled) }
        }
    }

    fun updateSettings(transform: (GlobalFlashSettings) -> GlobalFlashSettings) {
        viewModelScope.launch {
            repository.updateGlobalSettings(transform)
        }
    }

    fun triggerTestPulse(pattern: FlashPattern, strength: Int) {
        if (_isTestingPulse.value) {
            pulseEngine.stop()
            _isTestingPulse.value = false
            return
        }

        _isTestingPulse.value = true
        pulseEngine.triggerPattern(pattern, strength) {
            _isTestingPulse.value = false
        }
    }

    fun stopTestPulse() {
        pulseEngine.stop()
        _isTestingPulse.value = false
    }
}
