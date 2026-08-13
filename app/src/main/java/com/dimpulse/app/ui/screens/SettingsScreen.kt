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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.dimpulse.app.data.model.PatternType
import com.dimpulse.app.data.model.TriggerOrientation
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

        // Section 2: Global Defaults & Waveform Tuning
        SectionHeader(title = "DEFAULT WAVEFORM & BRIGHTNESS")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Default Waveform Pattern",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PatternType.entries.forEach { pattern ->
                        val isSelected = pattern == globalSettings.defaultPattern
                        val bg = if (isSelected) AmberPrimary else DarkSurfaceVariant
                        val textCol = if (isSelected) DarkBackground else TextPrimary

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .border(1.dp, if (isSelected) AmberPrimary else DarkBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    mainViewModel.updateSettings { it.copy(defaultPattern = pattern) }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = pattern.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = textCol
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Default Brightness",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Level ${globalSettings.defaultStrength} of $safeMax",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = globalSettings.defaultStrength.toFloat(),
                    onValueChange = {
                        mainViewModel.updateSettings { s -> s.copy(defaultStrength = it.roundToInt()) }
                    },
                    valueRange = 1f..safeMax.toFloat(),
                    steps = if (safeMax > 1) safeMax - 2 else 0,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberPrimary,
                        activeTrackColor = AmberPrimary,
                        inactiveTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Repeat Nag Interval
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Missed Alert Reminder",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (globalSettings.repeatIntervalSeconds == 0) "Flash once only" else "Re-flash every ${globalSettings.repeatIntervalSeconds}s while locked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 15, 30, 60).forEach { sec ->
                            val isSel = globalSettings.repeatIntervalSeconds == sec
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AmberPrimary else DarkSurfaceVariant)
                                    .clickable {
                                        mainViewModel.updateSettings { it.copy(repeatIntervalSeconds = sec) }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (sec == 0) "Off" else "${sec}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) DarkBackground else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Smart Environmental Filters
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

        // Section 4: Privacy & Attribution
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
