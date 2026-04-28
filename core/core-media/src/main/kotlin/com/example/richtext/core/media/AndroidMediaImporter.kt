package com.example.richtext.core.media

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AndroidMediaImporter @Inject constructor(
    @ApplicationContext private val context: Context,
) : MediaImporter {

    override suspend fun importImage(noteId: String, sourceUriString: String): ImportedMedia {
        return withContext(Dispatchers.IO) {
            val sourceUri = Uri.parse(sourceUriString)
            val resolver = context.contentResolver
            val ext = resolver.getType(sourceUri)?.substringAfter('/')?.ifBlank { "jpg" } ?: "jpg"
            val mimeType = resolver.getType(sourceUri) ?: "image/*"

            val targetDir = File(context.filesDir, "notes/$noteId/img")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val fileName = "${UUID.randomUUID()}.$ext"
            val targetFile = File(targetDir, fileName)

            resolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: error("无法读取图片输入流")

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(targetFile.absolutePath, bounds)

            ImportedMedia(
                relativePath = "notes/$noteId/img/$fileName",
                absolutePath = targetFile.absolutePath,
                mimeType = mimeType,
                sizeBytes = targetFile.length(),
                widthPx = bounds.outWidth.coerceAtLeast(0),
                heightPx = bounds.outHeight.coerceAtLeast(0),
            )
        }
    }
}
