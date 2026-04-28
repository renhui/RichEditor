package com.example.richtext.data.note.repository

import com.example.richtext.core.database.dao.NoteAssetDao
import com.example.richtext.core.database.dao.NoteDao
import com.example.richtext.core.database.model.NoteAssetEntity
import com.example.richtext.core.database.model.NoteEntity
import com.example.richtext.core.media.MediaImporter
import com.example.richtext.data.note.model.NoteSort
import com.example.richtext.data.note.model.NoteSummary
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@Singleton
class RoomNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteAssetDao: NoteAssetDao,
    private val mediaImporter: MediaImporter,
) : NoteRepository {

    override fun observeAll(query: Flow<String>, sort: Flow<NoteSort>): Flow<List<NoteSummary>> {
        return combine(
            query.map { it.trim() }.distinctUntilChanged(),
            sort.distinctUntilChanged(),
        ) { normalizedQuery, selectedSort ->
            normalizedQuery to selectedSort
        }.flatMapLatest { (normalizedQuery, selectedSort) ->
            val upstream = if (normalizedQuery.isEmpty()) {
                when (selectedSort) {
                    NoteSort.UPDATED_DESC -> noteDao.observeActiveByUpdated()
                    NoteSort.PINNED_THEN_UPDATED -> noteDao.observeActiveByPinnedThenUpdated()
                }
            } else {
                val useLikeFallback = NoteSearchQueryFormatter.shouldUseLikeFallback(normalizedQuery)
                if (useLikeFallback) {
                    val likePattern = NoteSearchQueryFormatter.buildLikePattern(normalizedQuery)
                    when (selectedSort) {
                        NoteSort.UPDATED_DESC -> noteDao.searchActiveByUpdatedLike(likePattern)
                        NoteSort.PINNED_THEN_UPDATED -> noteDao.searchActiveByPinnedThenUpdatedLike(likePattern)
                    }
                } else {
                    val match = NoteSearchQueryFormatter.buildFtsMatchQuery(normalizedQuery)
                    when (selectedSort) {
                        NoteSort.UPDATED_DESC -> noteDao.searchActiveByUpdated(match)
                        NoteSort.PINNED_THEN_UPDATED -> noteDao.searchActiveByPinnedThenUpdated(match)
                    }
                }
            }
            upstream.map { list ->
                list.map { note ->
                    note.toSummary()
                }
            }
        }
    }

    override fun observeById(id: String): Flow<NoteEntity?> = noteDao.observeById(id)

    override fun observeDeleted(): Flow<List<NoteSummary>> {
        return noteDao.observeDeleted().map { list -> list.map { it.toSummary() } }
    }

    override suspend fun upsert(note: NoteEntity) {
        noteDao.upsert(note.copy(wordCount = note.markdown.length))
    }

    override suspend fun softDelete(id: String) {
        val now = System.currentTimeMillis()
        noteDao.softDelete(id = id, deletedAt = now, updatedAt = now)
    }

    override suspend fun restore(id: String) {
        noteDao.restore(id = id, updatedAt = System.currentTimeMillis())
    }

    override suspend fun setPinned(id: String, pinned: Boolean) {
        noteDao.updatePinned(id = id, pinned = pinned)
    }

    override suspend fun ensureSeedData() {
        if (noteDao.countActiveNotes() > 0) return

        val now = System.currentTimeMillis()
        val welcome = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = "欢迎使用 RichNote",
            markdown = "# 欢迎\n\n这是 D1 阶段的测试数据。\n\n- 已接入 Room\n- 已接入 Flow 列表订阅\n- 已接入 FTS 搜索",
            createdAt = now,
            updatedAt = now,
            pinned = true,
            wordCount = 47,
        )
        noteDao.upsert(welcome)

        val second = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = "Markdown 示例",
            markdown = "## 任务\n- [ ] 完成 D2 Markdown 渲染\n- [ ] 接入详情页\n\n`code block` 示例",
            createdAt = now,
            updatedAt = now,
            pinned = false,
            wordCount = 52,
        )
        noteDao.upsert(second)
    }

    override suspend fun attachImage(noteId: String, sourceUriString: String, altText: String): String {
        val imported = mediaImporter.importImage(noteId = noteId, sourceUriString = sourceUriString)
        val assetId = UUID.randomUUID().toString()
        noteAssetDao.upsert(
            NoteAssetEntity(
                id = assetId,
                noteId = noteId,
                relativePath = imported.absolutePath,
                mimeType = imported.mimeType,
                sizeBytes = imported.sizeBytes,
                widthPx = imported.widthPx,
                heightPx = imported.heightPx,
                createdAt = System.currentTimeMillis(),
            ),
        )
        return "![${altText.ifBlank { "image" }}](note://asset/$assetId)"
    }

    override suspend fun resolveAssetAbsolutePath(assetId: String): String? {
        val asset = noteAssetDao.getById(assetId) ?: return null
        return asset.relativePath
    }

    override suspend fun exportAllAsMarkdown(): String {
        val all = noteDao.observeActiveByPinnedThenUpdated()
        // D6 最小可用实现：取一次当前快照文本（真实 zip/文件导出后续增强）
        val snapshot = all.map { notes ->
            notes.joinToString("\n\n---\n\n") { note ->
                "# ${note.title}\n\n${note.markdown}"
            }
        }
        return snapshot.firstOrNull().orEmpty()
    }

    override suspend fun importFromMarkdown(markdownText: String, title: String): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        noteDao.upsert(
            NoteEntity(
                id = id,
                title = title,
                markdown = markdownText,
                createdAt = now,
                updatedAt = now,
                pinned = false,
                deletedAt = null,
                coverImagePath = null,
                wordCount = markdownText.length,
            ),
        )
        return id
    }

    override suspend fun purge(id: String) {
        noteAssetDao.deleteByNoteId(id)
        noteDao.deleteById(id)
    }

    private fun NoteEntity.toSummary(): NoteSummary {
        val previewText = markdown
            .replace(Regex("[#>*_`\\[\\]()!-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(120)

        return NoteSummary(
            id = id,
            title = title,
            preview = previewText,
            updatedAt = updatedAt,
            pinned = pinned,
        )
    }
}
