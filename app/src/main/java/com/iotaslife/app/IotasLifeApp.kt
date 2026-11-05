// File: app/src/main/java/com/iotaslife/app/IotasLifeApp.kt
// Description: Application class providing manual dependency injection container and initial database seeding for the default organization.
// All Rights Reserved isgaar

package com.iotaslife.app

import android.app.Application
import com.iotaslife.app.data.local.AppDatabase
import com.iotaslife.app.data.repository.BitacoraRepositoryImpl
import com.iotaslife.app.data.repository.OrganizationRepositoryImpl
import com.iotaslife.app.data.repository.TaskRepositoryImpl
import com.iotaslife.app.domain.model.Organization
import com.iotaslife.app.domain.repository.BitacoraRepository
import com.iotaslife.app.domain.repository.OrganizationRepository
import com.iotaslife.app.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface AppContainer {
    val organizationRepository: OrganizationRepository
    val bitacoraRepository: BitacoraRepository
    val taskRepository: TaskRepository
}

class AppContainerImpl(private val application: Application) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(application)
    }

    override val organizationRepository: OrganizationRepository by lazy {
        OrganizationRepositoryImpl(database.organizationDao())
    }

    override val bitacoraRepository: BitacoraRepository by lazy {
        BitacoraRepositoryImpl(database.bitacoraDao())
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(database.taskDao())
    }
}

class IotasLifeApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)

        // Seed a default organization if empty so user has immediate context
        val appScope = CoroutineScope(Dispatchers.IO)
        appScope.launch {
            try {
                val orgs = container.organizationRepository.getAllOrganizations().first()
                if (orgs.isEmpty()) {
                    container.organizationRepository.insertOrganization(
                        Organization(
                            name = "Empresa Principal",
                            description = "Operaciones de desarrollo & sistemas",
                            iconEmoji = "CORP",
                            colorHex = "#2B6CB0"
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }
    }
}
