package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "text_documents")
data class TextDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_notes")
data class CalendarNoteEntity(
    @PrimaryKey val dateKey: String, // Format: YYYY-MM-DD
    val note: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
