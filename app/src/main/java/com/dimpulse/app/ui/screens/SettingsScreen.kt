package com.dimpulse.app.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Call
import com.dimpulse.app.data.model.AlertImportanceFilter
import com.dimpulse.app.data.model.CallFlashConfig
import com.dimpulse.app.data.model.CallFlashLoopMode
import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.data.model.LightProfilePreset
import com.dimpulse.app.data.model.TriggerOrientation
import com.dimpulse.app.ui.components.LedConfigurationEditor
import com.dimpulse.app.ui.theme.AmberPrimary
import com.dimpulse.app.ui.theme.DarkBackground
import com.dimpulse.app.ui.theme.DarkBorder
import com.dimpulse.app.ui.theme.DarkSurface
import com.dimpulse.app.ui.theme.DarkSurfaceVariant
import com.dimpulse.app.ui.theme.TextMuted
import com.dimpulse.app.ui.theme.TextPrimary
import com.dimpulse.app.ui.theme.TextSecondary
import com.dimpulse.app.ui.viewmodel.MainViewModel
import com.dimpulse.app.util.formatMinutesToTimeString
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val globalSettings by mainViewModel.globalSettings.collectAsState()
    val hardwareInfo = mainViewModel.hardwareInfo
    val safeMax = hardwareInfo.maxStrengthLevel.coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: HiLight Orientation & Trigger Behavior
        SectionHeader(title = "HILIGHT TRIGGER MODE")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Desk & Orientation Gating",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Control when the ambient LED is permitted to flash",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                TriggerOrientation.entries.forEach { orientation ->
                    val isSelected = orientation == globalSettings.triggerOrientation
                    val bg = if (isSelected) AmberPrimary.copy(alpha = 0.15f) else DarkSurfaceVariant
                    val border = if (isSelected) AmberPrimary else DarkBorder

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(12.dp))
                            .clickable {
                                mainViewModel.updateSettings { it.copy(triggerOrientation = orientation) }
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = orientation.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) AmberPrimary else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = orientation.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Alert Sound & Silent Filter
        SectionHeader(title = "ALERT IMPORTANCE FILTER")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Notification Sound Level Filtering",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Mute silent background syncs while preserving alerts when phone ringer is muted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                AlertImportanceFilter.entries.forEach { filter ->
                    val isSelected = filter == globalSettings.alertImportanceFilter
                    val bg = if (isSelected) AmberPrimary.copy(alpha = 0.15f) else DarkSurfaceVariant
                    val border = if (isSelected) AmberPrimary else DarkBorder

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(12.dp))
                            .clickable {
                                mainViewModel.updateSettings { it.copy(alertImportanceFilter = filter) }
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = filter.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) AmberPrimary else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = filter.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Global Defaults & Waveform Tuning
        SectionHeader(title = "DEFAULT WAVEFORM & BRIGHTNESS")

        val isTestingSettings by mainViewModel.isTestingPulse.collectAsState()

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LedConfigurationEditor(
                    initialPreset = globalSettings.defaultProfilePreset,
                    initialRepeatCount = globalSettings.defaultRepeatCount,
                    initialStrength = globalSettings.defaultStrength,
                    maxStrengthLevel = hardwareInfo.maxStrengthLevel,
                    initialFadeInMs = globalSettings.defaultFadeInMs,
                    initialStayOnMs = globalSettings.defaultStayOnMs,
                    initialFadeOutMs = globalSettings.defaultFadeOutMs,
                    initialGapMs = globalSettings.defaultGapMs,
                    initialCooldownSeconds = globalSettings.cooldownSeconds,
                    initialImportanceFilter = globalSettings.alertImportanceFilter,
                    showDndBypassToggle = false,
                    showImportanceFilter = true,
                    isTesting = isTestingSettings,
                    onTestClick = { pattern, strength ->
                        mainViewModel.triggerTestPulse(pattern, strength)
                    },
                    onStopTestClick = {
                        mainViewModel.stopTestPulse()
                    },
                    saveButtonText = "Save Global Defaults",
                    onSaveClick = { preset, count, strength, fadeIn, stayOn, fadeOut, gap, cooldown, importance, _ ->
                        mainViewModel.updateSettings {
                            it.copy(
                                defaultProfilePreset = preset,
                                defaultRepeatCount = count,
                                defaultStrength = strength,
                                defaultFadeInMs = fadeIn,
                                defaultStayOnMs = stayOn,
                                defaultFadeOutMs = fadeOut,
                                defaultGapMs = gap,
                                cooldownSeconds = cooldown,
                                alertImportanceFilter = importance
                            )
                        }
                    }
                )
            }
        }

        // Section: Incoming Calls (Voice & Video)
        SectionHeader(title = "INCOMING CALLS (VOICE & VIDEO)")

        val isTestingCall by mainViewModel.isTestingCall.collectAsState()
        val callConfig = globalSettings.callConfig

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Incoming Calls Master Toggle Row
                SettingToggleRow(
                    icon = Icons.Default.Call,
                    title = "Incoming Call Flash",
                    subtitle = "Flash LED when a voice/video call rings (GSM, WhatsApp, Telegram, etc.)",
                    checked = callConfig.isEnabled,
                    onCheckedChange = { isEnabled ->
                        mainViewModel.updateSettings {
                            it.copy(callConfig = it.callConfig.copy(isEnabled = isEnabled))
                        }
                    }
                )

                if (callConfig.isEnabled) {
                    // Loop Mode Selector: Continuous vs One-Time
                    Column {
                        Text(
                            text = "RINGING FLASH MODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CallFlashLoopMode.entries.forEach { mode ->
                                val isSel = mode == callConfig.loopMode
                                val bg = if (isSel) AmberPrimary else DarkSurfaceVariant
                                val textCol = if (isSel) DarkBackground else TextPrimary

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bg)
                                        .border(1.dp, if (isSel) AmberPrimary else DarkBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            mainViewModel.updateSettings {
                                                it.copy(callConfig = it.callConfig.copy(loopMode = mode))
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = textCol
                                    )
                                }
                            }
                        }
                    }

                    // Sequence Interval (Cadence delay between flash sequences) if continuous
                    if (callConfig.loopMode == CallFlashLoopMode.CONTINUOUS_LOOP) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            TimingSliderWithInput(
                                label = "Cadence Interval (Wait Between Bursts)",
                                valueMs = callConfig.sequenceIntervalMs,
                                minMs = 300L,
                                maxMs = 5000L,
                                stepIncrement = 100L,
                                unit = "ms",
                                onValueChange = { ms ->
                                    mainViewModel.updateSettings {
                                        it.copy(callConfig = it.callConfig.copy(sequenceIntervalMs = ms))
                                    }
                                }
                            )
                            Text(
                                text = "Pause duration between repeated flash sequences while ringing (e.g. .x.x.x ... .x.x.x)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Silence Call Flash on Lift Toggle
                    SettingToggleRow(
                        icon = Icons.Default.Sensors,
                        title = "Silence Flash on Lift",
                        subtitle = "Instantly stop flashing as soon as you pick the phone up from the desk",
                        checked = callConfig.silenceOnLift,
                        onCheckedChange = { checked ->
                            mainViewModel.updateSettings {
                                it.copy(callConfig = it.callConfig.copy(silenceOnLift = checked))
                            }
                        }
                    )

                    // Shared Unified LED Configuration Editor for Calls
                    LedConfigurationEditor(
                        initialPreset = callConfig.profilePreset,
                        initialRepeatCount = callConfig.repeatCount,
                        initialStrength = callConfig.strengthLevel,
                        maxStrengthLevel = hardwareInfo.maxStrengthLevel,
                        initialFadeInMs = callConfig.fadeInMs,
                        initialStayOnMs = callConfig.stayOnMs,
                        initialFadeOutMs = callConfig.fadeOutMs,
                        initialGapMs = callConfig.gapMs,
                        initialCooldownSeconds = 0,
                        initialImportanceFilter = AlertImportanceFilter.ONLY_ALERTING,
                        initialBypassDnd = callConfig.bypassDnd,
                        showDndBypassToggle = true,
                        showImportanceFilter = false,
                        showDebouncer = false,
                        isTesting = isTestingCall,
                        onTestClick = { _, _ ->
                            mainViewModel.triggerTestCallCadence(callConfig)
                        },
                        onStopTestClick = {
                            mainViewModel.stopTestPulse()
                        },
                        saveButtonText = "Save Call Settings",
                        onSaveClick = { preset, count, strength, fadeIn, stayOn, fadeOut, gap, _, _, bypass ->
                            mainViewModel.updateSettings {
                                it.copy(
                                    callConfig = it.callConfig.copy(
                                        profilePreset = preset,
                                        repeatCount = count,
                                        strengthLevel = strength,
                                        fadeInMs = fadeIn,
                                        stayOnMs = stayOn,
                                        fadeOutMs = fadeOut,
                                        gapMs = gap,
                                        bypassDnd = bypass
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }

        // Section 5: Environmental Filters
        SectionHeader(title = "ENVIRONMENTAL FILTERS")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Screen Off Check
                SettingToggleRow(
                    icon = Icons.Default.ScreenLockPortrait,
                    title = "Only When Screen is Off",
                    subtitle = "Suppress LED flashes while actively using the device",
                    checked = globalSettings.onlyWhenScreenOff,
                    onCheckedChange = { checked ->
                        mainViewModel.updateSettings { it.copy(onlyWhenScreenOff = checked) }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Respect DND
                SettingToggleRow(
                    icon = Icons.Default.DoNotDisturb,
                    title = "Respect Do Not Disturb",
                    subtitle = "Mute flashes when system DND / Priority filter is active",
                    checked = globalSettings.respectDnd,
                    onCheckedChange = { checked ->
                        mainViewModel.updateSettings { it.copy(respectDnd = checked) }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quiet Hours
                SettingToggleRow(
                    icon = Icons.Default.Bedtime,
                    title = "Quiet Hours Schedule",
                    subtitle = "Mute flashes during set sleep hours",
                    checked = globalSettings.quietHoursEnabled,
                    onCheckedChange = { checked ->
                        mainViewModel.updateSettings { it.copy(quietHoursEnabled = checked) }
                    }
                )

                if (globalSettings.quietHoursEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimePickerButton(
                            label = "Start",
                            timeMinutes = globalSettings.quietHoursStartMinutes,
                            onTimeSelected = { newMins ->
                                mainViewModel.updateSettings { it.copy(quietHoursStartMinutes = newMins) }
                            }
                        )
                        Text(text = "to", color = TextSecondary, fontSize = 13.sp)
                        TimePickerButton(
                            label = "End",
                            timeMinutes = globalSettings.quietHoursEndMinutes,
                            onTimeSelected = { newMins ->
                                mainViewModel.updateSettings { it.copy(quietHoursEndMinutes = newMins) }
                            }
                        )
                    }
                }
            }
        }

        // Section 5: Privacy & Attribution
        SectionHeader(title = "PRIVACY & ATTRIBUTION")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "100% Offline & Private",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DimPulse does not request INTERNET permissions and operates strictly on-device. No telemetry, no logs, zero cloud dependencies.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "DimPulse (Mimics HiLight on Android 13+)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Architected & AI Generated by Gemini 3.7 Flash (High)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AmberPrimary,
                checkedTrackColor = DarkSurfaceVariant,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
private fun TimePickerButton(
    label: String,
    timeMinutes: Int,
    onTimeSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val hour = timeMinutes / 60
    val minute = timeMinutes % 60

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .clickable {
                TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        onTimeSelected(selectedHour * 60 + selectedMinute)
                    },
                    hour,
                    minute,
                    true
                ).show()
            }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = AmberPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$label: ${formatMinutesToTimeString(timeMinutes)}",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
