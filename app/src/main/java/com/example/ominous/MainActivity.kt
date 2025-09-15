package com.example.ominous

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.ominous.data.services.FloatingWidgetService
import com.example.ominous.presentation.theme.OminousTheme
import com.example.ominous.utils.Constants
import com.example.ominous.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkAndStartServices()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        createNotificationChannel()
        requestRequiredPermissions()
        
        setContent {
            OminousTheme {
                PermissionAwareMainScreen(
                    onPermissionsGranted = { checkAndStartServices() }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check permissions again when returning from settings
        checkAndStartServices()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for Ominous floating widget and services"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Check notification permission
        if (!PermissionHelper.hasNotificationPermission(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Check storage permissions
        if (!PermissionHelper.hasStoragePermissions(this)) {
            permissionsToRequest.addAll(listOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            checkOverlayPermission()
        }
    }
    
    private fun checkOverlayPermission() {
        if (!PermissionHelper.hasOverlayPermission(this)) {
            // This will be handled by the UI
            return
        }
        checkAndStartServices()
    }
    
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            checkOverlayPermission()
        }
        // If not all granted, the UI will show the permission request screen
    }
    
    private fun checkAndStartServices() {
        lifecycleScope.launch {
            val permissionStatus = PermissionHelper.checkAllRequiredPermissions(this@MainActivity)
            if (permissionStatus.allGranted) {
                startFloatingWidgetService()
            }
        }
    }
    
    private fun startFloatingWidgetService() {
        val intent = Intent(this, FloatingWidgetService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PermissionHelper.REQUEST_NOTIFICATION_PERMISSION,
            PermissionHelper.REQUEST_STORAGE_PERMISSION -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                if (allGranted) {
                    checkOverlayPermission()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionAwareMainScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var permissionStatus by remember { 
        mutableStateOf(PermissionHelper.checkAllRequiredPermissions(context))
    }
    
    LaunchedEffect(Unit) {
        // Refresh permission status periodically
        permissionStatus = PermissionHelper.checkAllRequiredPermissions(context)
    }
    
    if (!permissionStatus.allGranted) {
        // Show permission request screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ominous - Setup Required") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Ominous needs the following permissions to work properly:",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                PermissionCard(
                    title = "Overlay Permission",
                    description = "Required for the floating widget to appear over other apps",
                    isGranted = permissionStatus.hasOverlayPermission,
                    onRequest = {
                        PermissionHelper.requestOverlayPermission(context)
                    }
                )
                
                PermissionCard(
                    title = "Notification Permission",
                    description = "Required for service notifications",
                    isGranted = permissionStatus.hasNotificationPermission,
                    onRequest = {
                        if (context is ComponentActivity) {
                            PermissionHelper.requestNotificationPermission(context)
                        }
                    }
                )
                
                PermissionCard(
                    title = "Storage Permission",
                    description = "Required to save screenshots and export notes",
                    isGranted = permissionStatus.hasStoragePermissions,
                    onRequest = {
                        if (context is ComponentActivity) {
                            PermissionHelper.requestStoragePermissions(context)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            permissionStatus = PermissionHelper.checkAllRequiredPermissions(context)
                            if (permissionStatus.allGranted) {
                                onPermissionsGranted()
                            }
                        }
                    ) {
                        Text("Check Permissions")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            PermissionHelper.openAppSettings(context)
                        }
                    ) {
                        Text("Open Settings")
                    }
                }
            }
        }
    } else {
        // Show main app screen
        LaunchedEffect(permissionStatus.allGranted) {
            onPermissionsGranted()
        }
        com.example.ominous.presentation.main.MainScreen()
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isGranted) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isGranted) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        }
                    )
                }
                
                if (isGranted) {
                    Text(
                        text = "✓ Granted",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Button(
                        onClick = onRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Grant")
                    }
                }
            }
        }
    }
}