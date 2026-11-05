package com.iotaslife.app.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iotaslife.app.domain.model.TaskItem
import com.iotaslife.app.domain.model.TaskPriority
import com.iotaslife.app.domain.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL, PENDING, COMPLETED
}

data class TasksUiState(
    val tasks: List<TaskItem> = emptyList(),
    val allCount: Int = 0,
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = false
)

class TasksViewModel(
    private val repository: TaskRepository,
    private val orgIdFlow: StateFlow<Long>
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TasksUiState> = combine(
        orgIdFlow.flatMapLatest { orgId ->
            repository.getTasksForOrganization(orgId)
        },
        _currentFilter
    ) { allTasks, filter ->
        val pendingCount = allTasks.count { !it.isCompleted }
        val completedCount = allTasks.count { it.isCompleted }
        val filtered = when (filter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.PENDING -> allTasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> allTasks.filter { it.isCompleted }
        }
        TasksUiState(
            tasks = filtered,
            allCount = allTasks.size,
            pendingCount = pendingCount,
            completedCount = completedCount,
            currentFilter = filter,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState(isLoading = true)
    )

    fun onFilterChanged(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun toggleCompleted(task: TaskItem) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun saveTask(
        id: Long = 0,
        organizationId: Long,
        title: String,
        description: String,
        priority: TaskPriority,
        isCompleted: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val task = TaskItem(
                id = id,
                organizationId = organizationId,
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                isCompleted = isCompleted
            )
            if (id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
            onComplete()
        }
    }

    fun deleteTask(task: TaskItem, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTask(task)
            onComplete()
        }
    }

    companion object {
        fun provideFactory(
            repository: TaskRepository,
            orgIdFlow: StateFlow<Long>
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TasksViewModel(repository, orgIdFlow) as T
            }
        }
    }
}

