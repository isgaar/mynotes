package com.iotaslife.app.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.iotaslife.app.IotasLifeApp
import com.iotaslife.app.presentation.bitacora.BitacoraViewModel
import com.iotaslife.app.presentation.tasks.TasksViewModel
import com.iotaslife.app.presentation.theme.IotasLifeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as IotasLifeApp
        val container = app.container

        val mainViewModel: MainViewModel by viewModels {
            MainViewModel.provideFactory(container.organizationRepository)
        }

        val bitacoraViewModel: BitacoraViewModel by viewModels {
            BitacoraViewModel.provideFactory(
                container.bitacoraRepository,
                mainViewModel.selectedOrgId
            )
        }

        val tasksViewModel: TasksViewModel by viewModels {
            TasksViewModel.provideFactory(
                container.taskRepository,
                mainViewModel.selectedOrgId
            )
        }

        setContent {
            IotasLifeTheme {
                MainScreen(
                    mainViewModel = mainViewModel,
                    bitacoraViewModel = bitacoraViewModel,
                    tasksViewModel = tasksViewModel
                )
            }
        }
    }
}
