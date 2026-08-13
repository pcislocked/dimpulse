package com.dimpulse.app

import android.app.Application
import com.dimpulse.app.data.repository.FlashConfigRepository
import com.dimpulse.app.engine.FlashController
import com.dimpulse.app.engine.FlashHardwareInfo
import com.dimpulse.app.engine.HardwareDiagnostics
import com.dimpulse.app.engine.ProximitySensorHelper
import com.dimpulse.app.engine.PulseEngine

class DimPulseApp : Application() {

    lateinit var repository: FlashConfigRepository
        private set

    lateinit var hardwareInfo: FlashHardwareInfo
        private set

    lateinit var flashController: FlashController
        private set

    lateinit var pulseEngine: PulseEngine
        private set

    lateinit var proximityHelper: ProximitySensorHelper
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        repository = FlashConfigRepository(this)
        val diagnostics = HardwareDiagnostics(this)
        hardwareInfo = diagnostics.probeFlashHardware()
        flashController = FlashController(this, hardwareInfo)
        pulseEngine = PulseEngine(flashController)
        proximityHelper = ProximitySensorHelper(this)
    }

    companion object {
        lateinit var instance: DimPulseApp
            private set
    }
}
