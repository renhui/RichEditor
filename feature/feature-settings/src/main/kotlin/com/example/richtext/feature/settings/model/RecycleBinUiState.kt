package com.example.richtext.feature.settings.model

import com.example.richtext.data.note.model.NoteSummary

data class RecycleBinUiState(
    val notes: List<NoteSummary> = emptyList(),
    val isLoading: Boolean = true,
)
