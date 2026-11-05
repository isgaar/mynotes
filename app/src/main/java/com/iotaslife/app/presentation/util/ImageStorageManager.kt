package com.iotaslife.app.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageManager {

    private const val DIRECTORY_NAME = "evidence_images"

    /**
     * Guarda una imagen en el almacenamiento privado de la app (filesDir).
     * Al estar en el directorio privado, Android MediaScanner NO la indexa
     * y por tanto NUNCA aparecerá duplicada en la galería del dispositivo.
     */
    suspend fun saveImageFromUri(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, DIRECTORY_NAME).apply {
                if (!exists()) mkdirs()
            }
            val fileName = "evidencia_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
            val targetFile = File(dir, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            inputStream?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Elimina el archivo de evidencia si se descarta o elimina la nota.
     */
    suspend fun deleteImage(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Decodifica un bitmap optimizado para miniaturas.
     */
    suspend fun loadThumbnailBitmap(filePath: String, targetSize: Int = 300): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, options)

            var sampleSize = 1
            if (options.outHeight > targetSize || options.outWidth > targetSize) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / sampleSize) >= targetSize && (halfWidth / sampleSize) >= targetSize) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeFile(filePath, decodeOptions)
        } catch (e: Exception) {
            null
        }
    }
}

