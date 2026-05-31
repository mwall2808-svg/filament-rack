package com.example.thefilamentrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spools")
data class SpoolEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val brand: String = "",
    val material: String = "PLA",
    val colorName: String = "White",
    val colorHex: String = "#F0F0F0",
    val totalWeight: Float = 1000f,
    val remainingWeight: Float = 1000f,
    val notes: String = "",
    val nfcTagId: String? = null
)