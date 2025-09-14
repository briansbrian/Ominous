package com.example.ominous.di

import com.example.ominous.data.repository.NoteRepository
import com.example.ominous.domain.repository.INoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        noteRepository: NoteRepository
    ): INoteRepository
}