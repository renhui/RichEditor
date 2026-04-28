package com.example.richtext.core.datastore

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setFontScale(scale: Float)
    suspend fun setAutoSaveEnabled(enabled: Boolean)
}
