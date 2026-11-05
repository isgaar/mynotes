package com.iotaslife.app.data.repository

import com.iotaslife.app.data.local.dao.BitacoraDao
import com.iotaslife.app.data.local.entity.BitacoraEntity
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.domain.repository.BitacoraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BitacoraRepositoryImpl(
    private val dao: BitacoraDao
) : BitacoraRepository {

    override fun getEntriesForOrganization(orgId: Long): Flow<List<BitacoraEntry>> {
        return dao.getEntriesForOrganization(orgId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getEntryById(id: Long): BitacoraEntry? {
        return dao.getEntryById(id)?.toDomain()
    }

    override fun searchEntries(orgId: Long, query: String): Flow<List<BitacoraEntry>> {
        return dao.searchEntries(orgId, query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertEntry(entry: BitacoraEntry): Long {
        return dao.insert(BitacoraEntity.fromDomain(entry))
    }

    override suspend fun updateEntry(entry: BitacoraEntry) {
        dao.update(BitacoraEntity.fromDomain(entry))
    }

    override suspend fun deleteEntry(entry: BitacoraEntry) {
        dao.delete(BitacoraEntity.fromDomain(entry))
    }

    override suspend fun deleteEntryById(id: Long) {
        dao.deleteById(id)
    }
}

