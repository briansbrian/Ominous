package com.example.ominous.data.services

import android.app.Service
import android.content.ClipboardManager
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ominous.R
import com.example.ominous.domain.usecase.ClipboardMonitorUseCase
import com.example.ominous.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardMonitorService : Service() {

    @Inject
    lateinit var clipboardMonitorUseCase: ClipboardMonitorUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var clipboardManager: ClipboardManager? = null
    private var clipboardListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    private var isMonitoring = false

    companion object {
        private const val TAG = "ClipboardMonitorService"
        const val ACTION_START_MONITORING = "com.example.ominous.START_CLIPBOARD_MONITORING"
        const val ACTION_STOP_MONITORING = "com.example.ominous.STOP_CLIPBOARD_MONITORING"
        
        // Sensitive data patterns to filter out
        private val SENSITIVE_PATTERNS = listOf(
            Regex("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"), // Credit card numbers
            Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"), // SSN format
            Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // Email addresses
            Regex("\\b(?:\\+?1[-.]?)?\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4}\\b"), // Phone numbers
            Regex("\\bpassword\\s*[:=]\\s*\\S+", RegexOption.IGNORE_CASE), // Password fields
            Regex("\\btoken\\s*[:=]\\s*\\S+", RegexOption.IGNORE_CASE), // API tokens
            Regex("\\bkey\\s*[:=]\\s*\\S+", RegexOption.IGNORE_CASE) // API keys
        )
    }

    override fun onCreate() {
        super.onCreate()
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        setupClipboardListener()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> startMonitoring()
            ACTION_STOP_MONITORING -> stopMonitoring()
        }
        return START_STICKY
    }

    private fun setupClipboardListener() {
        clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
            if (isMonitoring) {
                handleClipboardChange()
            }
        }
    }

    private fun startMonitoring() {
        if (!isMonitoring) {
            isMonitoring = true
            clipboardListener?.let { listener ->
                clipboardManager?.addPrimaryClipChangedListener(listener)
            }
            startForeground()
            Log.d(TAG, "Clipboard monitoring started")
        }
    }

    private fun stopMonitoring() {
        if (isMonitoring) {
            isMonitoring = false
            clipboardListener?.let { listener ->
                clipboardManager?.removePrimaryClipChangedListener(listener)
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            Log.d(TAG, "Clipboard monitoring stopped")
        }
    }

    private fun handleClipboardChange() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val clipData = clipboardManager?.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val clipText = clipData.getItemAt(0).text?.toString()
                    
                    if (!clipText.isNullOrBlank()) {
                        val filteredText = filterSensitiveData(clipText)
                        if (filteredText != null && filteredText.length >= 10) { // Minimum length threshold
                            // Create a new note with the clipboard content
                            // For now, we'll just log it - the actual implementation would create a note
                            android.util.Log.d(TAG, "Clipboard content: $filteredText")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling clipboard change", e)
            }
        }
    }

    private fun filterSensitiveData(text: String): String? {
        // Check if text contains sensitive patterns
        for (pattern in SENSITIVE_PATTERNS) {
            if (pattern.containsMatchIn(text)) {
                Log.d(TAG, "Filtered out sensitive clipboard content")
                return null // Don't process sensitive data
            }
        }

        // Additional filtering for common sensitive keywords
        val lowerText = text.lowercase()
        val sensitiveKeywords = listOf(
            "password", "passwd", "pwd", "secret", "token", "key", "auth",
            "credit card", "ssn", "social security", "bank account", "routing"
        )

        for (keyword in sensitiveKeywords) {
            if (lowerText.contains(keyword)) {
                Log.d(TAG, "Filtered out clipboard content with sensitive keyword: $keyword")
                return null
            }
        }

        // Filter out very short or very long content
        return when {
            text.length < 10 -> null // Too short
            text.length > 5000 -> text.take(5000) // Truncate if too long
            else -> text
        }
    }

    private fun startForeground() {
        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Clipboard Monitor Active")
            .setContentText("Monitoring clipboard for note content")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(Constants.CLIPBOARD_SERVICE_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
    }
}