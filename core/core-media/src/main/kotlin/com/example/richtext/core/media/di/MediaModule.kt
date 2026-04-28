package com.example.richtext.core.media.di

import com.example.richtext.core.media.AndroidMediaImporter
import com.example.richtext.core.media.MediaImporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {

    @Binds
    @Singleton
    abstract fun bindMediaImporter(impl: AndroidMediaImporter): MediaImporter
}
