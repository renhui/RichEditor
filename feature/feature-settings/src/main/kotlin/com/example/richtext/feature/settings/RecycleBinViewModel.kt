package com.example.richtext.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.richtext.data.note.repository.NoteRepository
import com.example.richtext.feature.settings.model.RecycleBinUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    val uiState: StateFlow<RecycleBinUiState> = noteRepository.observeDeleted()
        .map { deleted ->
            RecycleBinUiState(
                notes = deleted,
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecycleBinUiState(),
        )

    fun restore(id: String) {
        viewModelScope.launch { noteRepository.restore(id) }
    }

    fun purge(id: String) {
        viewModelScope.launch { noteRepository.purge(id) }
    }
}
