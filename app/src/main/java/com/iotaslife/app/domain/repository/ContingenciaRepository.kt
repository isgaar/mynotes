// File: app/src/main/java/com/iotaslife/app/domain/repository/ContingenciaRepository.kt
// Description: Repository contract defining reactive querying, creation, mutation, and deletion of contingency entries.
// All Rights Reserved isgaar

package com.iotaslife.app.domain.repository

import com.iotaslife.app.domain.model.ContingenciaEntry
import kotlinx.coroutines.flow.Flow

interface ContingenciaRepository {
    fun getContingenciasForOrganization(organizationId: Long): Flow<List<ContingenciaEntry>>
    suspend fun getContingenciaById(id: Long): ContingenciaEntry?
    suspend fun insertContingencia(entry: ContingenciaEntry): Long
    suspend fun updateContingencia(entry: ContingenciaEntry)
    suspend fun deleteContingencia(entry: ContingenciaEntry)
    suspend fun deleteContingenciaById(id: Long)
    suspend fun toggleReimbursed(id: Long, isReimbursed: Boolean)
}

