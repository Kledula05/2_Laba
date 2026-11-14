package com.ustenko.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Match::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao

    companion object {
        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val hasMode = db.query("PRAGMA table_info(matches)").use { columns ->
                    val nameIndex = columns.getColumnIndexOrThrow("name")
                    var found = false
                    while (columns.moveToNext()) {
                        if (columns.getString(nameIndex) == "mode") {
                            found = true
                            break
                        }
                    }
                    found
                }

                if (!hasMode) {
                    db.execSQL("ALTER TABLE matches ADD COLUMN mode TEXT NOT NULL DEFAULT 'DUO'")
                }
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "reflex_duel.db"
                ).addMigrations(migration1To2).build().also { instance = it }
            }
        }
    }
}
