package com.example.ominous.data.database.dao

import androidx.room.*
import com.example.ominous.data.database.entities.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): Note?
    
    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedNotes(): Flow<List<Note>>
    
    @Query("SELECT * FROM notes WHERE content LIKE '%' || :query || '%' OR :query IN (SELECT value FROM json_each(tags)) ORDER BY updatedAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long
    
    @Update
    suspend fun updateNote(note: Note)
    
    @Delete
    suspend fun deleteNote(note: Note)
    
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)
    
    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinnedStatus(id: String, isPinned: Boolean)
    
    @Query("UPDATE notes SET updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTimestamp(id: String, timestamp: Long = System.currentTimeMillis())
}