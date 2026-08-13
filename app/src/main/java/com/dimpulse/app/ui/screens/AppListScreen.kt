package com.dimpulse.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimpulse.app.data.model.FlashPattern
import com.dimpulse.app.ui.components.AppConfigItem
import com.dimpulse.app.ui.theme.AmberPrimary
import com.dimpulse.app.ui.theme.DarkBackground
import com.dimpulse.app.ui.theme.DarkBorder
import com.dimpulse.app.ui.theme.DarkSurface
import com.dimpulse.app.ui.theme.DarkSurfaceVariant
import com.dimpulse.app.ui.theme.TextMuted
import com.dimpulse.app.ui.theme.TextPrimary
import com.dimpulse.app.ui.theme.TextSecondary
import com.dimpulse.app.ui.viewmodel.AppListViewModel
import com.dimpulse.app.ui.viewmodel.MainViewModel

@Composable
fun AppListScreen(
    appListViewModel: AppListViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by appListViewModel.searchQuery.collectAsState()
    val isLoading by appListViewModel.isLoading.collectAsState()
    val filteredApps by appListViewModel.filteredApps.collectAsState()
    val selectedAppForEdit by appListViewModel.selectedAppForEdit.collectAsState()
    val globalSettings by mainViewModel.globalSettings.collectAsState()
    val hardwareInfo = mainViewModel.hardwareInfo

    var showConfiguredOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (filteredApps.isEmpty()) {
            appListViewModel.loadInstalledApps(context)
        }
    }

    val displayApps = remember(filteredApps, showConfiguredOnly) {
        if (showConfiguredOnly) {
            filteredApps.filter { it.config != null }
        } else {
            filteredApps
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { appListViewModel.setSearchQuery(it) },
            placeholder = { Text("Search installed apps...", color = TextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { appListViewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = AmberPrimary,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterTabChip(
                title = "All Apps (${filteredApps.size})",
                isSelected = !showConfiguredOnly,
                onClick = { showConfiguredOnly = false }
            )
            FilterTabChip(
                title = "Custom Rules (${filteredApps.count { it.config != null }})",
                isSelected = showConfiguredOnly,
                onClick = { showConfiguredOnly = true }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App List / Loading
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AmberPrimary)
            }
        } else if (displayApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (showConfiguredOnly) "No custom app rules yet" else "No applications found",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = if (showConfiguredOnly) "Tap any app to assign a custom pattern" else "Try adjusting your search query",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = displayApps,
                    key = { it.packageName }
                ) { appItem ->
                    AppConfigItem(
                        appItem = appItem,
                        defaultPattern = globalSettings.defaultPattern,
                        defaultStrength = globalSettings.defaultStrength,
                        onClick = { appListViewModel.selectAppForEdit(appItem) },
                        onToggle = { enabled -> appListViewModel.toggleAppEnabled(appItem, enabled) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Modal Editor Bottom Sheet
    selectedAppForEdit?.let { app ->
        PatternEditorSheet(
            appItem = app,
            maxStrengthLevel = hardwareInfo.maxStrengthLevel,
            defaultPattern = globalSettings.defaultPattern,
            defaultStrength = globalSettings.defaultStrength,
            onDismiss = { appListViewModel.selectAppForEdit(null) },
            onSave = { config -> appListViewModel.saveConfig(config) },
            onReset = { pkg -> appListViewModel.resetAppConfig(pkg) },
            onTestPattern = { pattern, strength ->
                mainViewModel.triggerTestPulse(pattern, strength)
            }
        )
    }
}

@Composable
private fun FilterTabChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) AmberPrimary else DarkSurfaceVariant
    val textCol = if (isSelected) DarkBackground else TextSecondary
    val border = if (isSelected) AmberPrimary else DarkBorder

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textCol
        )
    }
}
