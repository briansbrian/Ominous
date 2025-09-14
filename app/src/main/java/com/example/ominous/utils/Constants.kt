package com.example.ominous.utils

object Constants {
    // Notification IDs
    const val FLOATING_WIDGET_NOTIFICATION_ID = 1001
    const val CLIPBOARD_SERVICE_NOTIFICATION_ID = 1002
    const val SCREENSHOT_SERVICE_NOTIFICATION_ID = 1003
    
    // Notification Channels
    const val NOTIFICATION_CHANNEL_ID = "ominous_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Ominous Notifications"
    
    // Shared Preferences
    const val PREFS_NAME = "ominous_prefs"
    const val PREF_WIDGET_X = "widget_x"
    const val PREF_WIDGET_Y = "widget_y"
    const val PREF_WIDGET_WIDTH = "widget_width"
    const val PREF_WIDGET_HEIGHT = "widget_height"
    const val PREF_WIDGET_MINIMIZED = "widget_minimized"
    
    // File paths
    const val SCREENSHOTS_DIR = "screenshots"
    const val THUMBNAILS_DIR = "thumbnails"
    
    // Screenshot settings
    const val SCREENSHOT_QUALITY = 80
    const val THUMBNAIL_SIZE = 200
    
    // UI Constants
    const val FLOATING_WIDGET_MIN_WIDTH = 200
    const val FLOATING_WIDGET_MIN_HEIGHT = 150
    const val FLOATING_WIDGET_DEFAULT_WIDTH = 300
    const val FLOATING_WIDGET_DEFAULT_HEIGHT = 200
    
    // Auto-minimize delay (milliseconds)
    const val AUTO_MINIMIZE_DELAY = 5000L
    
    // Permission request codes
    const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
    const val SCREENSHOT_PERMISSION_REQUEST_CODE = 1002
}