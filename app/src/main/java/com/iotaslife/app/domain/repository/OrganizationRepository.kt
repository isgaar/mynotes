// File: app/src/main/java/com/iotaslife/app/domain/repository/OrganizationRepository.kt
// Description: Repository contract defining reactive retrieval, creation, updating, and deletion of workspace organizations.
// All Rights Reserved isgaar

package com.iotaslife.app.domain.repository

import com.iotaslife.app.domain.model.Organization
import kotlinx.coroutines.flow.Flow

interface OrganizationRepository {
    fun getAllOrganizations(): Flow<List<Organization>>
    suspend fun getOrganizationById(id: Long): Organization?
    suspend fun insertOrganization(organization: Organization): Long
    suspend fun updateOrganization(organization: Organization)
    suspend fun deleteOrganization(organization: Organization)
}

