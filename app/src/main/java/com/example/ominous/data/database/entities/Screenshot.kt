package com.example.ominous.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "screenshots",
    foreignKeys = [ForeignKey(
        entity = Note::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["noteId"])]
)
data class Screenshot(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val noteId: String,
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null
)