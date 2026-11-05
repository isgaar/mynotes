package com.iotaslife.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iotaslife.app.domain.model.BitacoraEntry
import com.iotaslife.app.domain.model.BitacoraStatus

@Entity(
    tableName = "bitacoras",
    foreignKeys = [
        ForeignKey(
            entity = OrganizationEntity::class,
            parentColumns = ["id"],
            childColumns = ["organizationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["organizationId"]),
        Index(value = ["ticketCode"])
    ]
)
data class BitacoraEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val organizationId: Long,
    val ticketCode: String,
    val title: String,
    val dateEpochMillis: Long,
    val workDone: String,
    val observations: String,
    val actionsTaken: String,
    val statusName: String,
    val imagePathsString: String = "",
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): BitacoraEntry {
        val status = try {
            BitacoraStatus.valueOf(statusName)
        } catch (_: Exception) {
            BitacoraStatus.IN_PROGRESS
        }
        val imageList = if (imagePathsString.isBlank()) {
            emptyList()
        } else {
            imagePathsString.split(";;;").filter { it.isNotBlank() }
        }
        return BitacoraEntry(
            id = id,
            organizationId = organizationId,
            ticketCode = ticketCode,
            title = title,
            dateEpochMillis = dateEpochMillis,
            workDone = workDone,
            observations = observations,
            actionsTaken = actionsTaken,
            status = status,
            imagePaths = imageList,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: BitacoraEntry): BitacoraEntity = BitacoraEntity(
            id = domain.id,
            organizationId = domain.organizationId,
            ticketCode = domain.ticketCode,
            title = domain.title,
            dateEpochMillis = domain.dateEpochMillis,
            workDone = domain.workDone,
            observations = domain.observations,
            actionsTaken = domain.actionsTaken,
            statusName = domain.status.name,
            imagePathsString = domain.imagePaths.joinToString(";;;"),
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}
