package com.example.ominous.utils

import android.content.Context
import com.example.ominous.data.database.entities.Note
import com.example.ominous.data.database.entities.Screenshot
import com.example.ominous.domain.model.ExportFormat
import com.example.ominous.domain.model.ExportOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ExportHelper(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    fun exportNotes(
        notes: List<Note>,
        screenshots: Map<String, List<Screenshot>>,
        options: ExportOptions
    ): String {
        return when (options.format) {
            ExportFormat.MARKDOWN -> exportToMarkdown(notes, screenshots, options)
            ExportFormat.HTML -> exportToHtml(notes, screenshots, options)
            ExportFormat.PLAIN_TEXT -> exportToPlainText(notes, screenshots, options)
        }
    }
    
    private fun exportToMarkdown(
        notes: List<Note>,
        screenshots: Map<String, List<Screenshot>>,
        options: ExportOptions
    ): String {
        val builder = StringBuilder()
        
        builder.appendLine("# Ominous Notes Export")
        builder.appendLine()
        builder.appendLine("Exported on: ${dateFormat.format(Date())}")
        builder.appendLine()
        
        notes.forEach { note ->
            builder.appendLine("## Note")
            if (note.isPinned) {
                builder.appendLine("📌 **Pinned**")
            }
            builder.appendLine()
            builder.appendLine("**Created:** ${dateFormat.format(Date(note.createdAt))}")
            builder.appendLine("**Updated:** ${dateFormat.format(Date(note.updatedAt))}")
            builder.appendLine()
            builder.appendLine("### Content")
            builder.appendLine(note.content)
            builder.appendLine()
            
            // Add screenshots if available and requested
            if (options.includeImages) {
                val noteScreenshots = screenshots[note.id] ?: emptyList()
                if (noteScreenshots.isNotEmpty()) {
                    builder.appendLine("### Screenshots")
                    noteScreenshots.forEach { screenshot ->
                        builder.appendLine("![Screenshot](${screenshot.filePath})")
                    }
                    builder.appendLine()
                }
            }
            
            builder.appendLine("---")
            builder.appendLine()
        }
        
        return builder.toString()
    }
    
    private fun exportToHtml(
        notes: List<Note>,
        screenshots: Map<String, List<Screenshot>>,
        options: ExportOptions
    ): String {
        val builder = StringBuilder()
        
        builder.appendLine("<!DOCTYPE html>")
        builder.appendLine("<html>")
        builder.appendLine("<head>")
        builder.appendLine("    <title>Ominous Notes Export</title>")
        builder.appendLine("    <style>")
        builder.appendLine("        body { font-family: Arial, sans-serif; margin: 20px; }")
        builder.appendLine("        .note { border: 1px solid #ccc; padding: 15px; margin: 10px 0; }")
        builder.appendLine("        .pinned { background-color: #fff3cd; }")
        builder.appendLine("        .meta { color: #666; font-size: 0.9em; }")
        builder.appendLine("        .content { margin: 10px 0; white-space: pre-wrap; }")
        builder.appendLine("        .screenshot { max-width: 300px; margin: 5px; }")
        builder.appendLine("    </style>")
        builder.appendLine("</head>")
        builder.appendLine("<body>")
        builder.appendLine("    <h1>Ominous Notes Export</h1>")
        builder.appendLine("    <p>Exported on: ${dateFormat.format(Date())}</p>")
        
        notes.forEach { note ->
            val cssClass = if (note.isPinned) "note pinned" else "note"
            builder.appendLine("    <div class=\"$cssClass\">")
            if (note.isPinned) {
                builder.appendLine("        <div>📌 <strong>Pinned</strong></div>")
            }
            builder.appendLine("        <div class=\"meta\">")
            builder.appendLine("            Created: ${dateFormat.format(Date(note.createdAt))}<br>")
            builder.appendLine("            Updated: ${dateFormat.format(Date(note.updatedAt))}")
            builder.appendLine("        </div>")
            builder.appendLine("        <div class=\"content\">${note.content}</div>")
            
            if (options.includeImages) {
                val noteScreenshots = screenshots[note.id] ?: emptyList()
                if (noteScreenshots.isNotEmpty()) {
                    builder.appendLine("        <div>")
                    builder.appendLine("            <h4>Screenshots:</h4>")
                    noteScreenshots.forEach { screenshot ->
                        builder.appendLine("            <img src=\"${screenshot.filePath}\" class=\"screenshot\" alt=\"Screenshot\">")
                    }
                    builder.appendLine("        </div>")
                }
            }
            
            builder.appendLine("    </div>")
        }
        
        builder.appendLine("</body>")
        builder.appendLine("</html>")
        
        return builder.toString()
    }
    
    private fun exportToPlainText(
        notes: List<Note>,
        screenshots: Map<String, List<Screenshot>>,
        options: ExportOptions
    ): String {
        val builder = StringBuilder()
        
        builder.appendLine("OMINOUS NOTES EXPORT")
        builder.appendLine("===================")
        builder.appendLine()
        builder.appendLine("Exported on: ${dateFormat.format(Date())}")
        builder.appendLine()
        
        notes.forEachIndexed { index, note ->
            builder.appendLine("NOTE ${index + 1}")
            builder.appendLine("-".repeat(20))
            if (note.isPinned) {
                builder.appendLine("PINNED")
            }
            builder.appendLine("Created: ${dateFormat.format(Date(note.createdAt))}")
            builder.appendLine("Updated: ${dateFormat.format(Date(note.updatedAt))}")
            builder.appendLine()
            builder.appendLine("Content:")
            builder.appendLine(note.content)
            builder.appendLine()
            
            if (options.includeImages) {
                val noteScreenshots = screenshots[note.id] ?: emptyList()
                if (noteScreenshots.isNotEmpty()) {
                    builder.appendLine("Screenshots:")
                    noteScreenshots.forEach { screenshot ->
                        builder.appendLine("- ${screenshot.filePath}")
                    }
                    builder.appendLine()
                }
            }
            
            builder.appendLine()
        }
        
        return builder.toString()
    }
    
    fun saveToFile(content: String, filename: String): File {
        val exportsDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }
        
        val file = File(exportsDir, filename)
        file.writeText(content)
        return file
    }
}