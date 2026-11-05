// File: app/src/main/java/com/iotaslife/app/presentation/util/ExcelExporter.kt
// Description: Utility exporting organization bitacoras to Excel-compatible CSV with UTF-8 BOM encoding and native FileProvider sharing.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.domain.model.Organization
import com.iotaslife.app.presentation.bitacora.BitacoraViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    /**
     * Exporta las notas de bitácora a un archivo Excel (.csv delimitado con punto y coma y BOM UTF-8)
     * reconocido automáticamente por Microsoft Excel, Google Sheets y LibreOffice.
     */
    suspend fun exportBitacoraToExcel(
        context: Context,
        organization: Organization?,
        entries: List<BitacoraEntry>
    ): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }

            val orgNameSanitized = (organization?.name ?: "Empresa")
                .replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Bitacora_${orgNameSanitized}_$dateStr.csv"
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { fos ->
                // Escribir UTF-8 BOM (\uFEFF) para que Microsoft Excel detecte automáticamente la codificación UTF-8
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    // Encabezados
                    writer.write("Código Ticket;Fecha;Título;Estado;Lo que se hizo ese día;Acciones realizadas;Observaciones;Nro Evidencias;ID Interno\n")

                    for (entry in entries) {
                        val row = listOf(
                            escapeCsv(entry.ticketCode),
                            escapeCsv(BitacoraViewModel.formatDate(entry.dateEpochMillis)),
                            escapeCsv(entry.title),
                            escapeCsv(entry.status.name),
                            escapeCsv(entry.workDone),
                            escapeCsv(entry.actionsTaken),
                            escapeCsv(entry.observations),
                            escapeCsv(entry.imagePaths.size.toString()),
                            escapeCsv(entry.id.toString())
                        ).joinToString(";") + "\n"

                        writer.write(row)
                    }
                    writer.flush()
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Abre el selector de aplicaciones para compartir o abrir en Excel.
     */
    fun shareExcelFile(context: Context, file: File, organizationName: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Bitácora de Trabajo - $organizationName")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Exportar bitácora a Excel")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return "\"$clean\""
    }
}

