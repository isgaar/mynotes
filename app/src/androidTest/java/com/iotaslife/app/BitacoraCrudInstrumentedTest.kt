// File: app/src/androidTest/java/com/iotaslife/app/BitacoraCrudInstrumentedTest.kt
// Description: Automated Android instrumented test suite validating Room database operations, lifecycle CRUD, tasks, and image evidence persistence.
// All Rights Reserved isgaar

package com.iotaslife.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iotaslife.app.data.local.AppDatabase
import com.iotaslife.app.data.repository.BitacoraRepositoryImpl
import com.iotaslife.app.data.repository.ContingenciaRepositoryImpl
import com.iotaslife.app.data.repository.OrganizationRepositoryImpl
import com.iotaslife.app.data.repository.TaskRepositoryImpl
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.domain.model.BitacoraStatus
import com.iotaslife.app.domain.model.ContingenciaEntry
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
    private lateinit var contingenciaRepo: ContingenciaRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        orgRepo = OrganizationRepositoryImpl(database.organizationDao())
        bitacoraRepo = BitacoraRepositoryImpl(database.bitacoraDao())
        taskRepo = TaskRepositoryImpl(database.taskDao())
        contingenciaRepo = ContingenciaRepositoryImpl(database.contingenciaDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCompleteBitacoraLifecycle_Save_Edit_Delete() = runBlocking {
        // 1. Crear Organizacion
        val org = Organization(
            name = "Tech Solutions Corp",
            description = "Departamento de DevOps y Cloud",
            iconEmoji = "DEV",
            colorHex = "#2B6CB0"
        )
        val orgId = orgRepo.insertOrganization(org)
        assertTrue("Organization ID must be > 0", orgId > 0)

        val retrievedOrg = orgRepo.getOrganizationById(orgId)
        assertNotNull(retrievedOrg)
        assertEquals("Tech Solutions Corp", retrievedOrg?.name)

        // 2. PRUEBA DE GUARDADO (Creacion de nota / bitacora)
        val initialEntry = BitacoraEntry(
            organizationId = orgId,
            ticketCode = "TIK-101",
            title = "Migracion de Base de Datos PostgreSQL a Cloud SQL",
            dateEpochMillis = System.currentTimeMillis(),
            workDone = "Se realizo el dump de la base de datos de produccion y se restauro en la nueva instancia Cloud SQL.",
            observations = "El proceso tomo 45 minutos. Hubo una latencia leve durante el cutover.",
            actionsTaken = "1. Ejecucion de pg_dump con compresion.\n2. Verificacion de checksums.\n3. Actualizacion de connection pool.",
            status = BitacoraStatus.IN_PROGRESS
        )

        val savedId = bitacoraRepo.insertEntry(initialEntry)
        assertTrue("Saved ID should be valid", savedId > 0)

        // Verificar que se guardo correctamente
        var entries = bitacoraRepo.getEntriesForOrganization(orgId).first()
        assertEquals(1, entries.size)
        val savedEntry = entries.first()
        assertEquals("TIK-101", savedEntry.ticketCode)
        assertEquals("Migracion de Base de Datos PostgreSQL a Cloud SQL", savedEntry.title)
        assertEquals(BitacoraStatus.IN_PROGRESS, savedEntry.status)
        assertEquals(
            "Se realizo el dump de la base de datos de produccion y se restauro en la nueva instancia Cloud SQL.",
            savedEntry.workDone
        )

        // 3. PRUEBA DE EDICION (Actualizacion de nota / bitacora)
        val updatedEntry = savedEntry.copy(
            title = "Migracion de Base de Datos PostgreSQL a Cloud SQL [FINALIZADA CON EXITO]",
            status = BitacoraStatus.COMPLETED,
            actionsTaken = savedEntry.actionsTaken + "\n4. Pruebas de estres y validacion de endpoints HTTP pasaron al 100%."
        )
        bitacoraRepo.updateEntry(updatedEntry)

        // Verificar que los cambios se reflejen
        val retrievedAfterEdit = bitacoraRepo.getEntryById(savedId)
        assertNotNull(retrievedAfterEdit)
        assertEquals(
            "Migracion de Base de Datos PostgreSQL a Cloud SQL [FINALIZADA CON EXITO]",
            retrievedAfterEdit?.title
        )
        assertEquals(BitacoraStatus.COMPLETED, retrievedAfterEdit?.status)
        assertTrue(
            retrievedAfterEdit?.actionsTaken?.contains("Pruebas de estres") == true
        )

        // 4. PRUEBA DE ELIMINACION (Delete de nota / bitacora)
        bitacoraRepo.deleteEntryById(savedId)

        // Verificar que ya no existe
        val deletedEntry = bitacoraRepo.getEntryById(savedId)
        assertNull("Entry should be null after deletion", deletedEntry)

        entries = bitacoraRepo.getEntriesForOrganization(orgId).first()
        assertTrue("Entries list should be empty after delete", entries.isEmpty())
    }

    @Test
    fun testTasksLifecycle_Create_ToggleCompleted_Delete() = runBlocking {
        // Crear Organizacion
        val orgId = orgRepo.insertOrganization(
            Organization(name = "Fintech Org", description = "Finanzas", iconEmoji = "FIN")
        )

        // 1. Crear Pendiente
        val task = TaskItem(
            organizationId = orgId,
            title = "Configurar alertas de monitoreo en Prometheus",
            description = "Anadir umbral de 85% de uso de CPU en servidores",
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

    @Test
    fun testBitacoraWithImageEvidence() = runBlocking {
        val orgId = orgRepo.insertOrganization(
            Organization(name = "Evidence Org", description = "Test", iconEmoji = "IMG")
        )
        val imageList = listOf(
            "/data/user/0/com.iotaslife.app/files/evidence_images/evidence_1.jpg",
            "/data/user/0/com.iotaslife.app/files/evidence_images/evidence_2.jpg"
        )
        val entry = BitacoraEntry(
            organizationId = orgId,
            ticketCode = "TIK-IMG",
            title = "Inspeccion fisica de servidores",
            dateEpochMillis = System.currentTimeMillis(),
            workDone = "Reemplazo de cables de fibra optica.",
            observations = "Fotografias tomadas antes y despues.",
            actionsTaken = "Fotos anexadas como evidencia interna.",
            status = BitacoraStatus.COMPLETED,
            imagePaths = imageList
        )
        val id = bitacoraRepo.insertEntry(entry)
        val retrieved = bitacoraRepo.getEntryById(id)
        assertNotNull(retrieved)
        assertEquals(2, retrieved?.imagePaths?.size)
        assertEquals(imageList, retrieved?.imagePaths)
    }

    @Test
    fun testContingenciaLifecycle_Save_ToggleReimbursed_Delete() = runBlocking {
        val orgId = orgRepo.insertOrganization(
            Organization(name = "Contingencia Org", description = "Test Corp", iconEmoji = "CORP")
        )
        val entry = ContingenciaEntry(
            organizationId = orgId,
            title = "Caida de switch principal",
            reason = "Presencia inmediata requerida por fallo de red",
            solution = "Reinicio fisico y parcheo de cable",
            dateEpochMillis = System.currentTimeMillis(),
            taxiCost = 175.50,
            taxiDetails = "Taxi ida y vuelta domicilio - planta",
            isReimbursed = false,
            imagePaths = listOf("/data/user/0/com.iotaslife.app/files/evidence_images/receipt_1.jpg")
        )
        val id = contingenciaRepo.insertContingencia(entry)
        val retrieved = contingenciaRepo.getContingenciaById(id)
        assertNotNull(retrieved)
        assertEquals("Caida de switch principal", retrieved?.title)
        assertEquals(175.50, retrieved?.taxiCost ?: 0.0, 0.01)
        assertEquals(false, retrieved?.isReimbursed)
        assertEquals(1, retrieved?.imagePaths?.size)

        // Toggle reimbursed
        contingenciaRepo.toggleReimbursed(id, true)
        val updated = contingenciaRepo.getContingenciaById(id)
        assertEquals(true, updated?.isReimbursed)

        // Delete
        contingenciaRepo.deleteContingenciaById(id)
        val afterDelete = contingenciaRepo.getContingenciasForOrganization(orgId).first()
        assertTrue(afterDelete.isEmpty())
    }
}
