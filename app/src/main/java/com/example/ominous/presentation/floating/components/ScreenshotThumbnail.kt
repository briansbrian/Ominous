package com.example.ominous.presentation.floating.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ominous.data.database.entities.Screenshot
import java.io.File

@Composable
fun ScreenshotThumbnail(
    screenshot: Screenshot,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (screenshot.thumbnailPath.isNotBlank() && File(screenshot.thumbnailPath).exists()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(screenshot.thumbnailPath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Screenshot thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback icon if thumbnail doesn't exist
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Screenshot",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}