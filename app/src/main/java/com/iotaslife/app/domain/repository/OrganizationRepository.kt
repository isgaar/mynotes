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

