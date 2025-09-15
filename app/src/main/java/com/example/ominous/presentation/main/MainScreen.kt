package com.example.ominous.presentation.main

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ominous.data.database.entities.Note
import com.example.ominous.data.services.FloatingWidgetService
import com.example.ominous.domain.model.ExportFormat
import com.example.ominous.presentation.main.components.ExportDialog
import com.example.ominous.presentation.main.components.NoteCard
import com.example.ominous.presentation.main.components.PinnedNoteCard
import com.example.ominous.utils.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchTopBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onCloseSearch = { 
                        showSearchBar = false
                        viewModel.updateSearchQuery("")
                    }
                )
            } else {
                TopAppBar(
                    title = { 
                        Text(
                            text = if (uiState.selectedNotes.isNotEmpty()) {
                                "${uiState.selectedNotes.size} selected"
                            } else {
                                "Ominous"
                            }
                        )
                    },
                    actions = {
                        if (uiState.selectedNotes.isNotEmpty()) {
                            IconButton(onClick = { showExportDialog = true }) {
                                Icon(Icons.Default.GetApp, contentDescription = "Export")
                            }
                        } else {
                            IconButton(onClick = { showSearchBar = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Check overlay permission before launching floating widget
                    if (PermissionHelper.hasOverlayPermission(context)) {
                        // Permission granted, show floating overlay
                        val intent = Intent(context, FloatingWidgetService::class.java).apply {
                            action = FloatingWidgetService.ACTION_SHOW_FLOATING_WIDGET
                        }
                        context.startService(intent)
                    } else {
                        // Permission not granted, show explanation dialog
                        showOverlayPermissionDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Launch Floating Widget")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pinned notes horizontal scroll
            if (uiState.pinnedNotes.isNotEmpty()) {
                item {
                    Text(
                        text = "Pinned Notes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(uiState.pinnedNotes.size) { index ->
                            val note = uiState.pinnedNotes[index]
                            PinnedNoteCard(
                                note = note,
                                screenshotCount = uiState.noteScreenshots[note.id]?.size ?: 0,
                                onNoteClick = { viewModel.selectNote(note) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
            
            // Regular notes
            if (uiState.filteredNotes.isNotEmpty()) {
                item {
                    Text(
                        text = "All Notes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                val unpinnedNotes = uiState.filteredNotes.filter { !it.isPinned }
                items(unpinnedNotes.size) { index ->
                    val note = unpinnedNotes[index]
                    NoteCard(
                        note = note,
                        screenshots = uiState.noteScreenshots[note.id] ?: emptyList(),
                        isSelected = uiState.selectedNotes.contains(note.id),
                        onNoteClick = { viewModel.selectNote(note) },
                        onNoteLongClick = { viewModel.toggleNoteSelection(note.id) }
                    )
                }
            }
            
            if (uiState.filteredNotes.isEmpty() && uiState.pinnedNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) {
                                "No notes found for \"${uiState.searchQuery}\""
                            } else {
                                "No notes yet. Create your first note!"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
    
    if (showCreateDialog) {
        CreateNoteDialog(
            onDismiss = { showCreateDialog = false },
            onCreateNote = { content ->
                viewModel.createNote(content)
                showCreateDialog = false
            }
        )
    }
    
    if (showExportDialog) {
        ExportDialog(
            selectedNoteIds = uiState.selectedNotes.toList(),
            onExport = { format, includeScreenshots ->
                viewModel.exportNotes(format, includeScreenshots)
                showExportDialog = false
            },
            onDismiss = { showExportDialog = false }
        )
    }
    
    if (showOverlayPermissionDialog) {
        OverlayPermissionDialog(
            onGrantPermission = {
                showOverlayPermissionDialog = false
                PermissionHelper.requestOverlayPermission(context)
            },
            onDismiss = { showOverlayPermissionDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search notes...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseSearch) {
                Icon(Icons.Default.Clear, contentDescription = "Close search")
            }
        }
    )
}



@Composable
fun CreateNoteDialog(
    onDismiss: () -> Unit,
    onCreateNote: (String) -> Unit
) {
    var noteContent by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Note") },
        text = {
            OutlinedTextField(
                value = noteContent,
                onValueChange = { noteContent = it },
                label = { Text("Note content") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (noteContent.isNotBlank()) {
                        onCreateNote(noteContent)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OverlayPermissionDialog(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Overlay Permission Required") },
        text = {
            Text(
                "To use the floating note widget, Ominous needs permission to display over other apps. " +
                "This allows you to take notes and screenshots while using any application.\n\n" +
                "You'll be taken to Android settings to grant this permission."
            )
        },
        confirmButton = {
            TextButton(onClick = onGrantPermission) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}