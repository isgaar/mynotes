package com.iotaslife.app.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iotaslife.app.domain.model.Organization
import com.iotaslife.app.domain.repository.OrganizationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val organizations: List<Organization> = emptyList(),
    val selectedOrg: Organization? = null,
    val selectedTab: Int = 0 // 0: Bitacora, 1: Pendientes
)

class MainViewModel(
    private val orgRepository: OrganizationRepository
) : ViewModel() {

    private val _selectedOrgId = MutableStateFlow<Long>(0L)
    val selectedOrgId: StateFlow<Long> = _selectedOrgId

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    val uiState: StateFlow<MainUiState> = combine(
        orgRepository.getAllOrganizations(),
        _selectedOrgId,
        _selectedTab
    ) { orgs, selectedId, tab ->
        val selectedOrg = orgs.find { it.id == selectedId } ?: orgs.firstOrNull()
        if (selectedOrg != null && _selectedOrgId.value != selectedOrg.id) {
            _selectedOrgId.value = selectedOrg.id
        }
        MainUiState(
            organizations = orgs,
            selectedOrg = selectedOrg,
            selectedTab = tab
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun selectOrganization(org: Organization) {
        _selectedOrgId.value = org.id
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun createOrganization(name: String, description: String, emoji: String) {
        viewModelScope.launch {
            val newOrg = Organization(
                name = name,
                description = description,
                iconEmoji = emoji
            )
            val id = orgRepository.insertOrganization(newOrg)
            _selectedOrgId.value = id
        }
    }

    companion object {
        fun provideFactory(
            orgRepository: OrganizationRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(orgRepository) as T
            }
        }
    }
}

