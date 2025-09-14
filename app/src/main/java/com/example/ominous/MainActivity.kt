package com.example.ominous

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ominous.presentation.theme.OminousTheme
import com.example.ominous.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OminousTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var hasOverlayPermission by remember { 
        mutableStateOf(PermissionHelper.hasOverlayPermission(context)) 
    }
    
    if (!hasOverlayPermission) {
        // Show permission request screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ominous - Setup") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Overlay permission is required for the floating widget",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text("Grant Permission")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Refresh permission status
                        hasOverlayPermission = PermissionHelper.hasOverlayPermission(context)
                    }
                ) {
                    Text("Check Permission")
                }
            }
        }
    } else {
        // Show main app screen
        com.example.ominous.presentation.main.MainScreen()
    }
}