package com.example.thefilamentrack.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpoolDao {

    @Query("SELECT * FROM spools ORDER BY id DESC")
    fun getAllSpools(): Flow<List<SpoolEntity>>

    @Query("SELECT * FROM spools WHERE id = :id")
    suspend fun getSpoolById(id: Int): SpoolEntity?

    @Query("SELECT * FROM spools WHERE nfcTagId = :tagId LIMIT 1")
    suspend fun getSpoolByTag(tagId: String): SpoolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpool(spool: SpoolEntity): Long

    @Update
    suspend fun updateSpool(spool: SpoolEntity)

    @Delete
    suspend fun deleteSpool(spool: SpoolEntity)

    @Query("UPDATE spools SET nfcTagId = :tagId WHERE id = :spoolId")
    suspend fun updateNfcTag(spoolId: Int, tagId: String?)
}