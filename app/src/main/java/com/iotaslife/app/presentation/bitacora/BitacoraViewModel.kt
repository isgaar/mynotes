package com.iotaslife.app.presentation.bitacora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.domain.model.BitacoraStatus
import com.iotaslife.app.domain.repository.BitacoraRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BitacoraUiState(
    val entries: List<BitacoraEntry> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedStatusFilter: BitacoraStatus? = null
)

class BitacoraViewModel(
    private val repository: BitacoraRepository,
    private val orgIdFlow: StateFlow<Long>
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedStatusFilter = MutableStateFlow<BitacoraStatus?>(null)
    val selectedStatusFilter: StateFlow<BitacoraStatus?> = _selectedStatusFilter

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BitacoraUiState> = combine(
        orgIdFlow.flatMapLatest { orgId ->
            repository.getEntriesForOrganization(orgId)
        },
        _searchQuery,
        _selectedStatusFilter
    ) { entries, query, statusFilter ->
        val filtered = entries.filter { entry ->
            val matchesQuery = if (query.isBlank()) true else {
                entry.title.contains(query, ignoreCase = true) ||
                        entry.ticketCode.contains(query, ignoreCase = true) ||
                        entry.workDone.contains(query, ignoreCase = true) ||
                        entry.observations.contains(query, ignoreCase = true) ||
                        entry.actionsTaken.contains(query, ignoreCase = true)
            }
            val matchesStatus = statusFilter == null || entry.status == statusFilter
            matchesQuery && matchesStatus
        }
        BitacoraUiState(
            entries = filtered,
            searchQuery = query,
            selectedStatusFilter = statusFilter,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BitacoraUiState(isLoading = true)
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onStatusFilterChanged(status: BitacoraStatus?) {
        _selectedStatusFilter.value = status
    }

    fun saveEntry(
        id: Long = 0,
        organizationId: Long,
        ticketCode: String,
        title: String,
        dateEpochMillis: Long,
        workDone: String,
        observations: String,
        actionsTaken: String,
        status: BitacoraStatus,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val entry = BitacoraEntry(
                id = id,
                organizationId = organizationId,
                ticketCode = ticketCode.ifBlank { "TIK-${System.currentTimeMillis() % 10000}" },
                title = title.trim(),
                dateEpochMillis = dateEpochMillis,
                workDone = workDone.trim(),
                observations = observations.trim(),
                actionsTaken = actionsTaken.trim(),
                status = status,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertEntry(entry)
            } else {
                repository.updateEntry(entry)
            }
            onComplete()
        }
    }

    fun deleteEntry(entry: BitacoraEntry, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            onComplete()
        }
    }

    fun deleteEntryById(id: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteEntryById(id)
            onComplete()
        }
    }

    companion object {
        fun formatDate(millis: Long): String {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return sdf.format(Date(millis))
        }

        fun provideFactory(
            repository: BitacoraRepository,
            orgIdFlow: StateFlow<Long>
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BitacoraViewModel(repository, orgIdFlow) as T
            }
        }
    }
}

