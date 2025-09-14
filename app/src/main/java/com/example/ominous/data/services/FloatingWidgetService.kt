package com.example.ominous.data.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.ominous.R
import com.example.ominous.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FloatingWidgetService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FLOATING_WIDGET -> startFloatingWidget()
            ACTION_STOP_FLOATING_WIDGET -> stopFloatingWidget()
        }
        return START_STICKY
    }
    
    private fun startFloatingWidget() {
        if (floatingView != null) return
        
        startForeground(
            Constants.FLOATING_WIDGET_NOTIFICATION_ID,
            createNotification()
        )
        
        // Create floating view (placeholder for now)
        floatingView = LayoutInflater.from(this).inflate(
            android.R.layout.simple_list_item_1, null
        )
        
        val params = WindowManager.LayoutParams(
            Constants.FLOATING_WIDGET_DEFAULT_WIDTH,
            Constants.FLOATING_WIDGET_DEFAULT_HEIGHT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100
        
        windowManager?.addView(floatingView, params)
    }
    
    private fun stopFloatingWidget() {
        floatingView?.let { view ->
            windowManager?.removeView(view)
            floatingView = null
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.FLOATING_WIDGET_CHANNEL_ID,
                "Floating Widget",
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
        val stopIntent = Intent(this, FloatingWidgetService::class.java).apply {
            action = ACTION_STOP_FLOATING_WIDGET
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, Constants.FLOATING_WIDGET_CHANNEL_ID)
            .setContentTitle("Ominous Floating Widget")
            .setContentText("Tap to take notes and screenshots")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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
        stopFloatingWidget()
    }
    
    companion object {
        const val ACTION_START_FLOATING_WIDGET = "START_FLOATING_WIDGET"
        const val ACTION_STOP_FLOATING_WIDGET = "STOP_FLOATING_WIDGET"
    }
}