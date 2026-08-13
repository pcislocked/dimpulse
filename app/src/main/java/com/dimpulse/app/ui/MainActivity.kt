package com.dimpulse.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.dimpulse.app.ui.screens.AppListScreen
import com.dimpulse.app.ui.screens.DashboardScreen
import com.dimpulse.app.ui.screens.SettingsScreen
import com.dimpulse.app.ui.theme.AmberPrimary
import com.dimpulse.app.ui.theme.DarkBackground
import com.dimpulse.app.ui.theme.DarkBorder
import com.dimpulse.app.ui.theme.DarkSurface
import com.dimpulse.app.ui.theme.DarkSurfaceVariant
import com.dimpulse.app.ui.theme.DimPulseTheme
import com.dimpulse.app.ui.theme.TextMuted
import com.dimpulse.app.ui.theme.TextPrimary
import com.dimpulse.app.ui.viewmodel.AppListViewModel
import com.dimpulse.app.ui.viewmodel.MainViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    APPS("App Rules", Icons.Default.FormatListBulleted),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val appListViewModel: AppListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DimPulseTheme {
                MainRootScreen(
                    mainViewModel = mainViewModel,
                    appListViewModel = appListViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshPermissions(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRootScreen(
    mainViewModel: MainViewModel,
    appListViewModel: AppListViewModel
) {
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentTab.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = androidx.compose.ui.unit.dp
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> DashboardScreen(
                    mainViewModel = mainViewModel,
                    onNavigateToApps = { currentTab = NavigationTab.APPS },
                    onNavigateToSettings = { currentTab = NavigationTab.SETTINGS }
                )
                NavigationTab.APPS -> AppListScreen(
                    appListViewModel = appListViewModel,
                    mainViewModel = mainViewModel
                )
                NavigationTab.SETTINGS -> SettingsScreen(
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}
