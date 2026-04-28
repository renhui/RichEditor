package com.example.richtext.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.richtext.core.database.model.NoteAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteAssetDao {

    @Query("SELECT * FROM note_assets WHERE note_id = :noteId ORDER BY created_at DESC")
    fun observeAssetsByNoteId(noteId: String): Flow<List<NoteAssetEntity>>

    @Query("SELECT * FROM note_assets WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NoteAssetEntity?

    @Upsert
    suspend fun upsert(asset: NoteAssetEntity)

    @Query("DELETE FROM note_assets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM note_assets WHERE note_id = :noteId")
    suspend fun deleteByNoteId(noteId: String)
}
