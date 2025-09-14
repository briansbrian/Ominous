package com.example.ominous.domain.usecase

import android.content.ClipboardManager
import android.content.Context
import com.example.ominous.domain.repository.INoteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ClipboardMonitorUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: INoteRepository
) {
    
    fun startMonitoring(onClipboardChange: (String) -> Unit) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        clipboardManager.addPrimaryClipChangedListener {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0).text?.toString()
                if (!clipText.isNullOrBlank() && !isSensitiveData(clipText)) {
                    onClipboardChange(clipText)
                }
            }
        }
    }
    
    private fun isSensitiveData(text: String): Boolean {
        // Basic sensitive data detection
        val sensitivePatterns = listOf(
            "password",
            "pin",
            "ssn",
            "credit card",
            "cvv",
            "social security"
        )
        
        return sensitivePatterns.any { pattern ->
            text.lowercase().contains(pattern)
        }
    }
}