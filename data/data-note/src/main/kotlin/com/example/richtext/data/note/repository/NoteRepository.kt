package com.example.richtext.data.note.repository

import com.example.richtext.core.database.model.NoteEntity
import com.example.richtext.data.note.model.NoteSort
import com.example.richtext.data.note.model.NoteSummary
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeAll(query: Flow<String>, sort: Flow<NoteSort>): Flow<List<NoteSummary>>
    fun observeDeleted(): Flow<List<NoteSummary>>
    fun observeById(id: String): Flow<NoteEntity?>
    suspend fun upsert(note: NoteEntity)
    suspend fun softDelete(id: String)
    suspend fun restore(id: String)
    suspend fun setPinned(id: String, pinned: Boolean)
    suspend fun ensureSeedData()
    suspend fun attachImage(noteId: String, sourceUriString: String, altText: String = "image"): String
    suspend fun resolveAssetAbsolutePath(assetId: String): String?
    suspend fun exportAllAsMarkdown(): String
    suspend fun importFromMarkdown(markdownText: String, title: String = "导入便签"): String
    suspend fun purge(id: String)
}
