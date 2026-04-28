package com.example.richtext.data.note.model

data class NoteSummary(
    val id: String,
    val title: String,
    val preview: String,
    val updatedAt: Long,
    val pinned: Boolean,
)
