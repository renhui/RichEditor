package com.example.richtext.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = NoteEntity::class)
@Entity(tableName = "notes_fts")
data class NoteFtsEntity(
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "markdown")
    val markdown: String,
)
