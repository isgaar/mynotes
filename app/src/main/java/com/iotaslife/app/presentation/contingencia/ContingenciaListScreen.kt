// File: app/src/main/java/com/iotaslife/app/presentation/contingencia/ContingenciaListScreen.kt
// Description: Composable screen listing urgent contingency visits, taxi expenses, photo receipts, and boss expense justification report.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.contingencia

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iotaslife.app.R
import com.iotaslife.app.domain.model.ContingenciaEntry
import com.iotaslife.app.domain.model.Organization
import com.iotaslife.app.presentation.components.ConfirmDeleteDialog
import com.iotaslife.app.presentation.components.EvidenceThumbnail
import com.iotaslife.app.presentation.theme.NotionBlue
import com.iotaslife.app.presentation.theme.NotionBorder
import com.iotaslife.app.presentation.theme.NotionGreen
import com.iotaslife.app.presentation.theme.NotionRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContingenciaListScreen(
    viewModel: ContingenciaViewModel,
    organization: Organization?,
    onAddContingencia: () -> Unit,
    onEditContingencia: (ContingenciaEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var entryToDelete by remember { mutableStateOf<ContingenciaEntry?>(null) }

    val msgDeleted = stringResource(id = R.string.msg_contingencia_deleted)
    val msgToggled = stringResource(id = R.string.msg_contingencia_reimbursed_toggled)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title & Subtitle
            Text(
                text = stringResource(id = R.string.contingencia_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.contingencia_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Card (Expense Justification for Boss)
            ContingenciaExpenseSummaryCard(
                totalSpent = uiState.totalTaxiExpense,
                pendingSpent = uiState.pendingTaxiExpense,
                reimbursedSpent = uiState.reimbursedTaxiExpense,
                orgName = organization?.name ?: "Empresa",
                entries = uiState.allEntries,
                onShare = { reportText ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Reporte de Gastos por Contingencia")
                        putExtra(Intent.EXTRA_TEXT, reportText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir reporte de gastos con el jefe"))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.contingencia_search_hint),
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
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NotionBlue,
                    unfocusedBorderColor = NotionBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isSelected = uiState.currentFilter == ContingenciaFilter.ALL
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFilterChanged(ContingenciaFilter.ALL) },
                        label = {
                            Text(
                                text = stringResource(
                                    id = R.string.contingencia_filter_all,
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
                    val isSelected = uiState.currentFilter == ContingenciaFilter.PENDING
                    val isSelectedFilter = uiState.currentFilter == ContingenciaFilter.PENDING
                    FilterChip(
                        selected = isSelectedFilter,
                        onClick = { viewModel.onFilterChanged(ContingenciaFilter.PENDING) },
                        label = {
                            Text(
                                text = stringResource(
                                    id = R.string.contingencia_filter_pending,
                                    uiState.pendingCount
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NotionRed.copy(alpha = 0.15f),
                            selectedLabelColor = NotionRed
                        )
                    )
                }
                item {
                    val isSelected = uiState.currentFilter == ContingenciaFilter.REIMBURSED
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onFilterChanged(ContingenciaFilter.REIMBURSED) },
                        label = {
                            Text(
                                text = stringResource(
                                    id = R.string.contingencia_filter_reimbursed,
                                    uiState.reimbursedCount
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

            Spacer(modifier = Modifier.height(10.dp))

            // Content List or Empty State
            if (uiState.contingencias.isEmpty()) {
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
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.contingencia_empty_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(id = R.string.contingencia_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.contingencias, key = { it.id }) { item ->
                        ContingenciaCard(
                            entry = item,
                            onToggleReimbursed = {
                                viewModel.toggleReimbursed(item) {
                                    Toast.makeText(context, msgToggled, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEdit = { onEditContingencia(item) },
                            onDelete = { entryToDelete = item }
                        )
                    }
                }
            }
        }

        // Floating Action Button to create a new contingency
        FloatingActionButton(
            onClick = onAddContingencia,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.new_contingencia)
            )
        }
    }

    // Delete Confirmation Dialog
    entryToDelete?.let { entry ->
        ConfirmDeleteDialog(
            title = stringResource(id = R.string.delete_contingencia_confirm_title),
            message = stringResource(id = R.string.delete_contingencia_confirm_msg),
            onConfirm = {
                viewModel.deleteContingencia(entry) {
                    Toast.makeText(context, msgDeleted, Toast.LENGTH_SHORT).show()
                }
                entryToDelete = null
            },
            onDismiss = { entryToDelete = null }
        )
    }
}

@Composable
fun ContingenciaExpenseSummaryCard(
    totalSpent: Double,
    pendingSpent: Double,
    reimbursedSpent: Double,
    orgName: String,
    entries: List<ContingenciaEntry>,
    onShare: (String) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF7F8FA)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .shadow(2.dp, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(NotionGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = NotionGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.contingencia_summary_card_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (entries.isNotEmpty()) {
                    Button(
                        onClick = {
                            val report = buildExpenseReport(orgName, totalSpent, pendingSpent, reimbursedSpent, entries)
                            onShare(report)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NotionBlue.copy(alpha = 0.15f),
                            contentColor = NotionBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.contingencia_share_report),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.contingencia_total_spent),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(totalSpent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = stringResource(id = R.string.contingencia_pending_spent),
                        style = MaterialTheme.typography.bodySmall,
                        color = NotionRed
                    )
                    Text(
                        text = currencyFormat.format(pendingSpent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NotionRed
                    )
                }

                Column {
                    Text(
                        text = stringResource(id = R.string.contingencia_reimbursed_spent),
                        style = MaterialTheme.typography.bodySmall,
                        color = NotionGreen
                    )
                    Text(
                        text = currencyFormat.format(reimbursedSpent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NotionGreen
                    )
                }
            }
        }
    }
}

@Composable
fun ContingenciaCard(
    entry: ContingenciaEntry,
    onToggleReimbursed: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(entry.dateEpochMillis))
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFAFAF7)
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (entry.isReimbursed) NotionGreen.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .shadow(2.dp, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Date + Status Badge + Taxi Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Taxi Cost Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NotionGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = NotionGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currencyFormat.format(entry.taxiCost),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NotionGreen
                            )
                        }
                    }

                    // Reimbursed Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (entry.isReimbursed) NotionGreen.copy(alpha = 0.2f)
                                else NotionRed.copy(alpha = 0.15f)
                            )
                            .clickable { onToggleReimbursed() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (entry.isReimbursed) stringResource(id = R.string.contingencia_status_reimbursed)
                            else stringResource(id = R.string.contingencia_status_pending),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (entry.isReimbursed) NotionGreen else NotionRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Motivo de Presencia Inmediata
            if (entry.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = NotionRed,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Taxi route details
            if (entry.taxiDetails.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.taxiDetails,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Solución Realizada
            if (entry.solution.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = entry.solution,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Thumbnails of photo receipts & evidence
            if (entry.imagePaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(entry.imagePaths) { path ->
                        EvidenceThumbnail(
                            filePath = path,
                            size = 54.dp,
                            onDelete = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to toggle reimbursed status
                Button(
                    onClick = onToggleReimbursed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (entry.isReimbursed) MaterialTheme.colorScheme.surfaceVariant
                        else NotionGreen.copy(alpha = 0.2f),
                        contentColor = if (entry.isReimbursed) MaterialTheme.colorScheme.onSurfaceVariant
                        else NotionGreen
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = if (entry.isReimbursed) Icons.Outlined.CheckCircleOutline else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (entry.isReimbursed) stringResource(id = R.string.contingencia_mark_pending)
                        else stringResource(id = R.string.contingencia_mark_reimbursed),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.action_edit),
                            tint = NotionBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
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
    }
}

private fun buildExpenseReport(
    orgName: String,
    totalSpent: Double,
    pendingSpent: Double,
    reimbursedSpent: Double,
    entries: List<ContingenciaEntry>
): String {
    val currency = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val sb = StringBuilder()
    sb.appendLine("*Reporte de Gastos por Contingencia - $orgName*")
    sb.appendLine("Total Acumulado en Taxis: ${currency.format(totalSpent)}")
    sb.appendLine("Pendiente de Reembolso: ${currency.format(pendingSpent)}")
    sb.appendLine("Ya Reembolsado: ${currency.format(reimbursedSpent)}")
    sb.appendLine("----------------------------------------")

    entries.forEachIndexed { index, item ->
        val date = dateFormat.format(Date(item.dateEpochMillis))
        val status = if (item.isReimbursed) "REEMBOLSADO" else "PENDIENTE"
        sb.appendLine("[${index + 1}] $date - ${item.title}")
        sb.appendLine("• Motivo presencia inmediata: ${item.reason}")
        if (item.taxiDetails.isNotBlank()) {
            sb.appendLine("• Trayecto taxi: ${item.taxiDetails}")
        }
        sb.appendLine("• Costo Taxi: ${currency.format(item.taxiCost)} ($status)")
        if (item.solution.isNotBlank()) {
            sb.appendLine("• Solución aplicada: ${item.solution}")
        }
        if (item.imagePaths.isNotEmpty()) {
            sb.appendLine("• Evidencias/Comprobantes: ${item.imagePaths.size} fotos adjuntas")
        }
        sb.appendLine()
    }
    return sb.toString().trimEnd()
}

