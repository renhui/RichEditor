package com.example.richtext.core.datastore

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f,
    val autoSaveEnabled: Boolean = true,
)
