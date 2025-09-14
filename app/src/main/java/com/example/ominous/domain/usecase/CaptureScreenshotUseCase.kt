package com.example.ominous.domain.usecase

import com.example.ominous.data.database.entities.Screenshot
import com.example.ominous.domain.repository.INoteRepository
import javax.inject.Inject

class CaptureScreenshotUseCase @Inject constructor(
    private val repository: INoteRepository
) {
    suspend operator fun invoke(noteId: String, filePath: String, thumbnailPath: String? = null): String {
        val screenshot = Screenshot(
            noteId = noteId,
            filePath = filePath,
            thumbnailPath = thumbnailPath
        )
        repository.insertScreenshot(screenshot)
        
        // Update note timestamp
        repository.updateTimestamp(noteId)
        
        return screenshot.id
    }
}