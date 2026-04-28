package com.example.richtext.feature.detail.model

data class NoteDetailUiState(
    val noteId: String = "",
    val title: String = "详情",
    val markdown: String = "",
    val isLoading: Boolean = true,
)
