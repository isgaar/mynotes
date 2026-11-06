// File: app/src/main/java/com/iotaslife/app/presentation/components/EvidenceImageViewer.kt
// Description: Composable components providing thumbnail rendering and launching system default image viewer via FileProvider.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.iotaslife.app.R
import com.iotaslife.app.presentation.theme.NotionBorder
import com.iotaslife.app.presentation.theme.NotionRed
import com.iotaslife.app.presentation.util.ImageStorageManager
import java.io.File

/**
 * Lanza el visor de imágenes predeterminado del sistema (Google Fotos, Galería, etc.)
 * utilizando FileProvider con permisos seguros de lectura en tiempo real.
 */
fun openImageWithDefaultViewer(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(
                context,
                context.getString(R.string.msg_image_not_found),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(viewIntent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            e.localizedMessage ?: "Error al abrir la imagen en el visor del sistema",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun EvidenceThumbnail(
    filePath: String,
    size: Dp = 80.dp,
    onDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmapState by produceState<Bitmap?>(initialValue = null, key1 = filePath) {
        value = ImageStorageManager.loadThumbnailBitmap(filePath, targetSize = 300)
    }

    Box(
        modifier = modifier
            .size(size)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, NotionBorder, RoundedCornerShape(10.dp))
                .clickable {
                    if (onClick != null) {
                        onClick()
                    } else {
                        openImageWithDefaultViewer(context, filePath)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val bitmap = bitmapState
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Evidencia fotográfica",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Delete button
        if (onDelete != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(NotionRed)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar evidencia",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
