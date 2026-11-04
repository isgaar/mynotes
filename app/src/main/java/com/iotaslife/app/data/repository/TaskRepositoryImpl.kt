package com.iotaslife.app.data.repository

import com.iotaslife.app.data.local.dao.TaskDao
import com.iotaslife.app.data.local.entity.TaskEntity
import com.iotaslife.app.domain.model.TaskItem
import com.iotaslife.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val dao: TaskDao
) : TaskRepository {

    override fun getTasksForOrganization(orgId: Long): Flow<List<TaskItem>> {
        return dao.getTasksForOrganization(orgId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: Long): TaskItem? {
        return dao.getTaskById(id)?.toDomain()
    }

    override suspend fun insertTask(task: TaskItem): Long {
        return dao.insert(TaskEntity.fromDomain(task))
    }

    override suspend fun updateTask(task: TaskItem) {
        dao.update(TaskEntity.fromDomain(task))
    }

    override suspend fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean) {
        dao.updateCompleted(taskId, isCompleted)
    }

    override suspend fun deleteTask(task: TaskItem) {
        dao.delete(TaskEntity.fromDomain(task))
    }

    override suspend fun deleteTaskById(id: Long) {
        dao.deleteById(id)
    }
}
