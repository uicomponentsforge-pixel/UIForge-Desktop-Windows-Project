package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TextDocumentEntity::class, CalendarNoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DesktopDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: DesktopDatabase? = null

        fun getInstance(context: Context): DesktopDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DesktopDatabase::class.java,
                    "uiforge_desktop.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
