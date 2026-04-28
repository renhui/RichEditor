package com.example.richtext.feature.settings.model

import com.example.richtext.core.datastore.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f,
    val autoSaveEnabled: Boolean = true,
    val exportPreview: String = "",
)
