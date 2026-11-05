// File: app/src/main/java/com/iotaslife/app/presentation/tasks/TasksScreen.kt
// Description: Main tasks management screen featuring Notion-style animated checkboxes, category tabs, and priority indicators.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.tasks

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.iotaslife.app.domain.model.TaskItem
import com.iotaslife.app.presentation.components.ConfirmDeleteDialog
import com.iotaslife.app.presentation.components.PriorityChip
import com.iotaslife.app.presentation.theme.NotionBlue
import com.iotaslife.app.presentation.theme.NotionBorder
import com.iotaslife.app.presentation.theme.NotionGreen
import com.iotaslife.app.presentation.theme.NotionRed

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    organizationId: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }

    val msgTaskSaved = stringResource(id = R.string.msg_task_saved)
    val msgTaskUpdated = stringResource(id = R.string.msg_task_updated)
    val msgTaskDeleted = stringResource(id = R.string.msg_task_deleted)
    val msgTaskCompleted = stringResource(id = R.string.msg_task_completed)
    val msgTaskPending = stringResource(id = R.string.msg_task_pending)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.tasks_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.tasks_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Tabs: All, Pending, Completed
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isSelected = uiState.currentFilter == TaskFilter.ALL
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFilterChanged(TaskFilter.ALL) },
                        label = {
                            Text(
                                text = stringResource(
                                    id = R.string.pending_filter_all,
                                    uiState.allCount
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NotionBlue.copy(alpha = 0.15f),
                            selectedLabelColor = NotionBlue
                        )
                    )
                }
                item {
                    val isSelected = uiState.currentFilter == TaskFilter.PENDING
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFilterChanged(TaskFilter.PENDING) },
                        label = {
                            Text(
                                text = stringResource(
                                    id = R.string.pending_filter_pending,
                                    uiState.pendingCount
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NotionBlue.copy(alpha = 0.15f),
                            selectedLabelColor = NotionBlue
                        )
                    )
                }
                item {
                    val isSelected = uiState.currentFilter == TaskFilter.COMPLETED
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFilterChanged(TaskFilter.COMPLETED) },
                        label = {
                            Text(
                                text = stringResource(
                                    id = R.string.pending_filter_completed,
                                    uiState.completedCount
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NotionGreen.copy(alpha = 0.15f),
                            selectedLabelColor = NotionGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Task List
            if (uiState.tasks.isEmpty()) {
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
                                imageVector = Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.empty_tasks_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(id = R.string.empty_tasks_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.tasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggle = {
                                val nextState = !task.isCompleted
                                viewModel.toggleCompleted(task)
                                Toast.makeText(
                                    context,
                                    if (nextState) msgTaskCompleted else msgTaskPending,
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onEdit = { editingTask = task },
                            onDelete = { taskToDelete = task }
                        )
                    }
                }
            }
        }

        // FAB to create task with Material You colors
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.new_task)
            )
        }
    }

    // Create Task Dialog
    if (showCreateDialog) {
        TaskEditorDialog(
            task = null,
            organizationId = organizationId,
            onSave = { title, desc, priority ->
                viewModel.saveTask(
                    organizationId = organizationId,
                    title = title,
                    description = desc,
                    priority = priority
                ) {
                    Toast.makeText(context, msgTaskSaved, Toast.LENGTH_SHORT).show()
                }
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Edit Task Dialog
    editingTask?.let { task ->
        TaskEditorDialog(
            task = task,
            organizationId = organizationId,
            onSave = { title, desc, priority ->
                viewModel.saveTask(
                    id = task.id,
                    organizationId = organizationId,
                    title = title,
                    description = desc,
                    priority = priority,
                    isCompleted = task.isCompleted
                ) {
                    Toast.makeText(context, msgTaskUpdated, Toast.LENGTH_SHORT).show()
                }
                editingTask = null
            },
            onDismiss = { editingTask = null }
        )
    }

    // Delete Confirmation Dialog
    taskToDelete?.let { task ->
        ConfirmDeleteDialog(
            title = stringResource(id = R.string.delete_confirm_title),
            message = stringResource(id = R.string.delete_confirm_msg),
            onConfirm = {
                viewModel.deleteTask(task) {
                    Toast.makeText(context, msgTaskDeleted, Toast.LENGTH_SHORT).show()
                }
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NotionBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notion-style checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (task.isCompleted) NotionGreen else Color.Transparent
                    )
                    .border(
                        width = if (task.isCompleted) 0.dp else 1.5.dp,
                        color = if (task.isCompleted) Color.Transparent else NotionBorder,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggle() }
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                PriorityChip(priority = task.priority)
            }

            // Quick actions
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(id = R.string.action_edit),
                    tint = NotionBlue,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(id = R.string.action_delete),
                    tint = NotionRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

