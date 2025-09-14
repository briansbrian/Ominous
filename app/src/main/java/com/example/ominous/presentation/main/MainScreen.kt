package com.example.ominous.presentation.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ominous.data.database.entities.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val pinnedNotes by viewModel.pinnedNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedNotes by viewModel.selectedNotes.collectAsStateWithLifecycle()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ominous") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
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
            if (pinnedNotes.isNotEmpty()) {
                item {
                    Text(
                        text = "Pinned Notes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(pinnedNotes) { note ->
                    NoteCard(
                        note = note,
                        isSelected = selectedNotes.contains(note.id),
                        onToggleSelection = { viewModel.toggleNoteSelection(note.id) },
                        onTogglePin = { viewModel.togglePinStatus(note.id, !note.isPinned) }
                    )
                }
            }
            
            if (notes.isNotEmpty()) {
                item {
                    Text(
                        text = "All Notes",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(notes.filter { !it.isPinned }) { note ->
                    NoteCard(
                        note = note,
                        isSelected = selectedNotes.contains(note.id),
                        onToggleSelection = { viewModel.toggleNoteSelection(note.id) },
                        onTogglePin = { viewModel.togglePinStatus(note.id, !note.isPinned) }
                    )
                }
            }
            
            if (notes.isEmpty() && pinnedNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notes yet. Create your first note!",
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
}

@Composable
fun NoteCard(
    note: Note,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onTogglePin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Created: ${java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date(note.createdAt))}",
                    style = MaterialTheme.typography.labelSmall
                )
                if (note.isPinned) {
                    Text(
                        text = "📌",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
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