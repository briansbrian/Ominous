package com.example.ominous.data.services

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.core.app.NotificationCompat
import com.example.ominous.presentation.MediaProjectionPermissionActivity
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
    
    // Note editing state
    private var currentNoteContent = ""
    private var noteEditText: EditText? = null
    private var currentNoteId: Long = -1L
    
    // Touch handling for dragging
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    // Screenshot handling
    private var screenshotReceiver: BroadcastReceiver? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        sharedPreferences = getSharedPreferences("floating_widget_prefs", Context.MODE_PRIVATE)
        
        // Register screenshot broadcast receiver
        screenshotReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "com.example.ominous.SCREENSHOT_STARTING" -> {
                        temporaryHideWidget()
                    }
                    "com.example.ominous.SCREENSHOT_COMPLETED" -> {
                        showWidget()
                        // Show toast notification
                        val error = intent.getStringExtra("error")
                        if (error != null) {
                            android.widget.Toast.makeText(this@FloatingWidgetService, "Screenshot failed: $error", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(this@FloatingWidgetService, "Screenshot saved!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction("com.example.ominous.SCREENSHOT_STARTING")
            addAction("com.example.ominous.SCREENSHOT_COMPLETED")
        }
        registerReceiver(screenshotReceiver, filter)
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
        
        // Load any saved note content
        loadSavedNote()
        
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
                setStroke(3, 0xFFFFFFFF.toInt()) // White border to make it more obvious
            }
            background = drawable
            
            setPadding(16, 16, 16, 16)
            
            // Make it more obviously clickable
            isClickable = true
            isFocusable = true
            
            setOnClickListener {
                android.util.Log.d("FloatingWidget", "Minimized icon clicked - expanding widget")
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
        
        // Create title bar with minimize button
        val titleBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val title = TextView(this).apply {
            text = "Ominous - Quick Note"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
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
            setPadding(8, 4, 8, 4)
            textSize = 16f
            setOnClickListener { minimizeWidget() }
        }
        
        titleBar.addView(title)
        titleBar.addView(minimizeBtn)
        container.addView(titleBar)
        
        // Add note editing area
        noteEditText = EditText(this).apply {
            hint = "Type your note here..."
            setHintTextColor(0xAAFFFFFF.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x44FFFFFF.toInt())
            setPadding(12, 12, 12, 12)
            gravity = Gravity.TOP
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            maxLines = 8
            setText(currentNoteContent)
            
            // Auto-save as user types
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    currentNoteContent = s?.toString() ?: ""
                    saveCurrentNote()
                }
            })
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f // Take up remaining space
            ).apply {
                topMargin = 8
                bottomMargin = 8
            }
        }
        
        container.addView(noteEditText)
        
        // Create action buttons toolbar
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        
        val screenshotBtn = Button(this).apply {
            text = "📷"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x44FFFFFF.toInt())
            setPadding(12, 8, 12, 8)
            setOnClickListener { handleScreenshotClick() }
        }
        
        val newNoteBtn = Button(this).apply {
            text = "�"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x44FFFFFF.toInt())
            setPadding(12, 8, 12, 8)
            setOnClickListener { createNewNote() }
        }
        
        val saveBtn = Button(this).apply {
            text = "💾"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x4400FF00.toInt())
            setPadding(12, 8, 12, 8)
            setOnClickListener { saveCurrentNote() }
        }
        
        val closeBtn = Button(this).apply {
            text = "✕"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0x44FF0000.toInt())
            setPadding(12, 8, 12, 8)
            setOnClickListener { stopFloatingWidget() }
        }
        
        toolbar.addView(screenshotBtn)
        toolbar.addView(newNoteBtn)
        toolbar.addView(saveBtn)
        toolbar.addView(closeBtn)
        container.addView(toolbar)
        
        floatingView = container
        floatingView?.setOnTouchListener(createTouchListener())
    }
    
    private fun setupLayoutParams() {
        // Restore saved position and size
        val savedX = sharedPreferences.getInt("widget_x", 100)
        val savedY = sharedPreferences.getInt("widget_y", 100)
        val savedWidth = if (isMinimized) 80 else sharedPreferences.getInt("widget_width", 350)
        val savedHeight = if (isMinimized) 80 else sharedPreferences.getInt("widget_height", 300)
        
        layoutParams = WindowManager.LayoutParams(
            savedWidth,
            savedHeight,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            if (isMinimized) {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            } else {
                // Allow focus for text input when expanded
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            },
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }
    }
    
    private fun createTouchListener(): View.OnTouchListener {
        return View.OnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    // Only move if there's significant movement (avoid interfering with clicks)
                    if (kotlin.math.abs(deltaX) > 10 || kotlin.math.abs(deltaY) > 10) {
                        layoutParams?.x = initialX + deltaX.toInt()
                        layoutParams?.y = initialY + deltaY.toInt()
                        windowManager?.updateViewLayout(floatingView, layoutParams)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    // If movement was minimal, treat as click
                    if (kotlin.math.abs(deltaX) < 10 && kotlin.math.abs(deltaY) < 10) {
                        view.performClick()
                        return@OnTouchListener true
                    }
                    
                    // Save position after drag
                    saveWidgetState()
                    true
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
        android.util.Log.d("FloatingWidget", "Expanding widget from minimized state")
        isMinimized = false
        hideFloatingWidget()
        createExpandedView()
        setupLayoutParams()
        windowManager?.addView(floatingView, layoutParams)
        saveWidgetState()
        android.util.Log.d("FloatingWidget", "Widget expanded successfully")
    }
    
    private fun hideFloatingWidget() {
        floatingView?.let { view ->
            windowManager?.removeView(view)
            floatingView = null
        }
    }
    
    private fun temporaryHideWidget() {
        floatingView?.visibility = View.GONE
        android.util.Log.d("FloatingWidget", "Widget temporarily hidden for screenshot")
    }
    
    private fun showWidget() {
        floatingView?.visibility = View.VISIBLE
        android.util.Log.d("FloatingWidget", "Widget shown after screenshot")
    }
    
    private fun saveWidgetState() {
        sharedPreferences.edit().apply {
            putBoolean("is_minimized", isMinimized)
            putInt("widget_x", layoutParams?.x ?: 100)
            putInt("widget_y", layoutParams?.y ?: 100)
            if (!isMinimized) {
                putInt("widget_width", layoutParams?.width ?: 350)
                putInt("widget_height", layoutParams?.height ?: 300)
            }
            apply()
        }
    }
    
    private fun handleScreenshotClick() {
        android.util.Log.d("FloatingWidget", "Screenshot clicked")
        
        // Capture any pending text changes from EditText
        noteEditText?.text?.toString()?.let { text ->
            currentNoteContent = text
        }
        
        // Save current note content and get note ID
        ensureCurrentNoteExists()
        
        if (currentNoteId == -1L) {
            android.widget.Toast.makeText(this, "Error: Could not create note", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        android.util.Log.d("FloatingWidget", "Taking screenshot for note ID: $currentNoteId, content: ${currentNoteContent.take(50)}...")
        
        // Start MediaProjection permission activity
        val intent = MediaProjectionPermissionActivity.createIntent(this, currentNoteId)
        startActivity(intent)
    }
    
    private fun createNewNote() {
        // Save current note if it has content
        if (currentNoteContent.isNotBlank()) {
            saveCurrentNote()
        }
        
        // Clear the text field for new note
        currentNoteContent = ""
        noteEditText?.setText("")
        noteEditText?.hint = "Type your new note here..."
        
        android.util.Log.d("FloatingWidget", "Created new note")
        android.widget.Toast.makeText(this, "New note created", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun saveCurrentNote() {
        if (currentNoteContent.isNotBlank()) {
            // Save to SharedPreferences for now (later integrate with database)
            val timestamp = System.currentTimeMillis()
            sharedPreferences.edit().apply {
                putString("current_note_content", currentNoteContent)
                putLong("current_note_timestamp", timestamp)
                apply()
            }
            
            android.util.Log.d("FloatingWidget", "Note saved: ${currentNoteContent.take(50)}...")
            android.widget.Toast.makeText(this, "Note saved", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun ensureCurrentNoteExists() {
        if (currentNoteId == -1L) {
            // Create a new note ID based on timestamp
            val timestamp = System.currentTimeMillis()
            currentNoteId = timestamp
            
            // Save current note content if any
            if (currentNoteContent.isNotBlank()) {
                sharedPreferences.edit().apply {
                    putString("current_note_content", currentNoteContent)
                    putLong("current_note_timestamp", timestamp)
                    putLong("current_note_id", currentNoteId)
                    apply()
                }
            }
        } else {
            // Update existing note
            saveCurrentNote()
        }
    }
    
    private fun loadSavedNote() {
        currentNoteContent = sharedPreferences.getString("current_note_content", "") ?: ""
        currentNoteId = sharedPreferences.getLong("current_note_id", -1L)
        android.util.Log.d("FloatingWidget", "Loaded note: ${currentNoteContent.take(50)}...")
    }
    
    private fun handleNoteClick() {
        // This is now replaced by the inline editing, but keep for compatibility
        android.util.Log.d("FloatingWidget", "Note editing is now inline in the widget")
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
        
        // Unregister screenshot receiver
        screenshotReceiver?.let { receiver ->
            try {
                unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered
            }
        }
        
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