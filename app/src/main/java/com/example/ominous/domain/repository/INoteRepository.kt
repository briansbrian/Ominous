package com.example.ominous.domain.repository

import com.example.ominous.data.database.entities.Note
import com.example.ominous.data.database.entities.Screenshot
import com.example.ominous.domain.model.NoteWithScreenshots
import kotlinx.coroutines.flow.Flow

interface INoteRepository {
    
    // Note operations
    fun getAllNotes(): Flow<List<Note>>
    fun getPinnedNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun insertNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun deleteNoteById(id: String)
    suspend fun updatePinnedStatus(id: String, isPinned: Boolean)
    suspend fun updateTimestamp(id: String, timestamp: Long = System.currentTimeMillis())
    
    // Screenshot operations
    fun getScreenshotsForNote(noteId: String): Flow<List<Screenshot>>
    fun getAllScreenshots(): Flow<List<Screenshot>>
    suspend fun getScreenshotById(id: String): Screenshot?
    suspend fun insertScreenshot(screenshot: Screenshot): Long
    suspend fun updateScreenshot(screenshot: Screenshot)
    suspend fun deleteScreenshot(screenshot: Screenshot)
    suspend fun deleteScreenshotById(id: String)
    suspend fun deleteScreenshotsForNote(noteId: String)
    
    // Combined operations
    fun getNotesWithScreenshots(): Flow<List<NoteWithScreenshots>>
    suspend fun getNoteWithScreenshots(noteId: String): NoteWithScreenshots?
}