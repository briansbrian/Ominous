package com.example.ominous.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ominous.data.database.entities.Note
import com.example.ominous.domain.repository.INoteRepository
import com.example.ominous.domain.usecase.CreateNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: INoteRepository,
    private val createNoteUseCase: CreateNoteUseCase
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _selectedNotes = MutableStateFlow<Set<String>>(emptySet())
    val selectedNotes = _selectedNotes.asStateFlow()
    
    val notes = searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getAllNotes()
        } else {
            repository.searchNotes(query)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val pinnedNotes = repository.getPinnedNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun createNote(content: String) {
        viewModelScope.launch {
            createNoteUseCase(content)
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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
    
    fun togglePinStatus(noteId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.updatePinnedStatus(noteId, isPinned)
        }
    }
}