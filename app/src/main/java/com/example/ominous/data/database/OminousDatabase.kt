package com.example.ominous.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.ominous.data.database.dao.NoteDao
import com.example.ominous.data.database.dao.ScreenshotDao
import com.example.ominous.data.database.entities.Note
import com.example.ominous.data.database.entities.Screenshot

@Database(
    entities = [Note::class, Screenshot::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(com.example.ominous.data.database.TypeConverters::class)
abstract class OminousDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    abstract fun screenshotDao(): ScreenshotDao
    
    companion object {
        const val DATABASE_NAME = "ominous_database"
    }
}