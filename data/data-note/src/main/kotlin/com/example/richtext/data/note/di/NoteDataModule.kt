package com.example.richtext.data.note.di

import android.content.Context
import androidx.room.Room
import com.example.richtext.core.database.RichNoteDatabase
import com.example.richtext.core.database.dao.NoteAssetDao
import com.example.richtext.core.database.dao.NoteDao
import com.example.richtext.data.note.repository.NoteRepository
import com.example.richtext.data.note.repository.RoomNoteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NoteDatabaseModule {

    @Provides
    @Singleton
    fun provideRichNoteDatabase(@ApplicationContext context: Context): RichNoteDatabase {
        return Room.databaseBuilder(
            context,
            RichNoteDatabase::class.java,
            "rich_note.db",
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: RichNoteDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideNoteAssetDao(database: RichNoteDatabase): NoteAssetDao = database.noteAssetDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: RoomNoteRepository): NoteRepository
}
