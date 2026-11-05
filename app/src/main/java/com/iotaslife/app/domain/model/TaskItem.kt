package com.iotaslife.app.domain.model

data class TaskItem(
    val id: Long = 0,
    val organizationId: Long,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val createdAt: Long = System.currentTimeMillis()
)

