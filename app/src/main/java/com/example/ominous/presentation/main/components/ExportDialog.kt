package com.example.ominous.presentation.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.ominous.domain.model.ExportFormat

@Composable
fun ExportDialog(
    selectedNoteIds: List<String>,
    onExport: (ExportFormat, Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.MARKDOWN) }
    var includeScreenshots by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export ${selectedNoteIds.size} note${if (selectedNoteIds.size > 1) "s" else ""}",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose export format:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Format selection
                ExportFormatOption(
                    format = ExportFormat.MARKDOWN,
                    icon = Icons.Default.Description,
                    title = "Markdown",
                    description = "Export as .md files with formatting",
                    selected = selectedFormat == ExportFormat.MARKDOWN,
                    onSelect = { selectedFormat = ExportFormat.MARKDOWN }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExportFormatOption(
                    format = ExportFormat.HTML,
                    icon = Icons.Default.Web,
                    title = "HTML",
                    description = "Export as web pages",
                    selected = selectedFormat == ExportFormat.HTML,
                    onSelect = { selectedFormat = ExportFormat.HTML }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExportFormatOption(
                    format = ExportFormat.PLAIN_TEXT,
                    icon = Icons.Default.Description,
                    title = "Plain Text",
                    description = "Export as simple text files",
                    selected = selectedFormat == ExportFormat.PLAIN_TEXT,
                    onSelect = { selectedFormat = ExportFormat.PLAIN_TEXT }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Screenshot inclusion option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = includeScreenshots,
                            onClick = { includeScreenshots = !includeScreenshots },
                            role = Role.Checkbox
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeScreenshots,
                        onCheckedChange = { includeScreenshots = it }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = "Include screenshots",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Embed or reference screenshot images",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onExport(selectedFormat, includeScreenshots) }
            ) {
                Icon(
                    imageVector = Icons.Default.GetApp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ExportFormatOption(
    format: ExportFormat,
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}