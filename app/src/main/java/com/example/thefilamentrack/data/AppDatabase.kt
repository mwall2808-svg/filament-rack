package com.example.thefilamentrack.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SpoolEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun spoolDao(): SpoolDao
}