package com.example.ominous.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ominous.data.services.ScreenshotCaptureService

/**
 * Transparent activity that handles MediaProjection permission requests
 * for the floating widget service. This is necessary because services 
 * cannot directly launch activities for permission requests.
 */
class MediaProjectionPermissionActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MediaProjectionPermission"
        const val EXTRA_NOTE_ID = "extra_note_id"
        
        fun createIntent(context: Context, noteId: Long): Intent {
            return Intent(context, MediaProjectionPermissionActivity::class.java).apply {
                putExtra(EXTRA_NOTE_ID, noteId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }

    private var noteId: Long = -1L
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "MediaProjection permission granted")
            result.data?.let { data ->
                startScreenshotCapture(result.resultCode, data)
            }
        } else {
            Log.w(TAG, "MediaProjection permission denied")
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make this activity transparent and non-intrusive
        setFinishOnTouchOutside(true)
        
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
        if (noteId == -1L) {
            Log.e(TAG, "No note ID provided")
            finish()
            return
        }

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        // Request MediaProjection permission
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        mediaProjectionLauncher.launch(captureIntent)
    }

    private fun startScreenshotCapture(resultCode: Int, resultData: Intent) {
        Log.d(TAG, "Starting screenshot capture for note ID: $noteId")
        
        // Start the capture service first
        val captureIntent = Intent(this, ScreenshotCaptureService::class.java).apply {
            action = ScreenshotCaptureService.ACTION_START_CAPTURE
            putExtra(ScreenshotCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenshotCaptureService.EXTRA_RESULT_DATA, resultData)
        }
        startForegroundService(captureIntent)
        
        // Give the service a moment to set up, then take the screenshot
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val screenshotIntent = Intent(this, ScreenshotCaptureService::class.java).apply {
                action = ScreenshotCaptureService.ACTION_TAKE_SCREENSHOT
                putExtra(ScreenshotCaptureService.EXTRA_NOTE_ID, noteId)
            }
            startService(screenshotIntent)
        }, 500) // 500ms delay to ensure service is ready
        
        // Notify the floating widget that screenshot is starting
        sendBroadcast(Intent().apply {
            action = "com.example.ominous.SCREENSHOT_STARTING"
            putExtra("noteId", noteId)
        })
    }
}