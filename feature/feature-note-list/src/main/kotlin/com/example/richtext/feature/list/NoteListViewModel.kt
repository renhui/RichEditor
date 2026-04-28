package com.example.richtext.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.richtext.data.note.model.NoteSort
import com.example.richtext.data.note.repository.NoteRepository
import com.example.richtext.feature.list.model.NoteListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val sortFlow = MutableStateFlow(NoteSort.PINNED_THEN_UPDATED)
    private val debouncedQueryFlow = queryFlow.debounce(300)

    val uiState: StateFlow<NoteListUiState> = combine(
        queryFlow,
        sortFlow,
        noteRepository.observeAll(debouncedQueryFlow, sortFlow),
    ) { query, sort, notes ->
        NoteListUiState(
            query = query,
            sort = sort,
            notes = notes,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NoteListUiState(),
    )

    init {
        viewModelScope.launch {
            noteRepository.ensureSeedData()
        }
    }

    fun updateQuery(query: String) {
        queryFlow.update { query }
    }

    fun toggleSort() {
        sortFlow.update { current ->
            if (current == NoteSort.PINNED_THEN_UPDATED) {
                NoteSort.UPDATED_DESC
            } else {
                NoteSort.PINNED_THEN_UPDATED
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            noteRepository.softDelete(id)
        }
    }

    fun restoreNote(id: String) {
        viewModelScope.launch {
            noteRepository.restore(id)
        }
    }
}
