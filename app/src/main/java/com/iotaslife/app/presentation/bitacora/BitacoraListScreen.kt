// File: app/src/main/java/com/iotaslife/app/presentation/bitacora/BitacoraListScreen.kt
// Description: Main bitacora list screen displaying skeuomorphic notepad ticket cards, pending activities, search, date filters, and Excel export.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.bitacora

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AssignmentLate
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iotaslife.app.R
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.domain.model.BitacoraStatus
import com.iotaslife.app.domain.model.Organization
import com.iotaslife.app.domain.model.TaskItem
import com.iotaslife.app.domain.model.TicketTaskHelper
import com.iotaslife.app.domain.model.TicketTaskHelper.isLinkedToTicket
import com.iotaslife.app.presentation.components.ConfirmDeleteDialog
import com.iotaslife.app.presentation.components.EvidenceThumbnail
import com.iotaslife.app.presentation.components.NotepadSheetCard
import com.iotaslife.app.presentation.components.QuickAddTicketTaskDialog
import com.iotaslife.app.presentation.components.StatusChip
import com.iotaslife.app.presentation.components.TicketTaskChecklistItem
import com.iotaslife.app.presentation.tasks.TasksViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.iotaslife.app.IotasLifeApp
import com.iotaslife.app.presentation.components.ExportBackupDialog
import com.iotaslife.app.presentation.theme.NotionBlue
import com.iotaslife.app.presentation.theme.NotionBorder
import com.iotaslife.app.presentation.theme.NotionDivider
import com.iotaslife.app.presentation.theme.NotionRed
import com.iotaslife.app.presentation.util.ExcelExporter
import com.iotaslife.app.presentation.util.OrganizationBackupManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun BitacoraListScreen(
    viewModel: BitacoraViewModel,
    organization: Organization? = null,
    tasksViewModel: TasksViewModel? = null,
    onAddEntry: () -> Unit,
    onEditEntry: (BitacoraEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    var entryToDelete by remember { mutableStateOf<BitacoraEntry?>(null) }
    var entryForQuickTask by remember { mutableStateOf<BitacoraEntry?>(null) }

    // Observe tasks for ticket task indicators
    val tasksState = tasksViewModel?.uiState?.collectAsState()
    val allTasks = tasksState?.value?.tasks ?: emptyList()

    val msgDeleted = stringResource(id = R.string.msg_bitacora_deleted)
    val msgNoEntries = stringResource(id = R.string.msg_no_entries_to_export)
    val msgExporting = stringResource(id = R.string.msg_exporting_excel)
    val msgExportSuccess = stringResource(id = R.string.msg_exported_success)
    val msgTaskCreated = stringResource(id = R.string.msg_ticket_task_created)
    val msgRestoring = stringResource(id = R.string.msg_restoring_backup)
    val msgGeneratingBackup = stringResource(id = R.string.msg_generating_backup)
    val msgBackupSuccess = stringResource(id = R.string.msg_backup_success)
    val msgRestoreError = stringResource(id = R.string.msg_restore_error)

    var showExportDialog by remember { mutableStateOf(false) }
    val appContainer = (context.applicationContext as? IotasLifeApp)?.container

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && organization != null && appContainer != null) {
            Toast.makeText(context, msgRestoring, Toast.LENGTH_SHORT).show()
            scope.launch {
                val result = OrganizationBackupManager.restoreBackupFromUri(
                    context = context,
                    uri = uri,
                    targetOrgId = organization.id,
                    bitacoraRepo = appContainer.bitacoraRepository,
                    taskRepo = appContainer.taskRepository,
                    contingenciaRepo = appContainer.contingenciaRepository
                )
                if (result.success) {
                    val summary = context.getString(
                        R.string.msg_restore_success,
                        result.bitacorasCount,
                        result.tasksCount,
                        result.contingenciasCount,
                        result.imagesCount
                    )
                    Toast.makeText(context, summary, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, result.errorMessage ?: msgRestoreError, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun exportToExcel() {
        val targetList = if (uiState.entries.isNotEmpty()) uiState.entries else uiState.rawEntries
        if (targetList.isEmpty()) {
            Toast.makeText(context, msgNoEntries, Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, msgExporting, Toast.LENGTH_SHORT).show()
        scope.launch {
            val file = ExcelExporter.exportBitacoraToExcel(context, organization, targetList)
            if (file != null) {
                Toast.makeText(context, msgExportSuccess, Toast.LENGTH_SHORT).show()
                ExcelExporter.shareExcelFile(context, file, organization?.name ?: "Empresa")
            }
        }
    }

    fun performOrganizationBackup() {
        if (organization == null || appContainer == null) {
            Toast.makeText(context, msgNoEntries, Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(context, msgGeneratingBackup, Toast.LENGTH_SHORT).show()
        scope.launch {
            val bitacoras = appContainer.bitacoraRepository.getEntriesForOrganization(organization.id).first()
            val tasks = appContainer.taskRepository.getTasksForOrganization(organization.id).first()
            val contingencias = appContainer.contingenciaRepository.getContingenciasForOrganization(organization.id).first()

            val file = OrganizationBackupManager.exportOrganizationBackup(
                context = context,
                organization = organization,
                bitacoras = bitacoras,
                tasks = tasks,
                contingencias = contingencias
            )

            if (file != null) {
                Toast.makeText(context, msgBackupSuccess, Toast.LENGTH_SHORT).show()
                OrganizationBackupManager.shareBackupFile(context, file, organization.name)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header section with Title and Export / Backup button
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.bitacora_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.bitacora_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledTonalButton(
                    onClick = { showExportDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = stringResource(id = R.string.export_button_label),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.export_button_label),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Notion-style search input (search by text, date, title, or "hoy")
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_bitacora_hint),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NotionBlue,
                    unfocusedBorderColor = NotionBorder,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter chips: "Todos", "Hoy", and status chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isAll = uiState.selectedStatusFilter == null && !uiState.filterTodayOnly
                    FilterChip(
                        selected = isAll,
                        onClick = {
                            viewModel.onStatusFilterChanged(null)
                            if (uiState.filterTodayOnly) viewModel.toggleTodayFilter()
                        },
                        label = { Text(stringResource(id = R.string.filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NotionBlue.copy(alpha = 0.15f),
                            selectedLabelColor = NotionBlue
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterTodayOnly,
                        onClick = { viewModel.toggleTodayFilter() },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Today,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(stringResource(id = R.string.filter_today)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                items(BitacoraStatus.entries) { status ->
                    val isSelected = uiState.selectedStatusFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.onStatusFilterChanged(if (isSelected) null else status)
                        },
                        label = { Text(text = stringResource(id = status.labelRes)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(status.bgHexColor),
                            selectedLabelColor = Color(status.hexColor)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Entries List
            if (uiState.entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.empty_bitacora_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(id = R.string.empty_bitacora_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.entries, key = { it.id }) { entry ->
                        val ticketTasks = allTasks.filter { it.isLinkedToTicket(entry.ticketCode) }

                        BitacoraCard(
                            entry = entry,
                            linkedTasks = ticketTasks,
                            onEdit = { onEditEntry(entry) },
                            onDelete = { entryToDelete = entry },
                            onQuickAddTask = { entryForQuickTask = entry },
                            onToggleTask = { task ->
                                tasksViewModel?.toggleCompleted(task)
                            },
                            onDeleteTask = { task ->
                                tasksViewModel?.deleteTask(task)
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to add entry
        FloatingActionButton(
            onClick = onAddEntry,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.new_bitacora_entry)
            )
        }
    }

    // Delete Confirmation Dialog
    entryToDelete?.let { entry ->
        ConfirmDeleteDialog(
            title = stringResource(id = R.string.delete_confirm_title),
            message = stringResource(id = R.string.delete_confirm_msg),
            onConfirm = {
                viewModel.deleteEntry(entry) {
                    Toast.makeText(context, msgDeleted, Toast.LENGTH_SHORT).show()
                }
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }

    // Quick Add Ticket Task Dialog
    entryForQuickTask?.let { entry ->
        QuickAddTicketTaskDialog(
            ticketCode = entry.ticketCode,
            ticketTitle = entry.title,
            onConfirm = { title, priority ->
                tasksViewModel?.saveTask(
                    organizationId = entry.organizationId,
                    title = title,
                    description = TicketTaskHelper.formatDescriptionWithTicket(entry.ticketCode, ""),
                    priority = priority,
                    onComplete = {
                        Toast.makeText(context, msgTaskCreated, Toast.LENGTH_SHORT).show()
                    }
                )
                entryForQuickTask = null
            },
            onDismiss = { entryForQuickTask = null }
        )
    }

    // Export and Backup Dialog
    if (showExportDialog) {
        ExportBackupDialog(
            organization = organization,
            onExportExcel = { exportToExcel() },
            onExportBackup = { performOrganizationBackup() },
            onRestoreBackup = { restoreBackupLauncher.launch("*/*") },
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun BitacoraCard(
    entry: BitacoraEntry,
    linkedTasks: List<TaskItem> = emptyList(),
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickAddTask: () -> Unit,
    onToggleTask: (TaskItem) -> Unit = {},
    onDeleteTask: (TaskItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expandedTasks by remember { mutableStateOf(false) }
    val pendingTasksCount = linkedTasks.count { !it.isCompleted }

    NotepadSheetCard(
        hasSpiral = true,
        modifier = modifier.clickable { onEdit() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Ticket Code Badge + Date + Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ticket code stamped badge
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = entry.ticketCode,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Formatted date
                    Text(
                        text = BitacoraViewModel.formatDate(entry.dateEpochMillis),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(status = entry.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Work done section (Lo que se hizo ese día)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.work_done_section),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = entry.workDone,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Actions taken section
            if (entry.actionsTaken.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.actions_taken_section),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = entry.actionsTaken,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Observations section
            if (entry.observations.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.observations_section),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = entry.observations,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Linked Pending Tasks Section (Sticky Note strip on card)
            if (linkedTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (pendingTasksCount > 0) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { expandedTasks = !expandedTasks }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AssignmentLate,
                            contentDescription = null,
                            tint = if (pendingTasksCount > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (pendingTasksCount > 0) {
                                stringResource(id = R.string.linked_pending_count, pendingTasksCount)
                            } else {
                                stringResource(id = R.string.stat_completed_tasks)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (expandedTasks) "Ocultar" else "Ver (${linkedTasks.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(visible = expandedTasks) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        linkedTasks.forEach { task ->
                            TicketTaskChecklistItem(
                                task = task,
                                onToggle = { onToggleTask(task) },
                                onDelete = { onDeleteTask(task) }
                            )
                        }
                    }
                }
            }

            // Evidence photo thumbnails section
            if (entry.imagePaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${entry.imagePaths.size} ${if (entry.imagePaths.size == 1) "evidencia" else "evidencias"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(entry.imagePaths) { imagePath ->
                        EvidenceThumbnail(
                            filePath = imagePath,
                            size = 64.dp,
                            onDelete = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NotionDivider)
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Action buttons row: Quick + Pendiente button & Edit/Delete actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick add pending task button directly on card
                OutlinedButton(
                    onClick = onQuickAddTask,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.PlaylistAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.action_quick_add_pending),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.action_edit),
                            tint = NotionBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(id = R.string.action_delete),
                            tint = NotionRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
