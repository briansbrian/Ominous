package com.example.ominous.presentation.theme

import androidx.compose.ui.graphics.Color

object OminousColors {
    val DeepMaroon = Color(0xFF8B1538)
    val RichGold = Color(0xFFD4AF37)
    val DarkCharcoal = Color(0xFF2C2C2C)
    val WarmBrown = Color(0xFF3C2E26)
    val CreamGold = Color(0xFFF5E6D3)
    val BrightGold = Color(0xFFFFD700)
    
    // Adaptive colors for floating widget
    val FloatingBackground = Color.White
    val FloatingBorderDefault = Color(0xFFCCCCCC)
    
    // Light theme colors
    val LightPrimary = DeepMaroon
    val LightOnPrimary = Color.White
    val LightSecondary = RichGold
    val LightOnSecondary = DarkCharcoal
    val LightBackground = CreamGold
    val LightOnBackground = DarkCharcoal
    val LightSurface = Color.White
    val LightOnSurface = DarkCharcoal
    
    // Dark theme colors
    val DarkPrimary = RichGold
    val DarkOnPrimary = DarkCharcoal
    val DarkSecondary = BrightGold
    val DarkOnSecondary = DarkCharcoal
    val DarkBackground = DarkCharcoal
    val DarkOnBackground = CreamGold
    val DarkSurface = WarmBrown
    val DarkOnSurface = CreamGold
}