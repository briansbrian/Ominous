package com.example.ominous.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    // Permission request codes
    const val REQUEST_OVERLAY_PERMISSION = 1001
    const val REQUEST_NOTIFICATION_PERMISSION = 1002
    const val REQUEST_STORAGE_PERMISSION = 1003
    
    // Required permissions
    private val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    private val NOTIFICATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }
    
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // Permission not required for older versions
        }
    }
    
    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            if (context is Activity) {
                context.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }
    
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required for older versions
        }
    }
    
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                NOTIFICATION_PERMISSIONS,
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }
    
    fun hasStoragePermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, we use scoped storage, so these permissions aren't needed
            true
        } else {
            STORAGE_PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
    
    fun requestStoragePermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                STORAGE_PERMISSIONS,
                REQUEST_STORAGE_PERMISSION
            )
        }
    }
    
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
    
    fun checkAllRequiredPermissions(context: Context): PermissionStatus {
        val hasOverlay = hasOverlayPermission(context)
        val hasNotification = hasNotificationPermission(context)
        val hasStorage = hasStoragePermissions(context)
        
        return PermissionStatus(
            hasOverlayPermission = hasOverlay,
            hasNotificationPermission = hasNotification,
            hasStoragePermissions = hasStorage,
            allGranted = hasOverlay && hasNotification && hasStorage
        )
    }
    
    data class PermissionStatus(
        val hasOverlayPermission: Boolean,
        val hasNotificationPermission: Boolean,
        val hasStoragePermissions: Boolean,
        val allGranted: Boolean
    )
}