package com.example.ominous.domain.model

enum class ExportFormat {
    MARKDOWN,
    HTML,
    PLAIN_TEXT
}

data class ExportOptions(
    val format: ExportFormat,
    val includeImages: Boolean,
    val dateRange: Pair<Long, Long>? = null
)