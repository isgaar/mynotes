// File: app/src/main/java/com/iotaslife/app/domain/repository/TaskRepository.kt
// Description: Repository contract defining reactive queries and transactional mutation operations for organizational pending tasks.
// All Rights Reserved isgaar

package com.iotaslife.app.domain.repository

import com.iotaslife.app.domain.model.TaskItem
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasksForOrganization(orgId: Long): Flow<List<TaskItem>>
    suspend fun getTaskById(id: Long): TaskItem?
    suspend fun insertTask(task: TaskItem): Long
    suspend fun updateTask(task: TaskItem)
    suspend fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean)
    suspend fun deleteTask(task: TaskItem)
    suspend fun deleteTaskById(id: Long)
}

