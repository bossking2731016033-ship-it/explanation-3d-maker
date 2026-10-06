package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "explainer_settings")

data class UserSettings(
    val apiKey: String = "",
    val defaultLanguage: String = "English",
    val defaultDuration: Int = 30,
    val defaultVoice: String = "Male",
    val isDarkMode: Boolean = true,
    val resolution: String = "720p"
)

class AppSettingsDataStore(private val context: Context) {

    companion object {
        val KEY_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_DEFAULT_LANG = stringPreferencesKey("default_language")
        val KEY_DEFAULT_DURATION = intPreferencesKey("default_duration")
        val KEY_DEFAULT_VOICE = stringPreferencesKey("default_voice")
        val KEY_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val KEY_RESOLUTION = stringPreferencesKey("resolution")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            apiKey = prefs[KEY_API_KEY] ?: "",
            defaultLanguage = prefs[KEY_DEFAULT_LANG] ?: "English",
            defaultDuration = prefs[KEY_DEFAULT_DURATION] ?: 30,
            defaultVoice = prefs[KEY_DEFAULT_VOICE] ?: "Male",
            isDarkMode = prefs[KEY_DARK_MODE] ?: true,
            resolution = prefs[KEY_RESOLUTION] ?: "720p"
        )
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_API_KEY] = apiKey.trim()
        }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_LANG] = language
        }
    }

    suspend fun saveDuration(duration: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_DURATION] = duration
        }
    }

    suspend fun saveVoice(voice: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_VOICE] = voice
        }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = isDark
        }
    }

    suspend fun saveResolution(res: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RESOLUTION] = res
        }
    }
}
