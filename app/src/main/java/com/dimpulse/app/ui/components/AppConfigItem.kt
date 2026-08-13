package com.dimpulse.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.dimpulse.app.data.model.LightProfilePreset
import com.dimpulse.app.ui.theme.AmberPrimary
import com.dimpulse.app.ui.theme.DarkBorder
import com.dimpulse.app.ui.theme.DarkSurface
import com.dimpulse.app.ui.theme.DarkSurfaceVariant
import com.dimpulse.app.ui.theme.TextMuted
import com.dimpulse.app.ui.theme.TextPrimary
import com.dimpulse.app.ui.theme.TextSecondary
import com.dimpulse.app.ui.viewmodel.InstalledAppItem

@Composable
fun AppConfigItem(
    appItem: InstalledAppItem,
    defaultPreset: LightProfilePreset,
    defaultRepeatCount: Int,
    defaultStrength: Int,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = appItem.config
    val isEnabled = config?.isEnabled ?: false
    val preset = config?.profilePreset ?: defaultPreset
    val repeats = config?.repeatCount ?: defaultRepeatCount
    val strength = config?.strengthLevel ?: defaultStrength
    val hasCustomConfig = config != null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, if (hasCustomConfig) AmberPrimary.copy(alpha = 0.3f) else DarkBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // App Icon
                if (appItem.icon != null) {
                    val bitmap = try {
                        appItem.icon.toBitmap(96, 96).asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = appItem.appName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        DefaultAppIcon()
                    }
                } else {
                    DefaultAppIcon()
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Titles & Badges
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appItem.appName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "${preset.title} • ${repeats}x • Lvl $strength",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hasCustomConfig) AmberPrimary else TextSecondary,
                            fontSize = 12.sp
                        )

                        if (config?.bypassDnd == true) {
                            Text(
                                text = " • DND Bypass",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AmberPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Active Toggle
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AmberPrimary,
                    checkedTrackColor = DarkSurfaceVariant,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun DefaultAppIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Android,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}
