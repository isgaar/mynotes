// File: app/src/main/java/com/iotaslife/app/IotasLifeApp.kt
// Description: Application class providing manual dependency injection container, database seeding, and workspace profile management.
// All Rights Reserved isgaar

package com.iotaslife.app

import android.app.Application
import com.iotaslife.app.data.local.AppDatabase
import com.iotaslife.app.data.repository.BitacoraRepositoryImpl
import com.iotaslife.app.data.repository.ContingenciaRepositoryImpl
import com.iotaslife.app.data.repository.OrganizationRepositoryImpl
import com.iotaslife.app.data.repository.TaskRepositoryImpl
import com.iotaslife.app.domain.model.Organization
import com.iotaslife.app.domain.repository.BitacoraRepository
import com.iotaslife.app.domain.repository.ContingenciaRepository
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
    val contingenciaRepository: ContingenciaRepository
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

    override val contingenciaRepository: ContingenciaRepository by lazy {
        ContingenciaRepositoryImpl(database.contingenciaDao())
    }
}

class IotasLifeApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)

        val appScope = CoroutineScope(Dispatchers.IO)
        appScope.launch {
            try {
                val orgs = container.organizationRepository.getAllOrganizations().first()

                // Remove "Empresa Principal" if it exists, preserving all user data in Cianca
                val empresaPrincipal = orgs.find {
                    it.name.trim().equals("Empresa Principal", ignoreCase = true)
                }
                if (empresaPrincipal != null) {
                    container.organizationRepository.deleteOrganization(empresaPrincipal)
                }

                // Ensure "Hogar" profile exists as requested for home pending tasks
                val currentOrgs = container.organizationRepository.getAllOrganizations().first()
                val hogarExists = currentOrgs.any {
                    it.name.trim().equals("Hogar", ignoreCase = true)
                }
                if (!hogarExists) {
                    container.organizationRepository.insertOrganization(
                        Organization(
                            name = "Hogar",
                            description = "Lista de tareas pendientes del hogar",
                            iconEmoji = "CASA",
                            colorHex = "#2E7D32"
                        )
                    )
                }

                // If completely empty on fresh install, ensure Cianca exists
                val finalOrgs = container.organizationRepository.getAllOrganizations().first()
                val ciancaExists = finalOrgs.any {
                    it.name.trim().equals("Cianca", ignoreCase = true)
                }
                if (!ciancaExists && finalOrgs.none { !it.name.trim().equals("Hogar", ignoreCase = true) }) {
                    container.organizationRepository.insertOrganization(
                        Organization(
                            name = "Cianca",
                            description = "empresa donde le represento a daytek",
                            iconEmoji = "CORP",
                            colorHex = "#333333"
                        )
                    )
                }
            } catch (_: Exception) {
            }
        }
    }
}
