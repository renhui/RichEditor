package com.example.richtext.feature.editor.model

data class EditorUiState(
    val noteId: String = "",
    val createdAt: Long = 0L,
    val title: String = "",
    val markdown: String = "",
    val previewMarkdown: String = "",
    val mode: EditorMode = EditorMode.EDIT,
    val saveState: SaveState = SaveState.IDLE,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isLoading: Boolean = true,
)
