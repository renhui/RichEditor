package com.example.richtext.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.richtext.core.database.model.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query(
        """
        SELECT * FROM notes
        WHERE deleted_at IS NULL
        ORDER BY updated_at DESC
        """,
    )
    fun observeActiveByUpdated(): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT * FROM notes
        WHERE deleted_at IS NULL
        ORDER BY pinned DESC, updated_at DESC
        """,
    )
    fun observeActiveByPinnedThenUpdated(): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT n.* FROM notes n
        JOIN notes_fts f ON n.rowid = f.rowid
        WHERE n.deleted_at IS NULL
          AND f.notes_fts MATCH :matchQuery
        ORDER BY n.updated_at DESC
        """,
    )
    fun searchActiveByUpdated(matchQuery: String): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT n.* FROM notes n
        JOIN notes_fts f ON n.rowid = f.rowid
        WHERE n.deleted_at IS NULL
          AND f.notes_fts MATCH :matchQuery
        ORDER BY n.pinned DESC, n.updated_at DESC
        """,
    )
    fun searchActiveByPinnedThenUpdated(matchQuery: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<NoteEntity?>

    @Query(
        """
        SELECT * FROM notes
        WHERE deleted_at IS NOT NULL
        ORDER BY deleted_at DESC
        """,
    )
    fun observeDeleted(): Flow<List<NoteEntity>>

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("UPDATE notes SET deleted_at = :deletedAt, updated_at = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, deletedAt: Long, updatedAt: Long)

    @Query("UPDATE notes SET deleted_at = NULL, updated_at = :updatedAt WHERE id = :id")
    suspend fun restore(id: String, updatedAt: Long)

    @Query("UPDATE notes SET pinned = :pinned, updated_at = :updatedAt WHERE id = :id")
    suspend fun updatePinned(id: String, pinned: Boolean, updatedAt: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM notes WHERE deleted_at IS NULL")
    suspend fun countActiveNotes(): Int
}
