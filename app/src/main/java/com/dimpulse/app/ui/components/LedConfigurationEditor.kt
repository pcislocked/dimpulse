package com.dimpulse.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimpulse.app.data.model.AlertImportanceFilter
import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.data.model.LightProfilePreset
import com.dimpulse.app.ui.theme.AccentSuccess
import com.dimpulse.app.ui.theme.AmberPrimary
import com.dimpulse.app.ui.theme.DarkBackground
import com.dimpulse.app.ui.theme.DarkBorder
import com.dimpulse.app.ui.theme.DarkSurface
import com.dimpulse.app.ui.theme.DarkSurfaceVariant
import com.dimpulse.app.ui.theme.TextMuted
import com.dimpulse.app.ui.theme.TextPrimary
import com.dimpulse.app.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun LedConfigurationEditor(
    initialPreset: LightProfilePreset,
    initialRepeatCount: Int,
    initialStrength: Int,
    maxStrengthLevel: Int,
    initialFadeInMs: Long,
    initialStayOnMs: Long,
    initialFadeOutMs: Long,
    initialGapMs: Long,
    initialCooldownSeconds: Int,
    initialImportanceFilter: AlertImportanceFilter = AlertImportanceFilter.ONLY_ALERTING,
    initialBypassDnd: Boolean = false,
    showDndBypassToggle: Boolean = true,
    showImportanceFilter: Boolean = true,
    showDebouncer: Boolean = true,
    isTesting: Boolean = false,
    onTestClick: (FlashPattern, Int) -> Unit,
    onStopTestClick: () -> Unit,
    saveButtonText: String = "Save Configuration",
    onSaveClick: (
        preset: LightProfilePreset,
        repeatCount: Int,
        strength: Int,
        fadeInMs: Long,
        stayOnMs: Long,
        fadeOutMs: Long,
        gapMs: Long,
        cooldownSeconds: Int,
        importanceFilter: AlertImportanceFilter,
        bypassDnd: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember(initialPreset) { mutableStateOf(initialPreset) }
    var repeatCount by remember(initialRepeatCount) { mutableIntStateOf(initialRepeatCount) }
    var strength by remember(initialStrength) { mutableFloatStateOf(initialStrength.toFloat()) }

    var fadeInMs by remember(initialFadeInMs) { mutableLongStateOf(initialFadeInMs) }
    var stayOnMs by remember(initialStayOnMs) { mutableLongStateOf(initialStayOnMs) }
    var fadeOutMs by remember(initialFadeOutMs) { mutableLongStateOf(initialFadeOutMs) }
    var gapMs by remember(initialGapMs) { mutableLongStateOf(initialGapMs) }

    var cooldownSeconds by remember(initialCooldownSeconds) { mutableIntStateOf(initialCooldownSeconds) }
    var importanceFilter by remember(initialImportanceFilter) { mutableStateOf(initialImportanceFilter) }
    var bypassDnd by remember(initialBypassDnd) { mutableStateOf(initialBypassDnd) }

    var showSavedFeedback by remember { mutableStateOf(false) }
    val safeMax = maxStrengthLevel.coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Light Profile Presets
        Column {
            Text(
                text = "LIGHT PROFILE",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LightProfilePreset.entries.forEach { preset ->
                    val isSelected = preset == selectedPreset
                    val bg = if (isSelected) AmberPrimary else DarkSurfaceVariant
                    val textCol = if (isSelected) DarkBackground else TextPrimary

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .border(1.dp, if (isSelected) AmberPrimary else DarkBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                selectedPreset = preset
                                showSavedFeedback = false
                                if (preset != LightProfilePreset.CUSTOM) {
                                    fadeInMs = preset.defaultFadeInMs
                                    stayOnMs = preset.defaultStayOnMs
                                    fadeOutMs = preset.defaultFadeOutMs
                                    gapMs = preset.defaultGapMs
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = preset.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textCol
                        )
                    }
                }
            }
        }

        // 2. Pulse Multiplier (1x, 2x, 3x, 4x)
        Column {
            Text(
                text = "PULSE MULTIPLIER",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    1 to "Single (1x)",
                    2 to "Double (2x)",
                    3 to "Triple (3x)",
                    4 to "Quad (4x)"
                ).forEach { (count, label) ->
                    val isSelected = count == repeatCount
                    val bg = if (isSelected) AmberPrimary else DarkSurfaceVariant
                    val textCol = if (isSelected) DarkBackground else TextPrimary

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .border(1.dp, if (isSelected) AmberPrimary else DarkBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                repeatCount = count
                                showSavedFeedback = false
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = textCol
                        )
                    }
                }
            }
        }

        // 3. Peak Brightness Level
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PEAK BRIGHTNESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Level ${strength.roundToInt()} of $safeMax",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = strength,
                onValueChange = {
                    strength = it
                    showSavedFeedback = false
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
        }

        // 4. Granular Timing Controls (Both Slider AND Enterable Field)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "WAVEFORM TIMING ENVELOPE (MILLISECONDS)",
                style = MaterialTheme.typography.labelSmall,
                color = AmberPrimary,
                fontWeight = FontWeight.Bold
            )

            // Fade-In Time
            TimingSliderWithInput(
                label = "Fade-In Rise",
                valueMs = fadeInMs,
                maxMs = 800L,
                onValueChange = {
                    fadeInMs = it
                    selectedPreset = LightProfilePreset.CUSTOM
                    showSavedFeedback = false
                }
            )

            // Stay-On Time
            TimingSliderWithInput(
                label = "Peak Hold (Stay On)",
                valueMs = stayOnMs,
                maxMs = 800L,
                onValueChange = {
                    stayOnMs = it
                    selectedPreset = LightProfilePreset.CUSTOM
                    showSavedFeedback = false
                }
            )

            // Fade-Out Time
            TimingSliderWithInput(
                label = "Fade-Out Decay",
                valueMs = fadeOutMs,
                maxMs = 800L,
                onValueChange = {
                    fadeOutMs = it
                    selectedPreset = LightProfilePreset.CUSTOM
                    showSavedFeedback = false
                }
            )

            // Inter-Pulse Gap (only relevant if repeatCount > 1)
            if (repeatCount > 1) {
                TimingSliderWithInput(
                    label = "Inter-Pulse Gap",
                    valueMs = gapMs,
                    maxMs = 600L,
                    onValueChange = {
                        gapMs = it
                        selectedPreset = LightProfilePreset.CUSTOM
                        showSavedFeedback = false
                    }
                )
            }
        }

        // 5. Debouncer / Burst Rate Limit (Both Slider AND Enterable Field)
        if (showDebouncer) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                TimingSliderWithInput(
                    label = "Burst Rate Limit (Debounce)",
                    valueMs = cooldownSeconds.toLong(),
                    maxMs = 30L,
                    unit = "s",
                    onValueChange = {
                        cooldownSeconds = it.toInt()
                        showSavedFeedback = false
                    }
                )
                Text(
                    text = if (cooldownSeconds == 0) "No rate limit (fires on every notification)" else "Max 1 flash sequence per ${cooldownSeconds}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 6. Importance Filter
        if (showImportanceFilter) {
            Column {
                Text(
                    text = "ALERT IMPORTANCE FILTER",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        AlertImportanceFilter.ONLY_ALERTING to "Only Loud (Alerting)",
                        AlertImportanceFilter.ALL_INCLUDING_SILENT to "All (Inc. Silent)",
                        AlertImportanceFilter.NONE to "None (Mute)"
                    ).forEach { (filter, label) ->
                        val isSel = filter == importanceFilter
                        val bg = if (isSel) AmberPrimary else DarkSurfaceVariant
                        val textCol = if (isSel) DarkBackground else TextPrimary

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .border(1.dp, if (isSel) AmberPrimary else DarkBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    importanceFilter = filter
                                    showSavedFeedback = false
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = textCol
                            )
                        }
                    }
                }
            }
        }

        // 7. Bypass DND Toggle
        if (showDndBypassToggle) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bypass Do Not Disturb",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Flash even when priority/silent DND is active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = bypassDnd,
                    onCheckedChange = {
                        bypassDnd = it
                        showSavedFeedback = false
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberPrimary,
                        checkedTrackColor = DarkSurface,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurface
                    )
                )
            }
        }

        // 8. Test & Save Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isTesting) {
                Button(
                    onClick = onStopTestClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPrimary.copy(alpha = 0.2f),
                        contentColor = AmberPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stop Test", fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = {
                        val pattern = FlashPattern(
                            preset = selectedPreset,
                            repeatCount = repeatCount,
                            fadeInMs = fadeInMs,
                            stayOnMs = stayOnMs,
                            fadeOutMs = fadeOutMs,
                            gapMs = gapMs
                        )
                        onTestClick(pattern, strength.roundToInt())
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = AmberPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Waveform", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Button(
                onClick = {
                    onSaveClick(
                        selectedPreset,
                        repeatCount,
                        strength.roundToInt(),
                        fadeInMs,
                        stayOnMs,
                        fadeOutMs,
                        gapMs,
                        cooldownSeconds,
                        importanceFilter,
                        bypassDnd
                    )
                    showSavedFeedback = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberPrimary,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (showSavedFeedback) Icons.Default.CheckCircle else Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showSavedFeedback) "Saved!" else saveButtonText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TimingSliderWithInput(
    label: String,
    valueMs: Long,
    maxMs: Long,
    minMs: Long = 0L,
    stepIncrement: Long = 10L,
    unit: String = "ms",
    onValueChange: (Long) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var textValue by remember(valueMs) { mutableStateOf(valueMs.toString()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )

            // Direct Enterable Number Field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        textValue = filtered
                        val parsed = filtered.toLongOrNull()
                        if (parsed != null) {
                            onValueChange(parsed)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = AmberPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = AmberPrimary,
                        unfocusedTextColor = AmberPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(72.dp)
                )
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Slider(
            value = valueMs.toFloat().coerceIn(minMs.toFloat(), maxMs.toFloat()),
            onValueChange = { raw ->
                val snapped = if (stepIncrement > 1) {
                    ((kotlin.math.round((raw - minMs) / stepIncrement.toFloat()) * stepIncrement) + minMs).toLong().coerceIn(minMs, maxMs)
                } else {
                    raw.toLong().coerceIn(minMs, maxMs)
                }
                onValueChange(snapped)
                textValue = snapped.toString()
            },
            valueRange = minMs.toFloat()..maxMs.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = AmberPrimary,
                activeTrackColor = AmberPrimary,
                inactiveTrackColor = DarkSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
