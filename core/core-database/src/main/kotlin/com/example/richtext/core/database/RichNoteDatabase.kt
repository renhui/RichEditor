package com.example.richtext.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.richtext.core.database.dao.NoteAssetDao
import com.example.richtext.core.database.dao.NoteDao
import com.example.richtext.core.database.model.NoteAssetEntity
import com.example.richtext.core.database.model.NoteEntity
import com.example.richtext.core.database.model.NoteFtsEntity

@Database(
    entities = [
        NoteEntity::class,
        NoteAssetEntity::class,
        NoteFtsEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class RichNoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun noteAssetDao(): NoteAssetDao
}
