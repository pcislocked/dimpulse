package com.dimpulse.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.dimpulse.app.data.model.GlobalFlashSettings
import com.dimpulse.app.ui.components.LedConfigurationEditor
import com.dimpulse.app.ui.theme.AccentError
import com.dimpulse.app.ui.theme.AmberPrimary
import com.dimpulse.app.ui.theme.DarkSurface
import com.dimpulse.app.ui.theme.DarkSurfaceVariant
import com.dimpulse.app.ui.theme.TextMuted
import com.dimpulse.app.ui.theme.TextPrimary
import com.dimpulse.app.ui.theme.TextSecondary
import com.dimpulse.app.ui.viewmodel.InstalledAppItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternEditorSheet(
    appItem: InstalledAppItem,
    globalSettings: GlobalFlashSettings,
    maxStrengthLevel: Int,
    isTesting: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (AppFlashConfig) -> Unit,
    onReset: (String) -> Unit,
    onTestPattern: (FlashPattern, Int) -> Unit,
    onStopTestPattern: () -> Unit = {}
) {
    val existing = appItem.config
    var isEnabled by remember { mutableStateOf(existing?.isEnabled ?: true) }
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
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = appItem.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enable App Toggle
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

            Spacer(modifier = Modifier.height(16.dp))

            // Reset to Global Defaults button (if custom config exists)
            if (existing != null) {
                OutlinedButton(
                    onClick = { onReset(appItem.packageName) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentError),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Reset to Global Settings Defaults", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Shared Unified LED Configuration Editor
            LedConfigurationEditor(
                initialPreset = existing?.profilePreset ?: globalSettings.defaultProfilePreset,
                initialRepeatCount = existing?.repeatCount ?: globalSettings.defaultRepeatCount,
                initialStrength = existing?.strengthLevel ?: globalSettings.defaultStrength,
                maxStrengthLevel = maxStrengthLevel,
                initialFadeInMs = existing?.fadeInMs ?: globalSettings.defaultFadeInMs,
                initialStayOnMs = existing?.stayOnMs ?: globalSettings.defaultStayOnMs,
                initialFadeOutMs = existing?.fadeOutMs ?: globalSettings.defaultFadeOutMs,
                initialGapMs = existing?.gapMs ?: globalSettings.defaultGapMs,
                initialCooldownSeconds = existing?.cooldownSeconds ?: globalSettings.cooldownSeconds,
                initialImportanceFilter = existing?.alertImportanceFilter ?: globalSettings.alertImportanceFilter,
                initialBypassDnd = existing?.bypassDnd ?: false,
                showDndBypassToggle = true,
                showImportanceFilter = true,
                isTesting = isTesting,
                onTestClick = onTestPattern,
                onStopTestClick = onStopTestPattern,
                saveButtonText = "Save App Rule",
                onSaveClick = { preset, repeatCount, strength, fadeInMs, stayOnMs, fadeOutMs, gapMs, cooldown, filter, bypass ->
                    val newConfig = AppFlashConfig(
                        packageName = appItem.packageName,
                        appName = appItem.appName,
                        isEnabled = isEnabled,
                        profilePreset = preset,
                        repeatCount = repeatCount,
                        strengthLevel = strength,
                        fadeInMs = fadeInMs,
                        stayOnMs = stayOnMs,
                        fadeOutMs = fadeOutMs,
                        gapMs = gapMs,
                        cooldownSeconds = cooldown,
                        alertImportanceFilter = filter,
                        bypassDnd = bypass
                    )
                    onSave(newConfig)
                }
            )
        }
    }
}
