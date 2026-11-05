package com.iotaslife.app.domain.repository

import com.iotaslife.app.domain.model.BitacoraEntry
import kotlinx.coroutines.flow.Flow

interface BitacoraRepository {
    fun getEntriesForOrganization(orgId: Long): Flow<List<BitacoraEntry>>
    suspend fun getEntryById(id: Long): BitacoraEntry?
    fun searchEntries(orgId: Long, query: String): Flow<List<BitacoraEntry>>
    suspend fun insertEntry(entry: BitacoraEntry): Long
    suspend fun updateEntry(entry: BitacoraEntry)
    suspend fun deleteEntry(entry: BitacoraEntry)
    suspend fun deleteEntryById(id: Long)
}

