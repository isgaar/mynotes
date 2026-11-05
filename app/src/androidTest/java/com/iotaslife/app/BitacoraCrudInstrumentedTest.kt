package com.iotaslife.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iotaslife.app.data.local.AppDatabase
import com.iotaslife.app.data.repository.BitacoraRepositoryImpl
import com.iotaslife.app.data.repository.OrganizationRepositoryImpl
import com.iotaslife.app.data.repository.TaskRepositoryImpl
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.domain.model.BitacoraStatus
import com.iotaslife.app.domain.model.Organization
import com.iotaslife.app.domain.model.TaskItem
import com.iotaslife.app.domain.model.TaskPriority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BitacoraCrudInstrumentedTest {

    private lateinit var database: AppDatabase
    private lateinit var orgRepo: OrganizationRepositoryImpl
    private lateinit var bitacoraRepo: BitacoraRepositoryImpl
    private lateinit var taskRepo: TaskRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        orgRepo = OrganizationRepositoryImpl(database.organizationDao())
        bitacoraRepo = BitacoraRepositoryImpl(database.bitacoraDao())
        taskRepo = TaskRepositoryImpl(database.taskDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCompleteBitacoraLifecycle_Save_Edit_Delete() = runBlocking {
        // 1. Crear Organización
        val org = Organization(
            name = "Tech Solutions Corp",
            description = "Departamento de DevOps y Cloud",
            iconEmoji = "🚀",
            colorHex = "#2B6CB0"
        )
        val orgId = orgRepo.insertOrganization(org)
        assertTrue("Organization ID must be > 0", orgId > 0)

        val retrievedOrg = orgRepo.getOrganizationById(orgId)
        assertNotNull(retrievedOrg)
        assertEquals("Tech Solutions Corp", retrievedOrg?.name)

        // 2. PRUEBA DE GUARDADO (Creación de nota / bitácora)
        val initialEntry = BitacoraEntry(
            organizationId = orgId,
            ticketCode = "TIK-101",
            title = "Migración de Base de Datos PostgreSQL a Cloud SQL",
            dateEpochMillis = System.currentTimeMillis(),
            workDone = "Se realizó el dump de la base de datos de producción y se restauró en la nueva instancia Cloud SQL.",
            observations = "El proceso tomó 45 minutos. Hubo una latencia leve durante el cutover.",
            actionsTaken = "1. Ejecución de pg_dump con compresión.\n2. Verificación de checksums.\n3. Actualización de connection pool.",
            status = BitacoraStatus.IN_PROGRESS
        )

        val savedId = bitacoraRepo.insertEntry(initialEntry)
        assertTrue("Saved ID should be valid", savedId > 0)

        // Verificar que se guardó correctamente
        var entries = bitacoraRepo.getEntriesForOrganization(orgId).first()
        assertEquals(1, entries.size)
        val savedEntry = entries.first()
        assertEquals("TIK-101", savedEntry.ticketCode)
        assertEquals("Migración de Base de Datos PostgreSQL a Cloud SQL", savedEntry.title)
        assertEquals(BitacoraStatus.IN_PROGRESS, savedEntry.status)
        assertEquals(
            "Se realizó el dump de la base de datos de producción y se restauró en la nueva instancia Cloud SQL.",
            savedEntry.workDone
        )

        // 3. PRUEBA DE EDICIÓN (Actualización de nota / bitácora)
        val updatedEntry = savedEntry.copy(
            title = "Migración de Base de Datos PostgreSQL a Cloud SQL [FINALIZADA CON ÉXITO]",
            status = BitacoraStatus.COMPLETED,
            actionsTaken = savedEntry.actionsTaken + "\n4. Pruebas de estrés y validación de endpoints HTTP pasaron al 100%."
        )
        bitacoraRepo.updateEntry(updatedEntry)

        // Verificar que los cambios se reflejen
        val retrievedAfterEdit = bitacoraRepo.getEntryById(savedId)
        assertNotNull(retrievedAfterEdit)
        assertEquals(
            "Migración de Base de Datos PostgreSQL a Cloud SQL [FINALIZADA CON ÉXITO]",
            retrievedAfterEdit?.title
        )
        assertEquals(BitacoraStatus.COMPLETED, retrievedAfterEdit?.status)
        assertTrue(
            retrievedAfterEdit?.actionsTaken?.contains("Pruebas de estrés") == true
        )

        // 4. PRUEBA DE ELIMINACIÓN (Delete de nota / bitácora)
        bitacoraRepo.deleteEntryById(savedId)

        // Verificar que ya no existe
        val deletedEntry = bitacoraRepo.getEntryById(savedId)
        assertNull("Entry should be null after deletion", deletedEntry)

        entries = bitacoraRepo.getEntriesForOrganization(orgId).first()
        assertTrue("Entries list should be empty after delete", entries.isEmpty())
    }

    @Test
    fun testTasksLifecycle_Create_ToggleCompleted_Delete() = runBlocking {
        // Crear Organización
        val orgId = orgRepo.insertOrganization(
            Organization(name = "Fintech Org", description = "Finanzas", iconEmoji = "💼")
        )

        // 1. Crear Pendiente
        val task = TaskItem(
            organizationId = orgId,
            title = "Configurar alertas de monitoreo en Prometheus",
            description = "Añadir umbral de 85% de uso de CPU en servidores",
            priority = TaskPriority.HIGH,
            isCompleted = false
        )
        val taskId = taskRepo.insertTask(task)
        assertTrue(taskId > 0)

        var tasks = taskRepo.getTasksForOrganization(orgId).first()
        assertEquals(1, tasks.size)
        assertEquals(false, tasks[0].isCompleted)
        assertEquals(TaskPriority.HIGH, tasks[0].priority)

        // 2. Marcar como finalizado (toggle)
        taskRepo.toggleTaskCompleted(taskId, true)
        val completedTask = taskRepo.getTaskById(taskId)
        assertEquals(true, completedTask?.isCompleted)

        // 3. Eliminar pendiente
        taskRepo.deleteTaskById(taskId)
        val tasksAfterDelete = taskRepo.getTasksForOrganization(orgId).first()
        assertTrue(tasksAfterDelete.isEmpty())
    }
}

