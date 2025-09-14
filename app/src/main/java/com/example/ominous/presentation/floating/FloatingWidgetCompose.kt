package com.example.ominous.presentation.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ominous.presentation.floating.components.ExpandedWidget
import com.example.ominous.presentation.floating.components.FloatingIcon
import com.example.ominous.presentation.theme.OminousTheme

@Composable
fun FloatingWidgetContent(
    viewModel: FloatingWidgetViewModel = viewModel(),
    onScreenshotClick: () -> Unit,
    onNoteClick: () -> Unit,
    onMinimize: () -> Unit,
    onExpand: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    OminousTheme {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            if (uiState.isMinimized) {
                FloatingIcon(
                    onClick = onExpand,
                    modifier = Modifier.size(56.dp)
                )
            } else {
                ExpandedWidget(
                    currentNote = uiState.currentNote,
                    recentScreenshots = uiState.recentScreenshots,
                    onScreenshotClick = onScreenshotClick,
                    onNoteClick = onNoteClick,
                    onMinimize = onMinimize,
                    modifier = Modifier
                        .width(300.dp)
                        .height(200.dp)
                )
            }
        }
    }
}

fun createFloatingWidgetComposeView(
    viewModel: FloatingWidgetViewModel,
    onScreenshotClick: () -> Unit,
    onNoteClick: () -> Unit,
    onMinimize: () -> Unit,
    onExpand: () -> Unit
): ComposeView {
    return ComposeView(viewModel.context).apply {
        setContent {
            FloatingWidgetContent(
                viewModel = viewModel,
                onScreenshotClick = onScreenshotClick,
                onNoteClick = onNoteClick,
                onMinimize = onMinimize,
                onExpand = onExpand
            )
        }
    }
}