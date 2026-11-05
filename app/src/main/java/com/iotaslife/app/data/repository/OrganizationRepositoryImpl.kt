// File: app/src/main/java/com/iotaslife/app/data/repository/OrganizationRepositoryImpl.kt
// Description: Repository implementation encapsulating OrganizationDao operations and mapping between Room entities and domain models.
// All Rights Reserved isgaar

package com.iotaslife.app.data.repository

import com.iotaslife.app.data.local.dao.OrganizationDao
import com.iotaslife.app.data.local.entity.OrganizationEntity
import com.iotaslife.app.domain.model.Organization
import com.iotaslife.app.domain.repository.OrganizationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrganizationRepositoryImpl(
    private val dao: OrganizationDao
) : OrganizationRepository {

    override fun getAllOrganizations(): Flow<List<Organization>> {
        return dao.getAllOrganizations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getOrganizationById(id: Long): Organization? {
        return dao.getOrganizationById(id)?.toDomain()
    }

    override suspend fun insertOrganization(organization: Organization): Long {
        return dao.insert(OrganizationEntity.fromDomain(organization))
    }

    override suspend fun updateOrganization(organization: Organization) {
        dao.update(OrganizationEntity.fromDomain(organization))
    }

    override suspend fun deleteOrganization(organization: Organization) {
        dao.delete(OrganizationEntity.fromDomain(organization))
    }
}

