package com.example.ominous.di

import android.content.Context
import androidx.room.Room
import com.example.ominous.data.database.OminousDatabase
import com.example.ominous.data.database.dao.NoteDao
import com.example.ominous.data.database.dao.ScreenshotDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideOminousDatabase(@ApplicationContext context: Context): OminousDatabase {
        return Room.databaseBuilder(
            context,
            OminousDatabase::class.java,
            OminousDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    fun provideNoteDao(database: OminousDatabase): NoteDao {
        return database.noteDao()
    }
    
    @Provides
    fun provideScreenshotDao(database: OminousDatabase): ScreenshotDao {
        return database.screenshotDao()
    }
}