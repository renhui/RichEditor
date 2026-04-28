package com.example.richtext.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.richtext.core.database.model.NoteEntity
import com.example.richtext.data.note.repository.NoteRepository
import com.example.richtext.feature.editor.model.EditorAction
import com.example.richtext.feature.editor.model.EditorMode
import com.example.richtext.feature.editor.model.EditorUiState
import com.example.richtext.feature.editor.model.SaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(FlowPreview::class)
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private data class EditorDraft(
        val title: String,
        val markdown: String,
    )

    private val historyLimit = 200
    private val undoStack = ArrayDeque<EditorDraft>()
    private val redoStack = ArrayDeque<EditorDraft>()

    private val routeNoteId: String = savedStateHandle["noteId"] ?: "new"
    private val actualNoteId: String = if (routeNoteId == "new") UUID.randomUUID().toString() else routeNoteId
    private val isNewNoteRoute: Boolean = routeNoteId == "new"

    private val _uiState = MutableStateFlow(EditorUiState(noteId = actualNoteId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val draftFlow = MutableStateFlow(EditorDraft(title = "", markdown = ""))
    private var isHydrating = true
    private var saveJob: Job? = null
    private var persistedDraft = EditorDraft(title = "", markdown = "")
    private var currentPinned = false

    init {
        observeExistingNote()
        observeAutoSave()
    }

    private fun observeExistingNote() {
        if (isNewNoteRoute) {
            isHydrating = false
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            noteRepository.observeById(actualNoteId).collectLatest { note ->
                if (note != null) {
                    currentPinned = note.pinned
                }
                if (note != null && isHydrating) {
                    val title = note.title
                    val markdown = note.markdown
                    val draft = EditorDraft(title = title, markdown = markdown)
                    draftFlow.value = draft
                    persistedDraft = draft
                    _uiState.update {
                        it.copy(
                            createdAt = note.createdAt,
                            title = title,
                            markdown = markdown,
                            previewMarkdown = markdown,
                            isLoading = false,
                            saveState = SaveState.IDLE,
                        )
                    }
                    refreshPreviewMarkdown(markdown)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
                isHydrating = false
            }
        }
    }

    private fun observeAutoSave() {
        saveJob = viewModelScope.launch {
            draftFlow.debounce(500).collectLatest { draft ->
                if (isHydrating) return@collectLatest
                if (!shouldSaveDraft(draft)) return@collectLatest
                saveDraft(draft)
            }
        }
    }

    private fun shouldSaveDraft(draft: EditorDraft): Boolean {
        if (draft == persistedDraft) return false
        if (isNewNoteRoute && persistedDraft == EditorDraft("", "") && draft.isEffectivelyEmpty()) {
            return false
        }
        return true
    }

    private fun EditorDraft.isEffectivelyEmpty(): Boolean = title.isBlank() && markdown.isBlank()

    private suspend fun saveDraft(draft: EditorDraft) {
        _uiState.update { it.copy(saveState = SaveState.SAVING) }
        try {
            val now = System.currentTimeMillis()
            val createdAt = _uiState.value.createdAt.takeIf { it > 0 } ?: now
            noteRepository.upsert(
                NoteEntity(
                    id = actualNoteId,
                    title = draft.title.ifBlank { "无标题" },
                    markdown = draft.markdown,
                    createdAt = createdAt,
                    updatedAt = now,
                    pinned = currentPinned,
                    deletedAt = null,
                    coverImagePath = null,
                    wordCount = draft.markdown.length,
                ),
            )
            persistedDraft = draft
            _uiState.update { it.copy(saveState = SaveState.SAVED) }
            delay(600)
            _uiState.update { state ->
                if (state.saveState == SaveState.SAVED) state.copy(saveState = SaveState.IDLE) else state
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(saveState = SaveState.ERROR) }
        }
    }

    fun updateTitle(value: String) {
        pushUndoSnapshot()
        _uiState.update { it.copy(title = value) }
        draftFlow.value = EditorDraft(value, _uiState.value.markdown)
        redoStack.clear()
        refreshHistoryFlags()
    }

    fun updateMarkdown(value: String) {
        pushUndoSnapshot()
        _uiState.update { it.copy(markdown = value) }
        _uiState.update { it.copy(previewMarkdown = value) }
        draftFlow.value = EditorDraft(_uiState.value.title, value)
        refreshPreviewMarkdown(value)
        redoStack.clear()
        refreshHistoryFlags()
    }

    fun switchMode(mode: EditorMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun applyAction(action: EditorAction) {
        val inserted = when (action) {
            EditorAction.HEADER_1 -> "\n# 标题\n"
            EditorAction.BOLD -> "****"
            EditorAction.TODO -> "\n- [ ] "
            EditorAction.CODE_BLOCK -> "\n```\n\n```\n"
            EditorAction.IMAGE -> "![image](note://asset/pending)"
        }
        if (action == EditorAction.IMAGE) {
            updateMarkdown(appendBlock(_uiState.value.markdown, inserted))
        } else {
            updateMarkdown(_uiState.value.markdown + inserted)
        }
    }

    fun insertImageFromUri(uriString: String) {
        viewModelScope.launch {
            runCatching {
                noteRepository.attachImage(
                    noteId = actualNoteId,
                    sourceUriString = uriString,
                    altText = "image",
                )
            }.onSuccess { imageMarkdown ->
                updateMarkdown(appendBlock(_uiState.value.markdown, imageMarkdown))
            }.onFailure {
                _uiState.update { state -> state.copy(saveState = SaveState.ERROR) }
            }
        }
    }

    private fun appendBlock(original: String, block: String): String {
        if (original.isBlank()) return "$block\n"
        val trimmedOriginal = original.trimEnd('\n')
        return "$trimmedOriginal\n$block\n"
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = EditorDraft(_uiState.value.title, _uiState.value.markdown)
        val snapshot = undoStack.removeLast()
        redoStack.addLast(current)
        restoreSnapshot(snapshot)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = EditorDraft(_uiState.value.title, _uiState.value.markdown)
        val snapshot = redoStack.removeLast()
        undoStack.addLast(current)
        restoreSnapshot(snapshot)
    }

    private fun restoreSnapshot(snapshot: EditorDraft) {
        _uiState.update {
            it.copy(
                title = snapshot.title,
                markdown = snapshot.markdown,
                previewMarkdown = snapshot.markdown,
            )
        }
        draftFlow.value = snapshot
        refreshPreviewMarkdown(snapshot.markdown)
        refreshHistoryFlags()
    }

    private fun pushUndoSnapshot() {
        val current = EditorDraft(_uiState.value.title, _uiState.value.markdown)
        if (undoStack.lastOrNull() == current) return
        undoStack.addLast(current)
        if (undoStack.size > historyLimit) {
            undoStack.removeFirst()
        }
    }

    private fun refreshHistoryFlags() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
        }
    }

    private fun refreshPreviewMarkdown(markdown: String) {
        viewModelScope.launch {
            val mapped = replaceAssetUris(markdown)
            _uiState.update { it.copy(previewMarkdown = mapped) }
        }
    }

    private suspend fun replaceAssetUris(markdown: String): String {
        val regex = Regex("""!\[([^]]*)]\(note://asset/([^)]+)\)""")
        var rendered = markdown
        regex.findAll(markdown).forEach { match ->
            val alt = match.groupValues[1]
            val assetId = match.groupValues[2]
            val path = noteRepository.resolveAssetAbsolutePath(assetId) ?: return@forEach
            val replacement = "![${alt}](file://${path})"
            rendered = rendered.replace(match.value, replacement)
        }
        return rendered
    }

    override fun onCleared() {
        super.onCleared()
        saveJob?.cancel()
    }
}
