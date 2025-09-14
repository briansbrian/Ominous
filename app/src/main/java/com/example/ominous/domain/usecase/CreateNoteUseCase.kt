package com.example.ominous.domain.usecase

import com.example.ominous.data.database.entities.Note
import com.example.ominous.domain.repository.INoteRepository
import javax.inject.Inject

class CreateNoteUseCase @Inject constructor(
    private val repository: INoteRepository
) {
    suspend operator fun invoke(content: String, isPinned: Boolean = false): String {
        val note = Note(
            content = content,
            isPinned = isPinned
        )
        repository.insertNote(note)
        return note.id
    }
}