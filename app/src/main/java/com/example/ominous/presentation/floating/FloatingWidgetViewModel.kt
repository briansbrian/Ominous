package com.example.ominous.presentation.floating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ominous.domain.repository.INoteRepository
import com.example.ominous.domain.usecase.CreateNoteUseCase
import com.example.ominous.domain.usecase.CaptureScreenshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FloatingWidgetViewModel @Inject constructor(
    private val repository: INoteRepository,
    private val createNoteUseCase: CreateNoteUseCase,
    private val captureScreenshotUseCase: CaptureScreenshotUseCase
) : ViewModel() {
    
    private val _isMinimized = MutableStateFlow(false)
    val isMinimized = _isMinimized.asStateFlow()
    
    private val _currentNoteContent = MutableStateFlow("")
    val currentNoteContent = _currentNoteContent.asStateFlow()
    
    private val _activeNoteId = MutableStateFlow<String?>(null)
    val activeNoteId = _activeNoteId.asStateFlow()
    
    fun toggleMinimized() {
        _isMinimized.value = !_isMinimized.value
    }
    
    fun updateNoteContent(content: String) {
        _currentNoteContent.value = content
        
        // Auto-save logic
        val noteId = _activeNoteId.value
        if (noteId != null) {
            // Update existing note
            viewModelScope.launch {
                val note = repository.getNoteById(noteId)
                if (note != null) {
                    repository.updateNote(note.copy(content = content, updatedAt = System.currentTimeMillis()))
                }
            }
        } else if (content.isNotBlank()) {
            // Create new note
            viewModelScope.launch {
                val newNoteId = createNoteUseCase(content)
                _activeNoteId.value = newNoteId
            }
        }
    }
    
    fun captureScreenshot() {
        val noteId = _activeNoteId.value
        if (noteId != null) {
            viewModelScope.launch {
                // TODO: Implement actual screenshot capture
                // For now, just create a placeholder
                captureScreenshotUseCase(noteId, "placeholder_path")
            }
        }
    }
    
    fun createNewNote() {
        _currentNoteContent.value = ""
        _activeNoteId.value = null
    }
}