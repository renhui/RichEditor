package com.example.richtext.core.markdown.di

import android.content.Context
import com.example.richtext.core.markdown.MarkdownRenderer
import com.example.richtext.core.markdown.MarkwonMarkdownRenderer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MarkdownProviderModule {

    @Provides
    @Singleton
    fun provideMarkwon(@ApplicationContext context: Context): Markwon {
        return Markwon.builder(context)
            .usePlugin(ImagesPlugin.create())
            .usePlugin(GlideImagesPlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(TablePlugin.create(context))
            .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configureTheme(builder: MarkwonTheme.Builder) {
                        builder.blockMargin(8)
                    }
                },
            )
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MarkdownBindModule {
    @Binds
    @Singleton
    abstract fun bindMarkdownRenderer(impl: MarkwonMarkdownRenderer): MarkdownRenderer
}
