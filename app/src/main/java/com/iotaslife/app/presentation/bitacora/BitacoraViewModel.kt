// File: app/src/main/java/com/iotaslife/app/presentation/bitacora/BitacoraViewModel.kt
// Description: Reactive ViewModel managing bitacora state, real-time multi-field search, status filtering, and date query evaluation.
// All Rights Reserved isgaar

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
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class BitacoraUiState(
    val entries: List<BitacoraEntry> = emptyList(),
    val rawEntries: List<BitacoraEntry> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedStatusFilter: BitacoraStatus? = null,
    val filterTodayOnly: Boolean = false
)

class BitacoraViewModel(
    private val repository: BitacoraRepository,
    private val orgIdFlow: StateFlow<Long>
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedStatusFilter = MutableStateFlow<BitacoraStatus?>(null)
    val selectedStatusFilter: StateFlow<BitacoraStatus?> = _selectedStatusFilter

    private val _filterTodayOnly = MutableStateFlow(false)
    val filterTodayOnly: StateFlow<Boolean> = _filterTodayOnly

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BitacoraUiState> = combine(
        orgIdFlow.flatMapLatest { orgId ->
            repository.getEntriesForOrganization(orgId)
        },
        _searchQuery,
        _selectedStatusFilter,
        _filterTodayOnly
    ) { entries, query, statusFilter, todayOnly ->
        val nowMillis = System.currentTimeMillis()
        val filtered = entries.filter { entry ->
            val formattedDate = formatDate(entry.dateEpochMillis)
            val matchesQuery = if (query.isBlank()) true else {
                val q = query.trim()
                entry.title.contains(q, ignoreCase = true) ||
                        entry.ticketCode.contains(q, ignoreCase = true) ||
                        entry.workDone.contains(q, ignoreCase = true) ||
                        entry.observations.contains(q, ignoreCase = true) ||
                        entry.actionsTaken.contains(q, ignoreCase = true) ||
                        formattedDate.contains(q, ignoreCase = true) ||
                        (q.equals("hoy", ignoreCase = true) && isSameDay(entry.dateEpochMillis, nowMillis))
            }
            val matchesStatus = statusFilter == null || entry.status == statusFilter
            val matchesToday = !todayOnly || isSameDay(entry.dateEpochMillis, nowMillis)

            matchesQuery && matchesStatus && matchesToday
        }
        BitacoraUiState(
            entries = filtered,
            rawEntries = entries,
            searchQuery = query,
            selectedStatusFilter = statusFilter,
            filterTodayOnly = todayOnly,
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

    fun toggleTodayFilter() {
        _filterTodayOnly.value = !_filterTodayOnly.value
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
        imagePaths: List<String> = emptyList(),
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
                imagePaths = imagePaths,
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

        fun isSameDay(millis1: Long, millis2: Long): Boolean {
            val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
            val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
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
