// File: app/src/main/java/com/iotaslife/app/domain/model/TaskItem.kt
// Description: Domain model representing an immutable to-do item associated with an organization, tracking priority and completion status.
// All Rights Reserved isgaar

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

