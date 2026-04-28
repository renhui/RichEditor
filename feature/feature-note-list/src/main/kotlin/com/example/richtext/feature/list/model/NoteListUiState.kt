package com.example.richtext.feature.list.model

import com.example.richtext.data.note.model.NoteSort
import com.example.richtext.data.note.model.NoteSummary

data class NoteListUiState(
    val query: String = "",
    val sort: NoteSort = NoteSort.PINNED_THEN_UPDATED,
    val notes: List<NoteSummary> = emptyList(),
    val isLoading: Boolean = true,
)
