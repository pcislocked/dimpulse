package com.dimpulse.app.ui.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimpulse.app.DimPulseApp
import com.dimpulse.app.data.model.AppFlashConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val hasLauncherIntent: Boolean = false,
    val config: AppFlashConfig?
)

enum class AppFilterType(val title: String) {
    USER_APPS("User Apps"),
    CONFIGURED("Custom Rules"),
    ALL_APPS("All Apps (System)")
}

class AppListViewModel : ViewModel() {

    private val repository = DimPulseApp.instance.repository

    private val _rawInstalledApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow(AppFilterType.USER_APPS)
    val filterType: StateFlow<AppFilterType> = _filterType.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedAppForEdit = MutableStateFlow<InstalledAppItem?>(null)
    val selectedAppForEdit: StateFlow<InstalledAppItem?> = _selectedAppForEdit.asStateFlow()

    val filteredApps: StateFlow<List<InstalledAppItem>> = combine(
        _rawInstalledApps,
        repository.appConfigs,
        _searchQuery,
        _filterType
    ) { apps, configsMap, query, filter ->
        val updatedList = apps.map { app ->
            app.copy(config = configsMap[app.packageName])
        }

        val typeFiltered = when (filter) {
            AppFilterType.USER_APPS -> updatedList.filter { !it.isSystemApp || it.hasLauncherIntent || it.config != null }
            AppFilterType.CONFIGURED -> updatedList.filter { it.config != null }
            AppFilterType.ALL_APPS -> updatedList
        }

        val searched = if (query.isBlank()) {
            typeFiltered
        } else {
            typeFiltered.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }

        // Sort: Configured apps first, then alphabetically
        searched.sortedWith(
            compareByDescending<InstalledAppItem> { it.config != null }
                .thenBy { it.appName.lowercase() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

                packages.mapNotNull { appInfo ->
                    // Exclude DimPulse itself
                    if (appInfo.packageName == context.packageName) return@mapNotNull null

                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val hasLauncher = pm.getLaunchIntentForPackage(appInfo.packageName) != null
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    val icon = try {
                        pm.getApplicationIcon(appInfo)
                    } catch (e: Exception) {
                        null
                    }

                    InstalledAppItem(
                        packageName = appInfo.packageName,
                        appName = appName,
                        icon = icon,
                        isSystemApp = isSystem,
                        hasLauncherIntent = hasLauncher,
                        config = null
                    )
                }
            }
            _rawInstalledApps.value = apps
            _isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(filter: AppFilterType) {
        _filterType.value = filter
    }

    fun selectAppForEdit(app: InstalledAppItem?) {
        _selectedAppForEdit.value = app
    }

    fun toggleAppEnabled(app: InstalledAppItem, isEnabled: Boolean) {
        viewModelScope.launch {
            val existing = repository.getConfigForPackage(app.packageName)
            if (existing != null) {
                repository.saveAppConfig(existing.copy(isEnabled = isEnabled))
            } else {
                val global = repository.globalSettings.value
                val newConfig = AppFlashConfig(
                    packageName = app.packageName,
                    appName = app.appName,
                    isEnabled = isEnabled,
                    flashStyle = global.defaultFlashStyle,
                    repeatCount = global.defaultRepeatCount,
                    strengthLevel = global.defaultStrength,
                    breathingDurationMs = global.breathingDurationMs
                )
                repository.saveAppConfig(newConfig)
            }
        }
    }

    fun saveConfig(config: AppFlashConfig) {
        viewModelScope.launch {
            repository.saveAppConfig(config)
            _selectedAppForEdit.value = null
        }
    }

    fun resetAppConfig(packageName: String) {
        viewModelScope.launch {
            repository.removeAppConfig(packageName)
            _selectedAppForEdit.value = null
        }
    }
}
