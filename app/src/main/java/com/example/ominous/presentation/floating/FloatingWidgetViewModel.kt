package com.example.ominous.presentation.floating

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ominous.data.database.entities.Note
import com.example.ominous.data.database.entities.Screenshot
import com.example.ominous.domain.repository.INoteRepository
import com.example.ominous.domain.usecase.CreateNoteUseCase
import com.example.ominous.domain.usecase.CaptureScreenshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FloatingWidgetUiState(
    val isMinimized: Boolean = false,
    val currentNote: Note? = null,
    val recentScreenshots: List<Screenshot> = emptyList()
)

@HiltViewModel
class FloatingWidgetViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val repository: INoteRepository,
    private val createNoteUseCase: CreateNoteUseCase,
    private val captureScreenshotUseCase: CaptureScreenshotUseCase
) : ViewModel() {
    
    private val _isMinimized = MutableStateFlow(false)
    private val _currentNote = MutableStateFlow<Note?>(null)
    private val _recentScreenshots = MutableStateFlow<List<Screenshot>>(emptyList())
    
    val uiState = combine(
        _isMinimized,
        _currentNote,
        _recentScreenshots
    ) { isMinimized, currentNote, screenshots ->
        FloatingWidgetUiState(
            isMinimized = isMinimized,
            currentNote = currentNote,
            recentScreenshots = screenshots
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FloatingWidgetUiState()
    )
    
    init {
        loadRecentScreenshots()
    }
    
    fun minimize() {
        _isMinimized.value = true
    }
    
    fun expand() {
        _isMinimized.value = false
    }
    
    fun setCurrentNote(note: Note?) {
        _currentNote.value = note
    }
    
    fun captureScreenshot() {
        val currentNote = _currentNote.value
        if (currentNote != null) {
            viewModelScope.launch {
                captureScreenshotUseCase(
                    noteId = currentNote.id,
                    filePath = "", // Will be set by the service
                    thumbnailPath = "" // Will be set by the service
                )
                loadRecentScreenshots()
            }
        }
    }
    
    fun createNewNote() {
        viewModelScope.launch {
            val noteId = createNoteUseCase("New note from floating widget")
            val newNote = repository.getNoteById(noteId.toString())
            _currentNote.value = newNote
        }
    }
    
    private fun loadRecentScreenshots() {
        viewModelScope.launch {
            repository.getAllScreenshots()
                .collect { screenshots ->
                    _recentScreenshots.value = screenshots.take(5)
                }
        }
    }
}