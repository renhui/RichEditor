package com.example.richtext.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.richtext.data.note.repository.NoteRepository
import com.example.richtext.feature.detail.model.NoteDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    val uiState: StateFlow<NoteDetailUiState> = noteRepository.observeById(noteId)
        .map { note ->
            if (note == null) {
                NoteDetailUiState(
                    noteId = noteId,
                    title = "笔记不存在",
                    markdown = "未找到对应内容。",
                    isLoading = false,
                )
            } else {
                NoteDetailUiState(
                    noteId = note.id,
                    title = note.title.ifBlank { "无标题" },
                    markdown = replaceAssetUris(note.markdown),
                    isLoading = false,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NoteDetailUiState(noteId = noteId),
        )

    private suspend fun replaceAssetUris(markdown: String): String {
        val regex = Regex("""!\[([^]]*)]\(note://asset/([^)]+)\)""")
        var rendered = markdown
        regex.findAll(markdown).forEach { match ->
            val alt = match.groupValues[1]
            val assetId = match.groupValues[2]
            val path = noteRepository.resolveAssetAbsolutePath(assetId) ?: return@forEach
            rendered = rendered.replace(match.value, "![${alt}](file://${path})")
        }
        return rendered
    }
}
