package com.example.richtext.core.media

data class ImportedMedia(
    val relativePath: String,
    val absolutePath: String,
    val mimeType: String,
    val sizeBytes: Long,
    val widthPx: Int,
    val heightPx: Int,
)

interface MediaImporter {
    suspend fun importImage(noteId: String, sourceUriString: String): ImportedMedia
}
