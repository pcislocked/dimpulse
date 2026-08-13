package com.dimpulse.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimpulse.app.engine.FlashHardwareInfo
import com.dimpulse.app.ui.theme.AccentError
import com.dimpulse.app.ui.theme.AccentSuccess
import com.dimpulse.app.ui.theme.AccentWarning
import com.dimpulse.app.ui.theme.AmberPrimary
import com.dimpulse.app.ui.theme.DarkBorder
import com.dimpulse.app.ui.theme.DarkSurface
import com.dimpulse.app.ui.theme.DarkSurfaceVariant
import com.dimpulse.app.ui.theme.TextMuted
import com.dimpulse.app.ui.theme.TextPrimary
import com.dimpulse.app.ui.theme.TextSecondary

@Composable
fun HardwareStatusHUD(
    hardwareInfo: FlashHardwareInfo,
    isNotificationGranted: Boolean,
    isBatteryOptimizationIgnored: Boolean,
    onGrantNotificationClick: () -> Unit,
    onIgnoreBatteryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row: Hardware status title & chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Flash Hardware",
                            tint = AmberPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Hardware Subsystem",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (hardwareInfo.hasFlash) {
                                "Camera ID ${hardwareInfo.cameraId ?: "0"} • ${hardwareInfo.maxStrengthLevel} Level Range"
                            } else {
                                "No Flash Detected"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Capability badge
                val (badgeText, badgeColor) = if (hardwareInfo.supportsGranularDimming) {
                    "Granular Dimming Ready" to AccentSuccess
                } else if (hardwareInfo.hasFlash) {
                    "Binary Strobe Only" to AccentWarning
                } else {
                    "Unsupported" to AccentError
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Permission Status Row Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PermissionChip(
                    title = "Notification Access",
                    isGranted = isNotificationGranted,
                    onClick = onGrantNotificationClick,
                    modifier = Modifier.weight(1f)
                )
                PermissionChip(
                    title = "Battery Unrestricted",
                    isGranted = isBatteryOptimizationIgnored,
                    onClick = onIgnoreBatteryClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PermissionChip(
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerBg = if (isGranted) DarkSurfaceVariant else AccentWarning.copy(alpha = 0.12f)
    val borderColor = if (isGranted) DarkBorder else AccentWarning.copy(alpha = 0.4f)
    val iconColor = if (isGranted) AccentSuccess else AccentWarning

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = if (isGranted) TextSecondary else TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
