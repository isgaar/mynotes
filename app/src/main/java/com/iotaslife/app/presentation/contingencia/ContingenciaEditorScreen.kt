// File: app/src/main/java/com/iotaslife/app/presentation/contingencia/ContingenciaEditorScreen.kt
// Description: Editor screen for recording urgent on-site contingency visits, taxi expenses, route details, and photographic receipts.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.contingencia

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iotaslife.app.R
import com.iotaslife.app.domain.model.ContingenciaEntry
import com.iotaslife.app.presentation.components.ConfirmDeleteDialog
import com.iotaslife.app.presentation.components.EvidenceThumbnail
import com.iotaslife.app.presentation.theme.NotionBlue
import com.iotaslife.app.presentation.theme.NotionBorder
import com.iotaslife.app.presentation.theme.NotionGreen
import com.iotaslife.app.presentation.theme.NotionRed
import com.iotaslife.app.presentation.util.ImageStorageManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ContingenciaEditorScreen(
    entry: ContingenciaEntry?,
    organizationId: Long,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: ContingenciaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf(entry?.title ?: "") }
    var reason by remember { mutableStateOf(entry?.reason ?: "") }
    var solution by remember { mutableStateOf(entry?.solution ?: "") }
    var taxiCostText by remember {
        mutableStateOf(if (entry != null && entry.taxiCost > 0) entry.taxiCost.toString() else "")
    }
    var taxiDetails by remember { mutableStateOf(entry?.taxiDetails ?: "") }
    var isReimbursed by remember { mutableStateOf(entry?.isReimbursed ?: false) }
    var dateEpochMillis by remember { mutableLongStateOf(entry?.dateEpochMillis ?: System.currentTimeMillis()) }
    var imagePaths by remember { mutableStateOf(entry?.imagePaths ?: emptyList()) }

    var titleError by remember { mutableStateOf(false) }
    var reasonError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    val msgSaved = stringResource(id = R.string.msg_contingencia_saved)
    val msgUpdated = stringResource(id = R.string.msg_contingencia_updated)
    val msgDeleted = stringResource(id = R.string.msg_contingencia_deleted)
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    // Photo picker for taxi receipts & evidence photos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val newPaths = mutableListOf<String>()
                for (uri in uris) {
                    val saved = ImageStorageManager.saveImageFromUri(context, uri)
                    if (saved != null) {
                        newPaths.add(saved)
                    }
                }
                if (newPaths.isNotEmpty()) {
                    imagePaths = imagePaths + newPaths
                    Toast.makeText(
                        context,
                        "${newPaths.size} comprobante(s) agregado(s)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val saveAction = {
        val cleanTitle = title.trim()
        val cleanReason = reason.trim()
        val cleanSolution = solution.trim()
        val parsedCost = taxiCostText.trim().toDoubleOrNull() ?: 0.0

        if (cleanTitle.isEmpty()) {
            titleError = true
            Toast.makeText(context, context.getString(R.string.validation_error_contingencia_title), Toast.LENGTH_SHORT).show()
        } else if (cleanReason.isEmpty()) {
            reasonError = true
            Toast.makeText(context, context.getString(R.string.validation_error_contingencia_reason), Toast.LENGTH_SHORT).show()
        } else {
            val toSave = ContingenciaEntry(
                id = entry?.id ?: 0L,
                organizationId = if (entry != null && entry.organizationId != 0L) entry.organizationId else organizationId,
                title = cleanTitle,
                reason = cleanReason,
                solution = cleanSolution,
                dateEpochMillis = dateEpochMillis,
                taxiCost = parsedCost,
                taxiDetails = taxiDetails.trim(),
                isReimbursed = isReimbursed,
                imagePaths = imagePaths,
                createdAt = entry?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            viewModel.saveContingencia(toSave) {
                Toast.makeText(context, if (entry == null) msgSaved else msgUpdated, Toast.LENGTH_SHORT).show()
                onSaveSuccess()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.action_cancel)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (entry == null) stringResource(id = R.string.new_contingencia)
                            else stringResource(id = R.string.edit_contingencia),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (entry != null) {
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = stringResource(id = R.string.action_delete),
                                    tint = NotionRed
                                )
                            }
                        }

                        Button(
                            onClick = saveAction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.action_save),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card: Incident Title & Date
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFAFAF7)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (it.isNotBlank()) titleError = false
                        },
                        label = { Text(text = stringResource(id = R.string.contingencia_title_label)) },
                        placeholder = { Text(text = stringResource(id = R.string.contingencia_title_hint)) },
                        isError = titleError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Date Picker Row
                    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(dateEpochMillis))
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = stringResource(id = R.string.contingencia_date_label)) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                val cal = Calendar.getInstance().apply { timeInMillis = dateEpochMillis }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val picked = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, day)
                                        }
                                        dateEpochMillis = picked.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Seleccionar fecha",
                                    tint = NotionBlue
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = dateEpochMillis }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val picked = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, day)
                                        }
                                        dateEpochMillis = picked.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Card: Taxi Expense & Transportation (Key Boss Justification)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFAFAF7)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        NotionGreen.copy(alpha = 0.35f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NotionGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = NotionGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.contingencia_taxi_cost_label),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gasto de transporte para reembolso",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = taxiCostText,
                            onValueChange = { taxiCostText = it },
                            label = { Text(text = "Costo ($)") },
                            placeholder = { Text(text = stringResource(id = R.string.contingencia_taxi_cost_hint)) },
                            leadingIcon = {
                                Text(
                                    text = "$",
                                    fontWeight = FontWeight.Bold,
                                    color = NotionGreen,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Reimbursed toggle switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(
                                    text = "¿Reembolsado?",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isReimbursed) "Sí (Pagado)" else "No (Pendiente)",
                                    fontSize = 11.sp,
                                    color = if (isReimbursed) NotionGreen else NotionRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isReimbursed,
                                onCheckedChange = { isReimbursed = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = NotionGreen,
                                    checkedTrackColor = NotionGreen.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = taxiDetails,
                        onValueChange = { taxiDetails = it },
                        label = { Text(text = stringResource(id = R.string.contingencia_taxi_details_label)) },
                        placeholder = { Text(text = stringResource(id = R.string.contingencia_taxi_details_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Card: Urgency Reason (Motivo de Presencia Inmediata)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFAFAF7)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (reasonError) NotionRed else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = NotionRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.contingencia_reason_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Justificación ante tu jefe de por qué requirió asistencia presencial urgente",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = {
                            reason = it
                            if (it.isNotBlank()) reasonError = false
                        },
                        placeholder = { Text(text = stringResource(id = R.string.contingencia_reason_hint)) },
                        isError = reasonError,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Card: Solution Applied (Trabajo Realizado en Sitio)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFAFAF7)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Build,
                            contentDescription = null,
                            tint = NotionBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.contingencia_solution_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = solution,
                        onValueChange = { solution = it },
                        placeholder = { Text(text = stringResource(id = R.string.contingencia_solution_hint)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Card: Photo Evidence & Taxi Receipts
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFAFAF7)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.contingencia_evidence_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.action_attach_receipt),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.contingencia_evidence_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (imagePaths.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(imagePaths) { path ->
                                EvidenceThumbnail(
                                    filePath = path,
                                    size = 72.dp,
                                    onDelete = {
                                        imagePaths = imagePaths - path
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Primary Bottom Save Button
            Button(
                onClick = saveAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.action_save),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDeleteConfirm && entry != null) {
        ConfirmDeleteDialog(
            title = stringResource(id = R.string.delete_contingencia_confirm_title),
            message = stringResource(id = R.string.delete_contingencia_confirm_msg),
            onConfirm = {
                viewModel.deleteContingencia(entry) {
                    Toast.makeText(context, msgDeleted, Toast.LENGTH_SHORT).show()
                    onSaveSuccess()
                }
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

