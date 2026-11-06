// File: app/src/main/java/com/iotaslife/app/presentation/util/OrganizationBackupManager.kt
// Description: Utility managing complete organizational backup archiving to ZIP and JSON restoration with evidence photos.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.domain.model.BitacoraStatus
import com.iotaslife.app.domain.model.ContingenciaEntry
import com.iotaslife.app.domain.model.Organization
import com.iotaslife.app.domain.model.TaskItem
import com.iotaslife.app.domain.model.TaskPriority
import com.iotaslife.app.domain.repository.BitacoraRepository
import com.iotaslife.app.domain.repository.ContingenciaRepository
import com.iotaslife.app.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupRestoreResult(
    val success: Boolean,
    val bitacorasCount: Int = 0,
    val tasksCount: Int = 0,
    val contingenciasCount: Int = 0,
    val imagesCount: Int = 0,
    val errorMessage: String? = null
)

object OrganizationBackupManager {

    private const val BACKUP_FORMAT = "mynotes_organization_backup"
    private const val BACKUP_VERSION = 1
    private const val MANIFEST_ENTRY_NAME = "manifest.json"
    private const val IMAGES_DIR_NAME = "images"

    /**
     * Genera un archivo ZIP que contiene el manifiesto JSON con notas de bitacora,
     * tareas pendientes, contingencias laborales con justificacion de taxi y todas las
     * imagenes fotograficas de evidencia fisica.
     */
    suspend fun exportOrganizationBackup(
        context: Context,
        organization: Organization,
        bitacoras: List<BitacoraEntry>,
        tasks: List<TaskItem>,
        contingencias: List<ContingenciaEntry>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }

            val orgNameSanitized = organization.name.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val zipFile = File(exportDir, "Backup_${orgNameSanitized}_$dateStr.zip")

            val rootJson = JSONObject().apply {
                put("app", "Mis Notas")
                put("format", BACKUP_FORMAT)
                put("version", BACKUP_VERSION)
                put("exportedAt", System.currentTimeMillis())

                put("organization", JSONObject().apply {
                    put("id", organization.id)
                    put("name", organization.name)
                    put("description", organization.description)
                    put("category", organization.iconEmoji)
                    put("colorHex", organization.colorHex)
                })

                val bitacorasArray = JSONArray()
                for (b in bitacoras) {
                    val bObj = JSONObject().apply {
                        put("ticketCode", b.ticketCode)
                        put("title", b.title)
                        put("workDone", b.workDone)
                        put("observations", b.observations)
                        put("actionsTaken", b.actionsTaken)
                        put("statusName", b.status.name)
                        put("dateEpochMillis", b.dateEpochMillis)

                        val imagesArray = JSONArray()
                        b.imagePaths.forEach { imgPath ->
                            val fName = File(imgPath).name
                            if (fName.isNotBlank()) imagesArray.put(fName)
                        }
                        put("imageFiles", imagesArray)
                    }
                    bitacorasArray.put(bObj)
                }
                put("bitacoras", bitacorasArray)

                val tasksArray = JSONArray()
                for (t in tasks) {
                    val tObj = JSONObject().apply {
                        put("title", t.title)
                        put("description", t.description)
                        put("isCompleted", t.isCompleted)
                        put("priorityName", t.priority.name)
                    }
                    tasksArray.put(tObj)
                }
                put("tasks", tasksArray)

                val contingenciasArray = JSONArray()
                for (c in contingencias) {
                    val cObj = JSONObject().apply {
                        put("title", c.title)
                        put("reason", c.reason)
                        put("solution", c.solution)
                        put("dateEpochMillis", c.dateEpochMillis)
                        put("taxiCost", c.taxiCost)
                        put("taxiDetails", c.taxiDetails)
                        put("isReimbursed", c.isReimbursed)

                        val imagesArray = JSONArray()
                        c.imagePaths.forEach { imgPath ->
                            val fName = File(imgPath).name
                            if (fName.isNotBlank()) imagesArray.put(fName)
                        }
                        put("imageFiles", imagesArray)
                    }
                    contingenciasArray.put(cObj)
                }
                put("contingencias", contingenciasArray)
            }

            // Write ZIP
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                // Write manifest.json
                val manifestBytes = rootJson.toString(2).toByteArray(StandardCharsets.UTF_8)
                val manifestEntry = ZipEntry(MANIFEST_ENTRY_NAME)
                zipOut.putNextEntry(manifestEntry)
                zipOut.write(manifestBytes)
                zipOut.closeEntry()

                // Collect all referenced image files
                val includedImageNames = mutableSetOf<String>()
                val allImagePaths = bitacoras.flatMap { it.imagePaths } + contingencias.flatMap { it.imagePaths }

                val evidenceDir = File(context.filesDir, "evidence_images")

                for (path in allImagePaths) {
                    val fName = File(path).name
                    if (fName.isBlank() || !includedImageNames.add(fName)) continue

                    val candidateFile = if (File(path).isAbsolute && File(path).exists()) {
                        File(path)
                    } else {
                        File(evidenceDir, fName)
                    }

                    if (candidateFile.exists() && candidateFile.isFile) {
                        val imageEntry = ZipEntry("$IMAGES_DIR_NAME/$fName")
                        zipOut.putNextEntry(imageEntry)
                        candidateFile.inputStream().use { input ->
                            input.copyTo(zipOut)
                        }
                        zipOut.closeEntry()
                    }
                }
                zipOut.flush()
            }

            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Lanza el selector del sistema Android para guardar o compartir el archivo ZIP generado.
     */
    fun shareBackupFile(context: Context, file: File, organizationName: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad - $organizationName")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Guardar o compartir copia de seguridad")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Restaura notas de bitacora, tareas, contingencias y fotografias de evidencia desde un archivo ZIP de respaldo.
     */
    suspend fun restoreBackupFromUri(
        context: Context,
        uri: Uri,
        targetOrgId: Long,
        bitacoraRepo: BitacoraRepository,
        taskRepo: TaskRepository,
        contingenciaRepo: ContingenciaRepository
    ): BackupRestoreResult = withContext(Dispatchers.IO) {
        try {
            val evidenceDir = File(context.filesDir, "evidence_images").apply {
                if (!exists()) mkdirs()
            }

            var manifestContent: String? = null
            var restoredImages = 0

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val entryName = entry.name
                        if (!entry.isDirectory) {
                            if (entryName == MANIFEST_ENTRY_NAME || entryName.endsWith("/$MANIFEST_ENTRY_NAME")) {
                                val buffer = ByteArrayOutputStream()
                                zipIn.copyTo(buffer)
                                manifestContent = buffer.toString(StandardCharsets.UTF_8.name())
                            } else if (entryName.startsWith("$IMAGES_DIR_NAME/") || entryName.contains("/$IMAGES_DIR_NAME/")) {
                                val fileName = File(entryName).name
                                val targetFile = File(evidenceDir, fileName)
                                FileOutputStream(targetFile).use { out ->
                                    zipIn.copyTo(out)
                                }
                                restoredImages++
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            } ?: return@withContext BackupRestoreResult(success = false, errorMessage = "No se pudo abrir el archivo")

            if (manifestContent == null) {
                return@withContext BackupRestoreResult(success = false, errorMessage = "Archivo de respaldo no valido (falta manifest.json)")
            }

            val rootJson = JSONObject(manifestContent!!)
            val format = rootJson.optString("format")
            if (format != BACKUP_FORMAT) {
                return@withContext BackupRestoreResult(success = false, errorMessage = "Formato de archivo no compatible")
            }

            var restoredBitacoras = 0
            var restoredTasks = 0
            var restoredContingencias = 0

            // Restore Bitacoras
            val bitacorasArray = rootJson.optJSONArray("bitacoras")
            if (bitacorasArray != null) {
                for (i in 0 until bitacorasArray.length()) {
                    val bObj = bitacorasArray.getJSONObject(i)
                    val imgFilesArray = bObj.optJSONArray("imageFiles") ?: JSONArray()
                    val imgPaths = mutableListOf<String>()
                    for (j in 0 until imgFilesArray.length()) {
                        val fName = imgFilesArray.getString(j)
                        val fullPath = File(evidenceDir, fName).absolutePath
                        imgPaths.add(fullPath)
                    }

                    val statusName = bObj.optString("statusName", BitacoraStatus.IN_PROGRESS.name)
                    val status = try {
                        BitacoraStatus.valueOf(statusName)
                    } catch (_: Exception) {
                        BitacoraStatus.IN_PROGRESS
                    }

                    val entry = BitacoraEntry(
                        id = 0,
                        organizationId = targetOrgId,
                        ticketCode = bObj.optString("ticketCode", "TIK-${System.currentTimeMillis() % 10000}"),
                        title = bObj.optString("title", "Nota restaurada"),
                        dateEpochMillis = bObj.optLong("dateEpochMillis", System.currentTimeMillis()),
                        workDone = bObj.optString("workDone", ""),
                        observations = bObj.optString("observations", ""),
                        actionsTaken = bObj.optString("actionsTaken", ""),
                        status = status,
                        imagePaths = imgPaths,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    bitacoraRepo.insertEntry(entry)
                    restoredBitacoras++
                }
            }

            // Restore Tasks
            val tasksArray = rootJson.optJSONArray("tasks")
            if (tasksArray != null) {
                for (i in 0 until tasksArray.length()) {
                    val tObj = tasksArray.getJSONObject(i)
                    val priorityName = tObj.optString("priorityName", TaskPriority.MEDIUM.name)
                    val priority = try {
                        TaskPriority.valueOf(priorityName)
                    } catch (_: Exception) {
                        TaskPriority.MEDIUM
                    }

                    val task = TaskItem(
                        id = 0,
                        organizationId = targetOrgId,
                        title = tObj.optString("title", "Pendiente restaurado"),
                        description = tObj.optString("description", ""),
                        isCompleted = tObj.optBoolean("isCompleted", false),
                        priority = priority,
                        createdAt = System.currentTimeMillis()
                    )
                    taskRepo.insertTask(task)
                    restoredTasks++
                }
            }

            // Restore Contingencias
            val contingenciasArray = rootJson.optJSONArray("contingencias")
            if (contingenciasArray != null) {
                for (i in 0 until contingenciasArray.length()) {
                    val cObj = contingenciasArray.getJSONObject(i)
                    val imgFilesArray = cObj.optJSONArray("imageFiles") ?: JSONArray()
                    val imgPaths = mutableListOf<String>()
                    for (j in 0 until imgFilesArray.length()) {
                        val fName = imgFilesArray.getString(j)
                        val fullPath = File(evidenceDir, fName).absolutePath
                        imgPaths.add(fullPath)
                    }

                    val contingencia = ContingenciaEntry(
                        id = 0,
                        organizationId = targetOrgId,
                        title = cObj.optString("title", "Contingencia restaurada"),
                        reason = cObj.optString("reason", ""),
                        solution = cObj.optString("solution", ""),
                        dateEpochMillis = cObj.optLong("dateEpochMillis", System.currentTimeMillis()),
                        taxiCost = cObj.optDouble("taxiCost", 0.0),
                        taxiDetails = cObj.optString("taxiDetails", ""),
                        isReimbursed = cObj.optBoolean("isReimbursed", false),
                        imagePaths = imgPaths,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    contingenciaRepo.insertContingencia(contingencia)
                    restoredContingencias++
                }
            }

            BackupRestoreResult(
                success = true,
                bitacorasCount = restoredBitacoras,
                tasksCount = restoredTasks,
                contingenciasCount = restoredContingencias,
                imagesCount = restoredImages
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BackupRestoreResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Error desconocido al restaurar copia"
            )
        }
    }
}
