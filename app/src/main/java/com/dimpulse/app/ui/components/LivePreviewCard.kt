package com.dimpulse.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.data.model.GlobalFlashSettings
import com.dimpulse.app.data.model.LightProfilePreset
import com.dimpulse.app.ui.theme.AmberPrimary
import com.dimpulse.app.ui.theme.AmberSecondary
import com.dimpulse.app.ui.theme.DarkBackground
import com.dimpulse.app.ui.theme.DarkBorder
import com.dimpulse.app.ui.theme.DarkSurface
import com.dimpulse.app.ui.theme.TextPrimary
import com.dimpulse.app.ui.theme.TextSecondary

@Composable
fun LivePreviewCard(
    globalSettings: GlobalFlashSettings,
    maxStrengthLevel: Int,
    isTesting: Boolean,
    onTestClick: (FlashPattern, Int) -> Unit,
    onStopClick: () -> Unit,
    onApplyAsDefault: (
        preset: LightProfilePreset,
        repeatCount: Int,
        strength: Int,
        fadeInMs: Long,
        stayOnMs: Long,
        fadeOutMs: Long,
        gapMs: Long,
        cooldownSeconds: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isTesting) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lighting Lab & Playground",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Craft light shapes & save as global default",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Animated glowing LED orb
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(if (isTesting) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            if (isTesting) AmberPrimary else AmberPrimary.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (isTesting) AmberSecondary else AmberPrimary.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = if (isTesting) DarkBackground else AmberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shared Unified LED Configuration Editor
            LedConfigurationEditor(
                initialPreset = globalSettings.defaultProfilePreset,
                initialRepeatCount = globalSettings.defaultRepeatCount,
                initialStrength = globalSettings.defaultStrength,
                maxStrengthLevel = maxStrengthLevel,
                initialFadeInMs = globalSettings.defaultFadeInMs,
                initialStayOnMs = globalSettings.defaultStayOnMs,
                initialFadeOutMs = globalSettings.defaultFadeOutMs,
                initialGapMs = globalSettings.defaultGapMs,
                initialCooldownSeconds = globalSettings.cooldownSeconds,
                showDndBypassToggle = false,
                showImportanceFilter = false,
                isTesting = isTesting,
                onTestClick = onTestClick,
                onStopTestClick = onStopClick,
                saveButtonText = "Save as Default",
                onSaveClick = { preset, repeatCount, strength, fadeInMs, stayOnMs, fadeOutMs, gapMs, cooldown, _, _ ->
                    onApplyAsDefault(
                        preset,
                        repeatCount,
                        strength,
                        fadeInMs,
                        stayOnMs,
                        fadeOutMs,
                        gapMs,
                        cooldown
                    )
                }
            )
        }
    }
}
