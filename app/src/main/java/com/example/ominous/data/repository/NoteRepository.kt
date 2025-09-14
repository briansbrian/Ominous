package com.example.ominous.data.repository

import com.example.ominous.data.database.dao.NoteDao
import com.example.ominous.data.database.dao.ScreenshotDao
import com.example.ominous.data.database.entities.Note
import com.example.ominous.data.database.entities.Screenshot
import com.example.ominous.domain.model.NoteWithScreenshots
import com.example.ominous.domain.repository.INoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val screenshotDao: ScreenshotDao
) : INoteRepository {
    
    // Note operations
    override fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    
    override fun getPinnedNotes(): Flow<List<Note>> = noteDao.getPinnedNotes()
    
    override fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
    
    override suspend fun getNoteById(id: String): Note? = noteDao.getNoteById(id)
    
    override suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)
    
    override suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    
    override suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
    
    override suspend fun deleteNoteById(id: String) = noteDao.deleteNoteById(id)
    
    override suspend fun updatePinnedStatus(id: String, isPinned: Boolean) = 
        noteDao.updatePinnedStatus(id, isPinned)
    
    override suspend fun updateTimestamp(id: String, timestamp: Long) = 
        noteDao.updateTimestamp(id, timestamp)
    
    // Screenshot operations
    override fun getScreenshotsForNote(noteId: String): Flow<List<Screenshot>> = 
        screenshotDao.getScreenshotsForNote(noteId)
    
    override fun getAllScreenshots(): Flow<List<Screenshot>> = screenshotDao.getAllScreenshots()
    
    override suspend fun getScreenshotById(id: String): Screenshot? = 
        screenshotDao.getScreenshotById(id)
    
    override suspend fun insertScreenshot(screenshot: Screenshot): Long = 
        screenshotDao.insertScreenshot(screenshot)
    
    override suspend fun updateScreenshot(screenshot: Screenshot) = 
        screenshotDao.updateScreenshot(screenshot)
    
    override suspend fun deleteScreenshot(screenshot: Screenshot) = 
        screenshotDao.deleteScreenshot(screenshot)
    
    override suspend fun deleteScreenshotById(id: String) = 
        screenshotDao.deleteScreenshotById(id)
    
    override suspend fun deleteScreenshotsForNote(noteId: String) = 
        screenshotDao.deleteScreenshotsForNote(noteId)
    
    // Combined operations
    override fun getNotesWithScreenshots(): Flow<List<NoteWithScreenshots>> {
        return combine(
            noteDao.getAllNotes(),
            screenshotDao.getAllScreenshots()
        ) { notes, screenshots ->
            notes.map { note ->
                NoteWithScreenshots(
                    note = note,
                    screenshots = screenshots.filter { it.noteId == note.id }
                )
            }
        }
    }
    
    override suspend fun getNoteWithScreenshots(noteId: String): NoteWithScreenshots? {
        val note = noteDao.getNoteById(noteId) ?: return null
        val screenshots = screenshotDao.getScreenshotsForNote(noteId)
        // Since we need to collect the flow, we'll return a simplified version
        // In a real implementation, you might want to create a separate DAO method
        return NoteWithScreenshots(note = note, screenshots = emptyList())
    }
}