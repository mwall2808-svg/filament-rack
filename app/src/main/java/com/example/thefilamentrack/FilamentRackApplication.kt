package com.example.thefilamentrack

import android.app.Application
import androidx.room.Room
import com.example.thefilamentrack.data.AppDatabase
import com.example.thefilamentrack.data.SpoolRepository

class FilamentRackApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "filament-db").build()
    }
    val repository: SpoolRepository by lazy { SpoolRepository(database.spoolDao()) }
}
