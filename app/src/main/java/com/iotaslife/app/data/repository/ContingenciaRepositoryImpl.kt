// File: app/src/main/java/com/iotaslife/app/data/repository/ContingenciaRepositoryImpl.kt
// Description: Repository implementation managing mapping between contingency Room entities and domain models.
// All Rights Reserved isgaar

package com.iotaslife.app.data.repository

import com.iotaslife.app.data.local.dao.ContingenciaDao
import com.iotaslife.app.data.local.entity.ContingenciaEntity
import com.iotaslife.app.domain.model.ContingenciaEntry
import com.iotaslife.app.domain.repository.ContingenciaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContingenciaRepositoryImpl(
    private val dao: ContingenciaDao
) : ContingenciaRepository {

    override fun getContingenciasForOrganization(organizationId: Long): Flow<List<ContingenciaEntry>> {
        return dao.getContingenciasForOrganization(organizationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getContingenciaById(id: Long): ContingenciaEntry? {
        return dao.getContingenciaById(id)?.toDomain()
    }

    override suspend fun insertContingencia(entry: ContingenciaEntry): Long {
        return dao.insert(ContingenciaEntity.fromDomain(entry))
    }

    override suspend fun updateContingencia(entry: ContingenciaEntry) {
        dao.update(ContingenciaEntity.fromDomain(entry))
    }

    override suspend fun deleteContingencia(entry: ContingenciaEntry) {
        dao.delete(ContingenciaEntity.fromDomain(entry))
    }

    override suspend fun deleteContingenciaById(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun toggleReimbursed(id: Long, isReimbursed: Boolean) {
        dao.toggleReimbursed(id, isReimbursed, System.currentTimeMillis())
    }
}

