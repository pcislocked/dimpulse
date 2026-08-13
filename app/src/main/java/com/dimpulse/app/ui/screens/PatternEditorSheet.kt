package com.dimpulse.app.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimpulse.app.data.model.AppFlashConfig
import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.data.model.PatternType
import com.dimpulse.app.ui.theme.AccentError
import com.dimpulse.app.ui.theme.AmberPrimary
import com.dimpulse.app.ui.theme.DarkBackground
import com.dimpulse.app.ui.theme.DarkBorder
import com.dimpulse.app.ui.theme.DarkSurface
import com.dimpulse.app.ui.theme.DarkSurfaceVariant
import com.dimpulse.app.ui.theme.TextMuted
import com.dimpulse.app.ui.theme.TextPrimary
import com.dimpulse.app.ui.theme.TextSecondary
import com.dimpulse.app.ui.viewmodel.InstalledAppItem
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternEditorSheet(
    appItem: InstalledAppItem,
    maxStrengthLevel: Int,
    defaultPattern: PatternType,
    defaultStrength: Int,
    onDismiss: () -> Unit,
    onSave: (AppFlashConfig) -> Unit,
    onReset: (String) -> Unit,
    onTestPattern: (FlashPattern, Int) -> Unit
) {
    val existing = appItem.config
    var isEnabled by remember { mutableStateOf(existing?.isEnabled ?: true) }
    var selectedPattern by remember { mutableStateOf(existing?.patternType ?: defaultPattern) }
    var strengthLevel by remember {
        mutableFloatStateOf((existing?.strengthLevel ?: defaultStrength).toFloat())
    }
    var repeatCount by remember { mutableIntStateOf(existing?.repeatCount ?: 1) }
    var bypassDnd by remember { mutableStateOf(existing?.bypassDnd ?: false) }

    val safeMax = maxStrengthLevel.coerceAtLeast(1)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appItem.appName,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = appItem.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Enable Alert for this app
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LED Flash Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Trigger flash when this app notifies",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberPrimary,
                        checkedTrackColor = DarkSurface,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Waveform Pattern Selector
            Text(
                text = "PULSE WAVEFORM",
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
                PatternType.entries.forEach { pattern ->
                    val isSelected = pattern == selectedPattern
                    val bg = if (isSelected) AmberPrimary else DarkSurfaceVariant
                    val textCol = if (isSelected) DarkBackground else TextPrimary

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .border(1.dp, if (isSelected) AmberPrimary else DarkBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedPattern = pattern }
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

            Spacer(modifier = Modifier.height(20.dp))

            // Brightness Intensity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BRIGHTNESS INTENSITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Level ${strengthLevel.roundToInt()} of $safeMax",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = strengthLevel,
                onValueChange = { strengthLevel = it },
                valueRange = 1f..safeMax.toFloat(),
                steps = if (safeMax > 1) safeMax - 2 else 0,
                colors = SliderDefaults.colors(
                    thumbColor = AmberPrimary,
                    activeTrackColor = AmberPrimary,
                    inactiveTrackColor = DarkSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bypass DND Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
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
                    onCheckedChange = { bypassDnd = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberPrimary,
                        checkedTrackColor = DarkSurface,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Test Pattern Action
            OutlinedButton(
                onClick = {
                    val pattern = FlashPattern.defaultFor(selectedPattern).copy(repeatCount = repeatCount)
                    onTestPattern(pattern, strengthLevel.roundToInt())
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = AmberPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test This App Profile", color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Save and Reset Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (existing != null) {
                    OutlinedButton(
                        onClick = { onReset(appItem.packageName) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentError),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = {
                        val config = AppFlashConfig(
                            packageName = appItem.packageName,
                            appName = appItem.appName,
                            isEnabled = isEnabled,
                            patternType = selectedPattern,
                            strengthLevel = strengthLevel.roundToInt(),
                            repeatCount = repeatCount,
                            bypassDnd = bypassDnd
                        )
                        onSave(config)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberPrimary,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(if (existing != null) 0.6f else 1f)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save App Rule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
