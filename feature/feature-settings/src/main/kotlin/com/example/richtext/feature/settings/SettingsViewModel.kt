package com.example.richtext.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.richtext.core.datastore.SettingsRepository
import com.example.richtext.core.datastore.ThemeMode
import com.example.richtext.feature.settings.model.SettingsUiState
import com.example.richtext.data.note.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val noteRepository: NoteRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.settingsFlow
        .map { settings ->
            SettingsUiState(
                themeMode = settings.themeMode,
                fontScale = settings.fontScale,
                autoSaveEnabled = settings.autoSaveEnabled,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { settingsRepository.setFontScale(scale) }
    }

    fun setAutoSave(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoSaveEnabled(enabled) }
    }

    fun exportPreview(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = noteRepository.exportAllAsMarkdown().take(400)
            onResult(result)
        }
    }

    fun importSample(onDone: (String) -> Unit) {
        viewModelScope.launch {
            val id = noteRepository.importFromMarkdown(
                markdownText = "# 导入样例\n\n这是 D6 的导入占位内容。",
                title = "导入样例",
            )
            onDone(id)
        }
    }
}
