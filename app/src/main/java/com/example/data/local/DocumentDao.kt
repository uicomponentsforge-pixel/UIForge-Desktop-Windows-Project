package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM text_documents ORDER BY updatedAtMillis DESC")
    fun getAllDocuments(): Flow<List<TextDocumentEntity>>

    @Query("SELECT COUNT(*) FROM text_documents")
    suspend fun getDocumentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(document: TextDocumentEntity): Long

    @Query("DELETE FROM text_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("DELETE FROM text_documents")
    suspend fun clearAllDocuments()

    @Query("SELECT * FROM calendar_notes")
    fun getAllCalendarNotes(): Flow<List<CalendarNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCalendarNote(note: CalendarNoteEntity)

    @Query("DELETE FROM calendar_notes WHERE dateKey = :dateKey")
    suspend fun deleteCalendarNote(dateKey: String)

    @Query("DELETE FROM calendar_notes")
    suspend fun clearAllCalendarNotes()
}
