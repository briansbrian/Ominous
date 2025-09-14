package com.example.ominous.domain.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.ominous.data.database.entities.Note
import com.example.ominous.data.database.entities.Screenshot

data class NoteWithScreenshots(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val screenshots: List<Screenshot>
)