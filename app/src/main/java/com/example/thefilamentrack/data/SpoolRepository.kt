package com.example.thefilamentrack.data

import kotlinx.coroutines.flow.Flow

class SpoolRepository(private val dao: SpoolDao) {

    val allSpools: Flow<List<SpoolEntity>> = dao.getAllSpools()

    suspend fun insert(spool: SpoolEntity) = dao.insertSpool(spool)

    suspend fun update(spool: SpoolEntity) = dao.updateSpool(spool)

    suspend fun delete(spool: SpoolEntity) = dao.deleteSpool(spool)

    suspend fun getSpoolById(id: Int): SpoolEntity? = dao.getSpoolById(id)

    suspend fun getSpoolByTag(tagId: String): SpoolEntity? = dao.getSpoolByTag(tagId)

    suspend fun updateNfcTag(spoolId: Int, tagId: String?) = dao.updateNfcTag(spoolId, tagId)
}