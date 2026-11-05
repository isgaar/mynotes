// File: app/src/main/java/com/iotaslife/app/presentation/main/MainScreen.kt
// Description: Root scaffold composable managing bottom navigation between Bitacora and Pendientes tabs and organization switching.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.iotaslife.app.R
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.presentation.bitacora.BitacoraEditorScreen
import com.iotaslife.app.presentation.bitacora.BitacoraListScreen
import com.iotaslife.app.presentation.bitacora.BitacoraViewModel
import com.iotaslife.app.presentation.components.NotionTopBar
import com.iotaslife.app.presentation.components.OrganizationSelectorDialog
import com.iotaslife.app.presentation.tasks.TasksScreen
import com.iotaslife.app.presentation.tasks.TasksViewModel
import com.iotaslife.app.presentation.theme.NotionBorder

sealed class CurrentScreen {
    object Main : CurrentScreen()
    data class BitacoraEditor(val entry: BitacoraEntry?) : CurrentScreen()
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    bitacoraViewModel: BitacoraViewModel,
    tasksViewModel: TasksViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mainUiState by mainViewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf<CurrentScreen>(CurrentScreen.Main) }
    var showOrgDialog by remember { mutableStateOf(false) }
    val msgOrgCreated = stringResource(id = R.string.msg_org_created)

    when (val screen = currentScreen) {
        is CurrentScreen.BitacoraEditor -> {
            BitacoraEditorScreen(
                entry = screen.entry,
                organizationId = mainUiState.selectedOrg?.id ?: 1L,
                onSaveSuccess = { currentScreen = CurrentScreen.Main },
                onBack = { currentScreen = CurrentScreen.Main },
                viewModel = bitacoraViewModel
            )
        }
        CurrentScreen.Main -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                topBar = {
                    NotionTopBar(
                        organization = mainUiState.selectedOrg,
                        onSwitchOrgClick = { showOrgDialog = true }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        NavigationBarItem(
                            selected = mainUiState.selectedTab == 0,
                            onClick = { mainViewModel.selectTab(0) },
                            icon = {
                                Icon(
                                    imageVector = if (mainUiState.selectedTab == 0) Icons.AutoMirrored.Filled.Assignment else Icons.AutoMirrored.Outlined.Assignment,
                                    contentDescription = stringResource(id = R.string.nav_bitacora)
                                )
                            },
                            label = { Text(text = stringResource(id = R.string.nav_bitacora)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = mainUiState.selectedTab == 1,
                            onClick = { mainViewModel.selectTab(1) },
                            icon = {
                                Icon(
                                    imageVector = if (mainUiState.selectedTab == 1) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                    contentDescription = stringResource(id = R.string.nav_pendientes)
                                )
                            },
                            label = { Text(text = stringResource(id = R.string.nav_pendientes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val orgId = mainUiState.selectedOrg?.id ?: 1L
                    if (mainUiState.selectedTab == 0) {
                        BitacoraListScreen(
                            viewModel = bitacoraViewModel,
                            organization = mainUiState.selectedOrg,
                            onAddEntry = {
                                currentScreen = CurrentScreen.BitacoraEditor(null)
                            },
                            onEditEntry = { entry ->
                                currentScreen = CurrentScreen.BitacoraEditor(entry)
                            }
                        )
                    } else {
                        TasksScreen(
                            viewModel = tasksViewModel,
                            organizationId = orgId
                        )
                    }
                }
            }
        }
    }

    if (showOrgDialog) {
        OrganizationSelectorDialog(
            organizations = mainUiState.organizations,
            selectedOrg = mainUiState.selectedOrg,
            onSelectOrg = { org -> mainViewModel.selectOrganization(org) },
            onCreateOrg = { name, desc, emoji ->
                mainViewModel.createOrganization(name, desc, emoji)
                Toast.makeText(context, msgOrgCreated, Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showOrgDialog = false }
        )
    }
}

