package com.example.data.local

import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val dao: DocumentDao) {
    val allDocuments: Flow<List<TextDocumentEntity>> = dao.getAllDocuments()
    val allCalendarNotes: Flow<List<CalendarNoteEntity>> = dao.getAllCalendarNotes()

    suspend fun ensureSeededWelcomeNote() {
        if (dao.getDocumentCount() == 0) {
            dao.upsertDocument(
                TextDocumentEntity(
                    title = "Welcome to UIForge.txt",
                    content = """
                        UIForge Desktop — Local Workspace
                        ==================================

                        Welcome! You are running a native Kotlin & Jetpack Compose desktop environment directly on Android.

                        Quick Tips:
                        • Drag any window title bar to reposition it across the workspace.
                        • Double-tap a title bar or press □ to maximize or restore.
                        • Long-press the desktop background or any desktop icon for context actions.
                        • Use the Start menu or taskbar to switch between open & minimized windows.
                        • All documents and personalization settings are saved locally offline.
                    """.trimIndent()
                )
            )
        }
    }

    suspend fun saveDocument(id: Long = 0L, title: String, content: String): Long {
        val cleanTitle = title.trim().ifEmpty { "Untitled.txt" }
        return dao.upsertDocument(
            TextDocumentEntity(
                id = id,
                title = cleanTitle,
                content = content,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteDocument(id: Long) {
        dao.deleteDocumentById(id)
    }

    suspend fun saveCalendarNote(dateKey: String, note: String) {
        val trimmed = note.trim()
        if (trimmed.isEmpty()) {
            dao.deleteCalendarNote(dateKey)
        } else {
            dao.upsertCalendarNote(
                CalendarNoteEntity(
                    dateKey = dateKey,
                    note = trimmed,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun clearAllUserData() {
        dao.clearAllDocuments()
        dao.clearAllCalendarNotes()
        ensureSeededWelcomeNote()
    }
}
