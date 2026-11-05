package com.iotaslife.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iotaslife.app.domain.model.TaskItem
import com.iotaslife.app.domain.model.TaskPriority

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = OrganizationEntity::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["organizationId"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val organizationId: Long,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priorityName: String,
    val createdAt: Long
) {
    fun toDomain(): TaskItem {
        val priority = try {
            TaskPriority.valueOf(priorityName)
        } catch (_: Exception) {
            TaskPriority.MEDIUM
        }
        return TaskItem(
            id = id,
            organizationId = organizationId,
            title = title,
            description = description,
            isCompleted = isCompleted,
            priority = priority,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(domain: TaskItem): TaskEntity = TaskEntity(
            id = domain.id,
            organizationId = domain.organizationId,
            title = domain.title,
            description = domain.description,
            isCompleted = domain.isCompleted,
            priorityName = domain.priority.name,
            createdAt = domain.createdAt
        )
    }
}

