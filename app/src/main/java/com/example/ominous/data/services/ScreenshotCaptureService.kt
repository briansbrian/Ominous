package com.example.ominous.data.services

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.ominous.R
import com.example.ominous.domain.usecase.CaptureScreenshotUseCase
import com.example.ominous.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ScreenshotCaptureService : Service() {

    @Inject
    lateinit var captureScreenshotUseCase: CaptureScreenshotUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var windowManager: WindowManager? = null
    private var displayMetrics: DisplayMetrics? = null

    companion object {
        private const val TAG = "ScreenshotCaptureService"
        const val ACTION_START_CAPTURE = "com.example.ominous.START_CAPTURE"
        const val ACTION_STOP_CAPTURE = "com.example.ominous.STOP_CAPTURE"
        const val ACTION_TAKE_SCREENSHOT = "com.example.ominous.TAKE_SCREENSHOT"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        const val EXTRA_NOTE_ID = "note_id"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        displayMetrics = DisplayMetrics().also {
            windowManager?.defaultDisplay?.getMetrics(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CAPTURE -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, -1)
                val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
                if (resultCode != -1 && resultData != null) {
                    startCapture(resultCode, resultData)
                }
            }
            ACTION_TAKE_SCREENSHOT -> {
                val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
                takeScreenshot(noteId)
            }
            ACTION_STOP_CAPTURE -> {
                stopCapture()
            }
        }
        return START_STICKY
    }

    private fun startCapture(resultCode: Int, resultData: Intent) {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)
        
        setupImageReader()
        createVirtualDisplay()
        startForeground()
    }

    private fun setupImageReader() {
        val metrics = displayMetrics ?: return
        
        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            1
        ).apply {
            setOnImageAvailableListener({ reader ->
                serviceScope.launch(Dispatchers.IO) {
                    processImage(reader)
                }
            }, null)
        }
    }

    private fun createVirtualDisplay() {
        val metrics = displayMetrics ?: return
        val reader = imageReader ?: return
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            null
        )
    }

    private fun takeScreenshot(noteId: Long) {
        // Trigger image capture by acquiring latest image
        imageReader?.let { reader ->
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val image = reader.acquireLatestImage()
                    if (image != null) {
                        processImageForNote(image, noteId)
                        image.close()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error taking screenshot", e)
                }
            }
        }
    }

    private fun processImage(reader: ImageReader) {
        try {
            val image = reader.acquireLatestImage()
            image?.let {
                // Process for general capture if needed
                it.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
        }
    }

    private fun processImageForNote(image: Image, noteId: Long) {
        try {
            val bitmap = imageToBitmap(image)
            val compressedBitmap = compressBitmap(bitmap)
            val filePath = saveBitmapToFile(compressedBitmap)
            val thumbnailPath = generateThumbnail(compressedBitmap)
            
            serviceScope.launch {
                captureScreenshotUseCase.invoke(
                    noteId = noteId.toString(),
                    filePath = filePath,
                    thumbnailPath = thumbnailPath
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image for note", e)
        }
    }

    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        
        return if (rowPadding == 0) {
            bitmap
        } else {
            Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
        }
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val maxWidth = 1920
        val maxHeight = 1080
        
        return if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
            val ratio = minOf(
                maxWidth.toFloat() / bitmap.width,
                maxHeight.toFloat() / bitmap.height
            )
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val screenshotsDir = File(filesDir, "screenshots")
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs()
        }
        
        val fileName = "screenshot_${System.currentTimeMillis()}.jpg"
        val file = File(screenshotsDir, fileName)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        
        return file.absolutePath
    }

    private fun generateThumbnail(bitmap: Bitmap): String {
        val thumbnailsDir = File(filesDir, "thumbnails")
        if (!thumbnailsDir.exists()) {
            thumbnailsDir.mkdirs()
        }
        
        val thumbnailSize = 200
        val thumbnail = Bitmap.createScaledBitmap(
            bitmap, 
            thumbnailSize, 
            (thumbnailSize * bitmap.height / bitmap.width), 
            true
        )
        
        val fileName = "thumb_${System.currentTimeMillis()}.jpg"
        val file = File(thumbnailsDir, fileName)
        
        FileOutputStream(file).use { out ->
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }
        
        return file.absolutePath
    }

    private fun startForeground() {
        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Screenshot Capture Active")
            .setContentText("Ready to capture screenshots")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(Constants.SCREENSHOT_SERVICE_NOTIFICATION_ID, notification)
    }

    private fun stopCapture() {
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCapture()
    }
}