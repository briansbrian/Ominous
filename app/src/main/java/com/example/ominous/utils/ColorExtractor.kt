package com.example.ominous.utils

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import com.example.ominous.presentation.theme.OminousColors

object ColorExtractor {
    
    fun extractDominantColor(bitmap: Bitmap): androidx.compose.ui.graphics.Color {
        return try {
            val palette = Palette.from(bitmap).generate()
            val dominantColor = palette.getDominantColor(OminousColors.DeepMaroon.toArgb())
            androidx.compose.ui.graphics.Color(dominantColor)
        } catch (e: Exception) {
            OminousColors.DeepMaroon
        }
    }
    
    fun extractVibrantColor(bitmap: Bitmap): androidx.compose.ui.graphics.Color {
        return try {
            val palette = Palette.from(bitmap).generate()
            val vibrantColor = palette.getVibrantColor(OminousColors.RichGold.toArgb())
            androidx.compose.ui.graphics.Color(vibrantColor)
        } catch (e: Exception) {
            OminousColors.RichGold
        }
    }
    
    fun getAdaptiveBorderColor(backgroundColor: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
        // Calculate luminance to determine if we need a light or dark border
        val red = backgroundColor.red
        val green = backgroundColor.green
        val blue = backgroundColor.blue
        
        val luminance = 0.299 * red + 0.587 * green + 0.114 * blue
        
        return if (luminance > 0.5) {
            // Light background, use dark border
            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)
        } else {
            // Dark background, use light border
            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)
        }
    }
}