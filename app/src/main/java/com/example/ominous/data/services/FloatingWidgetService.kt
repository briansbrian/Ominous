package com.example.ominous.data.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import com.example.ominous.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FloatingWidgetService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var isMinimized = false
    private var layoutParams: WindowManager.LayoutParams? = null
    private lateinit var sharedPreferences: SharedPreferences
    
    // Touch handling for dragging
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        sharedPreferences = getSharedPreferences("floating_widget_prefs", Context.MODE_PRIVATE)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FLOATING_WIDGET -> startFloatingWidget()
            ACTION_SHOW_FLOATING_WIDGET -> {
                // Always check permission before showing
                if (hasOverlayPermission()) {
                    showFloatingWidget()
                } else {
                    android.util.Log.w("FloatingWidget", "Cannot show widget - overlay permission not granted")
                    requestOverlayPermission()
                }
            }
            ACTION_MINIMIZE_WIDGET -> minimizeWidget()
            ACTION_EXPAND_WIDGET -> expandWidget()
            ACTION_STOP_FLOATING_WIDGET -> stopFloatingWidget()
            else -> {
                // Default behavior - start service but wait for explicit show command
                startForeground(
                    Constants.FLOATING_WIDGET_NOTIFICATION_ID,
                    createNotification()
                )
            }
        }
        return START_STICKY
    }
    
    private fun startFloatingWidget() {
        if (floatingView != null) return
        
        startForeground(
            Constants.FLOATING_WIDGET_NOTIFICATION_ID,
            createNotification()
        )
        
        showFloatingWidget()
    }
    
    private fun showFloatingWidget() {
        // Always check overlay permission before showing widget
        if (!hasOverlayPermission()) {
            android.util.Log.w("FloatingWidget", "Overlay permission not granted, cannot show widget")
            requestOverlayPermission()
            return
        }
        
        if (floatingView != null) return
        
        // Restore previous state
        isMinimized = sharedPreferences.getBoolean("is_minimized", false)
        
        if (isMinimized) {
            createMinimizedView()
        } else {
            createExpandedView()
        }
        
        setupLayoutParams()
        
        try {
            windowManager?.addView(floatingView, layoutParams)
            android.util.Log.d("FloatingWidget", "Floating widget shown successfully")
        } catch (e: Exception) {
            android.util.Log.e("FloatingWidget", "Failed to show floating widget", e)
            // Clean up on failure
            floatingView = null
        }
    }
    
    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(this)
        } else {
            true // Permission not required for older versions
        }
    }
    
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            ).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            try {
                startActivity(intent)
                android.util.Log.d("FloatingWidget", "Requesting overlay permission")
                
                // Show a toast to guide the user
                android.widget.Toast.makeText(
                    this,
                    "Please enable 'Display over other apps' for Ominous to use the floating widget",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                
            } catch (e: Exception) {
                android.util.Log.e("FloatingWidget", "Failed to request overlay permission", e)
                android.widget.Toast.makeText(
                    this,
                    "Unable to request overlay permission. Please enable it manually in Settings > Apps > Ominous",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun createMinimizedView() {
        floatingView = TextView(this).apply {
            text = "📝"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = android.view.Gravity.CENTER
            
            // Create circular background programmatically
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0xFF800020.toInt()) // Maroon color
                setSize(80, 80)
            }
            background = drawable
            
            setPadding(16, 16, 16, 16)
            
            setOnClickListener {
                expandWidget()
            }
            
            setOnTouchListener(createTouchListener())
        }
    }
    
    private fun createExpandedView() {
        // Create main container
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xCC800020.toInt()) // Semi-transparent maroon
            setPadding(16, 16, 16, 16)
        }
        
        // Create toolbar
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        
        // Add title and minimize button
        val titleBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val title = TextView(this).apply {
            text = "Ominous"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
            setPadding(0, 0, 16, 0)
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        
        val minimizeBtn = Button(this).apply {
            text = "−"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x44FFFFFF.toInt())
            setPadding(12, 8, 12, 8)
            setOnClickListener { minimizeWidget() }
        }
        
        titleBar.addView(title)
        titleBar.addView(minimizeBtn)
        container.addView(titleBar)
        
        // Add main toolbar with action buttons
        val screenshotBtn = Button(this).apply {
            text = "📷 Screenshot"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x44FFFFFF.toInt())
            setPadding(16, 12, 16, 12)
            setOnClickListener { handleScreenshotClick() }
        }
        
        val noteBtn = Button(this).apply {
            text = "📝 New Note"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x44FFFFFF.toInt())
            setPadding(16, 12, 16, 12)
            setOnClickListener { handleNoteClick() }
        }
        
        val closeBtn = Button(this).apply {
            text = "✕ Close"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x44FF0000.toInt())
            setPadding(16, 12, 16, 12)
            setOnClickListener { stopFloatingWidget() }
        }
        
        toolbar.addView(screenshotBtn)
        toolbar.addView(noteBtn)
        toolbar.addView(closeBtn)
        container.addView(toolbar)
        
        floatingView = container
        floatingView?.setOnTouchListener(createTouchListener())
    }
    
    private fun setupLayoutParams() {
        // Restore saved position and size
        val savedX = sharedPreferences.getInt("widget_x", 100)
        val savedY = sharedPreferences.getInt("widget_y", 100)
        val savedWidth = sharedPreferences.getInt("widget_width", WindowManager.LayoutParams.WRAP_CONTENT)
        val savedHeight = sharedPreferences.getInt("widget_height", WindowManager.LayoutParams.WRAP_CONTENT)
        
        layoutParams = WindowManager.LayoutParams(
            if (isMinimized) 80 else savedWidth,
            if (isMinimized) 80 else savedHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }
    }
    
    private fun createTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams?.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams?.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Save position
                    saveWidgetState()
                    false
                }
                else -> false
            }
        }
    }
    
    private fun minimizeWidget() {
        isMinimized = true
        hideFloatingWidget()
        createMinimizedView()
        setupLayoutParams()
        windowManager?.addView(floatingView, layoutParams)
        saveWidgetState()
    }
    
    private fun expandWidget() {
        isMinimized = false
        hideFloatingWidget()
        createExpandedView()
        setupLayoutParams()
        windowManager?.addView(floatingView, layoutParams)
        saveWidgetState()
    }
    
    private fun hideFloatingWidget() {
        floatingView?.let { view ->
            windowManager?.removeView(view)
            floatingView = null
        }
    }
    
    private fun saveWidgetState() {
        sharedPreferences.edit().apply {
            putBoolean("is_minimized", isMinimized)
            putInt("widget_x", layoutParams?.x ?: 100)
            putInt("widget_y", layoutParams?.y ?: 100)
            if (!isMinimized) {
                putInt("widget_width", layoutParams?.width ?: WindowManager.LayoutParams.WRAP_CONTENT)
                putInt("widget_height", layoutParams?.height ?: WindowManager.LayoutParams.WRAP_CONTENT)
            }
            apply()
        }
    }
    
    private fun handleScreenshotClick() {
        // Start screenshot capture
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        
        // TODO: Implement screenshot capture functionality
        android.util.Log.d("FloatingWidget", "Screenshot clicked")
    }
    
    private fun handleNoteClick() {
        // Open main app for note creation
        val intent = Intent(this, com.example.ominous.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("action", "create_note")
        }
        startActivity(intent)
        android.util.Log.d("FloatingWidget", "Note clicked - opening main app")
    }
    
    private fun stopFloatingWidget() {
        saveWidgetState()
        hideFloatingWidget()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Floating widget service notification"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val showIntent = Intent(this, FloatingWidgetService::class.java).apply {
            action = ACTION_SHOW_FLOATING_WIDGET
        }
        val showPendingIntent = PendingIntent.getService(
            this, 1, showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, FloatingWidgetService::class.java).apply {
            action = ACTION_STOP_FLOATING_WIDGET
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Ominous Floating Widget")
            .setContentText("Tap to show floating overlay for notes and screenshots")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .addAction(
                android.R.drawable.ic_menu_add,
                "Show Widget",
                showPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent
            )
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        saveWidgetState()
        hideFloatingWidget()
    }
    
    companion object {
        const val ACTION_START_FLOATING_WIDGET = "START_FLOATING_WIDGET"
        const val ACTION_SHOW_FLOATING_WIDGET = "SHOW_FLOATING_WIDGET"
        const val ACTION_MINIMIZE_WIDGET = "MINIMIZE_WIDGET"
        const val ACTION_EXPAND_WIDGET = "EXPAND_WIDGET"
        const val ACTION_STOP_FLOATING_WIDGET = "STOP_FLOATING_WIDGET"
    }
}