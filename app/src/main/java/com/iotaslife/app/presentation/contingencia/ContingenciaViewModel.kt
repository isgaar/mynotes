// File: app/src/main/java/com/iotaslife/app/presentation/contingencia/ContingenciaViewModel.kt
// Description: Reactive ViewModel managing contingency events, real-time query filtering, reimbursement toggling, and expense calculations.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.contingencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iotaslife.app.domain.model.ContingenciaEntry
import com.iotaslife.app.domain.repository.ContingenciaRepository
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

enum class ContingenciaFilter {
    ALL, PENDING, REIMBURSED
}

data class ContingenciaUiState(
    val contingencias: List<ContingenciaEntry> = emptyList(),
    val allEntries: List<ContingenciaEntry> = emptyList(),
    val totalTaxiExpense: Double = 0.0,
    val pendingTaxiExpense: Double = 0.0,
    val reimbursedTaxiExpense: Double = 0.0,
    val allCount: Int = 0,
    val pendingCount: Int = 0,
    val reimbursedCount: Int = 0,
    val searchQuery: String = "",
    val currentFilter: ContingenciaFilter = ContingenciaFilter.ALL,
    val isLoading: Boolean = false
)

class ContingenciaViewModel(
    private val repository: ContingenciaRepository,
    private val orgIdFlow: StateFlow<Long>
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _currentFilter = MutableStateFlow(ContingenciaFilter.ALL)
    val currentFilter: StateFlow<ContingenciaFilter> = _currentFilter

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ContingenciaUiState> = combine(
        orgIdFlow.flatMapLatest { orgId ->
            repository.getContingenciasForOrganization(orgId)
        },
        _searchQuery,
        _currentFilter
    ) { allEntries, query, filter ->
        var totalTaxi = 0.0
        var pendingTaxi = 0.0
        var reimbursedTaxi = 0.0
        var pendingCount = 0
        var reimbursedCount = 0

        for (item in allEntries) {
            totalTaxi += item.taxiCost
            if (item.isReimbursed) {
                reimbursedTaxi += item.taxiCost
                reimbursedCount++
            } else {
                pendingTaxi += item.taxiCost
                pendingCount++
            }
        }

        val filtered = allEntries.filter { item ->
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(item.dateEpochMillis))
            val matchesQuery = if (query.isBlank()) true else {
                val q = query.trim()
                item.title.contains(q, ignoreCase = true) ||
                    item.reason.contains(q, ignoreCase = true) ||
                    item.solution.contains(q, ignoreCase = true) ||
                    item.taxiDetails.contains(q, ignoreCase = true) ||
                    dateStr.contains(q, ignoreCase = true)
            }

            val matchesFilter = when (filter) {
                ContingenciaFilter.ALL -> true
                ContingenciaFilter.PENDING -> !item.isReimbursed
                ContingenciaFilter.REIMBURSED -> item.isReimbursed
            }

            matchesQuery && matchesFilter
        }

        ContingenciaUiState(
            contingencias = filtered,
            allEntries = allEntries,
            totalTaxiExpense = totalTaxi,
            pendingTaxiExpense = pendingTaxi,
            reimbursedTaxiExpense = reimbursedTaxi,
            allCount = allEntries.size,
            pendingCount = pendingCount,
            reimbursedCount = reimbursedCount,
            searchQuery = query,
            currentFilter = filter,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ContingenciaUiState(isLoading = true)
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterChanged(filter: ContingenciaFilter) {
        _currentFilter.value = filter
    }

    fun toggleReimbursed(entry: ContingenciaEntry, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.toggleReimbursed(entry.id, !entry.isReimbursed)
            onComplete()
        }
    }

    fun saveContingencia(entry: ContingenciaEntry, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (entry.id == 0L) {
                repository.insertContingencia(entry)
            } else {
                repository.updateContingencia(entry)
            }
            onComplete()
        }
    }

    fun deleteContingencia(entry: ContingenciaEntry, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteContingencia(entry)
            onComplete()
        }
    }

    companion object {
        fun provideFactory(
            repository: ContingenciaRepository,
            orgIdFlow: StateFlow<Long>
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ContingenciaViewModel(repository, orgIdFlow) as T
            }
        }
    }
}

