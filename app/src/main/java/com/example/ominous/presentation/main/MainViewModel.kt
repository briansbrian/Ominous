package com.example.ominous.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ominous.data.database.entities.Note
import com.example.ominous.data.database.entities.Screenshot
import com.example.ominous.domain.model.ExportFormat
import com.example.ominous.domain.model.ExportOptions
import com.example.ominous.domain.repository.INoteRepository
import com.example.ominous.domain.usecase.CreateNoteUseCase
import com.example.ominous.domain.usecase.ExportNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val allNotes: List<Note> = emptyList(),
    val pinnedNotes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val noteScreenshots: Map<String, List<Screenshot>> = emptyMap(),
    val searchQuery: String = "",
    val selectedNotes: Set<String> = emptySet(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: INoteRepository,
    private val createNoteUseCase: CreateNoteUseCase,
    private val exportNotesUseCase: ExportNotesUseCase
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedNotes = MutableStateFlow<Set<String>>(emptySet())
    private val _isLoading = MutableStateFlow(false)
    
    private val allNotes = repository.getAllNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    private val noteScreenshots = repository.getAllScreenshots().map { screenshots ->
        screenshots.groupBy { it.noteId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
    
    val uiState = combine(
        allNotes,
        noteScreenshots,
        _searchQuery,
        _selectedNotes,
        _isLoading
    ) { notes, screenshots, query, selected, loading ->
        val pinnedNotes = notes.filter { it.isPinned }
        val filteredNotes = if (query.isBlank()) {
            notes
        } else {
            notes.filter { note ->
                note.content.contains(query, ignoreCase = true) ||
                note.tags.any { it.contains(query, ignoreCase = true) }
            }
        }
        
        MainUiState(
            allNotes = notes,
            pinnedNotes = pinnedNotes,
            filteredNotes = filteredNotes,
            noteScreenshots = screenshots,
            searchQuery = query,
            selectedNotes = selected,
            isLoading = loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )
    
    fun createNote(content: String) {
        viewModelScope.launch {
            createNoteUseCase(content)
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun selectNote(note: Note) {
        // Handle single note selection (e.g., navigate to detail view)
        // For now, just clear selection
        clearSelection()
    }
    
    fun toggleNoteSelection(noteId: String) {
        val currentSelection = _selectedNotes.value.toMutableSet()
        if (currentSelection.contains(noteId)) {
            currentSelection.remove(noteId)
        } else {
            currentSelection.add(noteId)
        }
        _selectedNotes.value = currentSelection
    }
    
    fun clearSelection() {
        _selectedNotes.value = emptySet()
    }
    
    fun deleteSelectedNotes() {
        viewModelScope.launch {
            _selectedNotes.value.forEach { noteId ->
                repository.deleteNoteById(noteId)
            }
            clearSelection()
        }
    }
    
    fun exportNotes(format: ExportFormat, includeScreenshots: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                exportNotesUseCase(
                    ExportOptions(
                        format = format,
                        includeImages = includeScreenshots
                    )
                )
                clearSelection()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun togglePinStatus(noteId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.updatePinnedStatus(noteId, isPinned)
        }
    }
}