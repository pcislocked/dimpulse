package com.dimpulse.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dimpulse.app.data.model.AppFlashConfig
import com.dimpulse.app.data.model.GlobalFlashSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dimpulse_preferences")

class FlashConfigRepository(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val KEY_GLOBAL_SETTINGS = stringPreferencesKey("global_flash_settings")
    private val KEY_APP_CONFIGS = stringPreferencesKey("app_flash_configs_map")

    val globalSettings: StateFlow<GlobalFlashSettings> = context.dataStore.data
        .map { preferences ->
            val jsonStr = preferences[KEY_GLOBAL_SETTINGS]
            if (jsonStr != null) {
                try {
                    json.decodeFromString<GlobalFlashSettings>(jsonStr)
                } catch (e: Exception) {
                    GlobalFlashSettings()
                }
            } else {
                GlobalFlashSettings()
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = GlobalFlashSettings()
        )

    val appConfigs: StateFlow<Map<String, AppFlashConfig>> = context.dataStore.data
        .map { preferences ->
            val jsonStr = preferences[KEY_APP_CONFIGS]
            if (jsonStr != null) {
                try {
                    json.decodeFromString<Map<String, AppFlashConfig>>(jsonStr)
                } catch (e: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )

    suspend fun updateGlobalSettings(transform: (GlobalFlashSettings) -> GlobalFlashSettings) {
        context.dataStore.edit { preferences ->
            val current = globalSettings.value
            val updated = transform(current)
            preferences[KEY_GLOBAL_SETTINGS] = json.encodeToString(updated)
        }
    }

    suspend fun saveAppConfig(config: AppFlashConfig) {
        context.dataStore.edit { preferences ->
            val currentMap = appConfigs.value.toMutableMap()
            currentMap[config.packageName] = config
            preferences[KEY_APP_CONFIGS] = json.encodeToString(currentMap)
        }
    }

    suspend fun removeAppConfig(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentMap = appConfigs.value.toMutableMap()
            currentMap.remove(packageName)
            preferences[KEY_APP_CONFIGS] = json.encodeToString(currentMap)
        }
    }

    fun getConfigForPackage(packageName: String): AppFlashConfig? {
        return appConfigs.value[packageName]
    }
}
