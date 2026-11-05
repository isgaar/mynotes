// File: app/src/main/java/com/iotaslife/app/presentation/bitacora/BitacoraEditorScreen.kt
// Description: Skeuomorphic notepad editor screen for bitacora entries with ruled paper fields, photo evidence, and integrated ticket pending tasks.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.bitacora

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iotaslife.app.R
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.domain.model.BitacoraStatus
import com.iotaslife.app.domain.model.TicketTaskHelper
import com.iotaslife.app.domain.model.TicketTaskHelper.isLinkedToTicket
import com.iotaslife.app.presentation.components.ConfirmDeleteDialog
import com.iotaslife.app.presentation.components.EvidenceThumbnail
import com.iotaslife.app.presentation.components.NotebookSpiralHeader
import com.iotaslife.app.presentation.components.NotepadPaperField
import com.iotaslife.app.presentation.components.NotepadSheetCard
import com.iotaslife.app.presentation.components.PerforatedTearEdge
import com.iotaslife.app.presentation.components.TicketTasksSection
import com.iotaslife.app.presentation.tasks.TasksViewModel
import com.iotaslife.app.presentation.theme.NotionBorder
import com.iotaslife.app.presentation.theme.NotionRed
import com.iotaslife.app.presentation.util.ImageStorageManager
import kotlinx.coroutines.launch

@Composable
fun BitacoraEditorScreen(
    entry: BitacoraEntry?,
    organizationId: Long,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: BitacoraViewModel,
    tasksViewModel: TasksViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var ticketCode by remember {
        mutableStateOf(
            entry?.ticketCode ?: "TIK-${(System.currentTimeMillis() / 1000) % 10000}"
        )
    }
    var workDone by remember { mutableStateOf(entry?.workDone ?: "") }
    var observations by remember { mutableStateOf(entry?.observations ?: "") }
    var actionsTaken by remember { mutableStateOf(entry?.actionsTaken ?: "") }
    var status by remember { mutableStateOf(entry?.status ?: BitacoraStatus.IN_PROGRESS) }
    var dateEpochMillis by remember { mutableLongStateOf(entry?.dateEpochMillis ?: System.currentTimeMillis()) }
    var imagePaths by remember { mutableStateOf(entry?.imagePaths ?: emptyList()) }

    var titleError by remember { mutableStateOf(false) }
    var workDoneError by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val validationErrorTitle = stringResource(id = R.string.validation_error_title)
    val validationErrorWorkDone = stringResource(id = R.string.validation_error_work_done)
    val msgBitacoraSaved = stringResource(id = R.string.msg_bitacora_saved)
    val msgBitacoraUpdated = stringResource(id = R.string.msg_bitacora_updated)
    val msgBitacoraDeleted = stringResource(id = R.string.msg_bitacora_deleted)

    // Reactive tasks list for this organization
    val tasksState = tasksViewModel?.uiState?.collectAsState()
    val allTasks = tasksState?.value?.tasks ?: emptyList()
    val linkedTasks = allTasks.filter { it.isLinkedToTicket(ticketCode) }

    // Photo picker for evidence images (saves locally in app private storage)
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
                        "${newPaths.size} evidencia(s) agregada(s)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.action_cancel)
                            )
                        }
                        Text(
                            text = if (entry == null) {
                                stringResource(id = R.string.new_bitacora_entry)
                            } else {
                                stringResource(id = R.string.edit_bitacora_entry)
                            },
                            style = MaterialTheme.typography.titleLarge,
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
                            onClick = {
                                if (title.isBlank()) {
                                    titleError = true
                                    scope.launch { snackbarHostState.showSnackbar(validationErrorTitle) }
                                    return@Button
                                }
                                if (workDone.isBlank()) {
                                    workDoneError = true
                                    scope.launch { snackbarHostState.showSnackbar(validationErrorWorkDone) }
                                    return@Button
                                }

                                val isNew = entry == null
                                viewModel.saveEntry(
                                    id = entry?.id ?: 0L,
                                    organizationId = organizationId,
                                    ticketCode = ticketCode,
                                    title = title,
                                    dateEpochMillis = dateEpochMillis,
                                    workDone = workDone,
                                    observations = observations,
                                    actionsTaken = actionsTaken,
                                    status = status,
                                    imagePaths = imagePaths,
                                    onComplete = {
                                        Toast.makeText(
                                            context,
                                            if (isNew) msgBitacoraSaved else msgBitacoraUpdated,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onSaveSuccess()
                                    }
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(id = R.string.action_save))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Skeuomorphic Notepad Folio Card: Ticket metadata & Status
            NotepadSheetCard(hasSpiral = true) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = ticketCode,
                            onValueChange = { ticketCode = it },
                            label = { Text(text = stringResource(id = R.string.ticket_code)) },
                            placeholder = { Text(text = stringResource(id = R.string.ticket_code_hint)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = BitacoraViewModel.formatDate(dateEpochMillis),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text(text = stringResource(id = R.string.ticket_date)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.ticket_status),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Status Chips Selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BitacoraStatus.entries.forEach { itemStatus ->
                            val isSelected = status == itemStatus
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Color(itemStatus.bgHexColor)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(itemStatus.hexColor) else NotionBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { status = itemStatus }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(id = itemStatus.labelRes),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(itemStatus.hexColor) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (titleError && it.isNotBlank()) titleError = false
                },
                isError = titleError,
                label = { Text(text = stringResource(id = R.string.ticket_title)) },
                placeholder = { Text(text = stringResource(id = R.string.ticket_title_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Section 1: Lo que se hizo ese día (Work Done on ruled notepad paper)
            NotepadPaperField(
                title = stringResource(id = R.string.work_done_section),
                icon = Icons.AutoMirrored.Outlined.Assignment,
                value = workDone,
                onValueChange = {
                    workDone = it
                    if (workDoneError && it.isNotBlank()) workDoneError = false
                },
                placeholder = stringResource(id = R.string.work_done_hint),
                isError = workDoneError,
                height = 130.dp,
                headerColor = MaterialTheme.colorScheme.primary
            )

            // Section 2: Acciones realizadas (Actions Taken on ruled notepad paper)
            NotepadPaperField(
                title = stringResource(id = R.string.actions_taken_section),
                icon = Icons.Outlined.Build,
                value = actionsTaken,
                onValueChange = { actionsTaken = it },
                placeholder = stringResource(id = R.string.actions_taken_hint),
                height = 110.dp,
                headerColor = MaterialTheme.colorScheme.secondary
            )

            // Section 3: Observaciones (Observations on ruled notepad paper)
            NotepadPaperField(
                title = stringResource(id = R.string.observations_section),
                icon = Icons.Outlined.Visibility,
                value = observations,
                onValueChange = { observations = it },
                placeholder = stringResource(id = R.string.observations_hint),
                height = 110.dp,
                headerColor = MaterialTheme.colorScheme.tertiary
            )

            // Section 4: NEW! Dedicated Sticky Note Section for Pending Activities linked to this ticket
            if (tasksViewModel != null) {
                TicketTasksSection(
                    ticketCode = ticketCode,
                    linkedTasks = linkedTasks,
                    onToggleTask = { task -> tasksViewModel.toggleCompleted(task) },
                    onDeleteTask = { task -> tasksViewModel.deleteTask(task) },
                    onAddTask = { taskTitle, taskDesc, priority ->
                        tasksViewModel.saveTask(
                            organizationId = organizationId,
                            title = taskTitle,
                            description = TicketTaskHelper.formatDescriptionWithTicket(ticketCode, taskDesc),
                            priority = priority
                        )
                    },
                    candidateTextFromActions = actionsTaken,
                    candidateTextFromObservations = observations
                )
            }

            // Section 5: Evidencias Fotográficas (Guardado Privado)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.evidence_section_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(id = R.string.evidence_section_hint),
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(id = R.string.action_add_evidence), fontSize = 12.sp)
                        }
                    }

                    if (imagePaths.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(imagePaths, key = { it }) { path ->
                                EvidenceThumbnail(
                                    filePath = path,
                                    size = 90.dp,
                                    onDelete = {
                                        scope.launch {
                                            ImageStorageManager.deleteImage(path)
                                            imagePaths = imagePaths.filter { it != path }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDeleteConfirm && entry != null) {
        ConfirmDeleteDialog(
            title = stringResource(id = R.string.delete_confirm_title),
            message = stringResource(id = R.string.delete_confirm_msg),
            onConfirm = {
                viewModel.deleteEntry(entry) {
                    Toast.makeText(context, msgBitacoraDeleted, Toast.LENGTH_SHORT).show()
                    showDeleteConfirm = false
                    onBack()
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
