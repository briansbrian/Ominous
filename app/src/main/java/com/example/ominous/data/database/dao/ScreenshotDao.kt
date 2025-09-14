package com.example.ominous.data.database.dao

import androidx.room.*
import com.example.ominous.data.database.entities.Screenshot
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    
    @Query("SELECT * FROM screenshots WHERE noteId = :noteId ORDER BY createdAt DESC")
    fun getScreenshotsForNote(noteId: String): Flow<List<Screenshot>>
    
    @Query("SELECT * FROM screenshots WHERE id = :id")
    suspend fun getScreenshotById(id: String): Screenshot?
    
    @Query("SELECT * FROM screenshots ORDER BY createdAt DESC")
    fun getAllScreenshots(): Flow<List<Screenshot>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: Screenshot): Long
    
    @Update
    suspend fun updateScreenshot(screenshot: Screenshot)
    
    @Delete
    suspend fun deleteScreenshot(screenshot: Screenshot)
    
    @Query("DELETE FROM screenshots WHERE id = :id")
    suspend fun deleteScreenshotById(id: String)
    
    @Query("DELETE FROM screenshots WHERE noteId = :noteId")
    suspend fun deleteScreenshotsForNote(noteId: String)
}