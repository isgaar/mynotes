// File: app/src/main/java/com/iotaslife/app/data/local/entity/ContingenciaEntity.kt
// Description: Room entity mapping contingency entries with foreign key cascading delete on organization removal and indexes.
// All Rights Reserved isgaar

package com.iotaslife.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.iotaslife.app.domain.model.ContingenciaEntry

@Entity(
    tableName = "contingencias",
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
data class ContingenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val organizationId: Long,
    val title: String,
    val reason: String,
    val solution: String,
    val dateEpochMillis: Long,
    val taxiCost: Double,
    val taxiDetails: String = "",
    val isReimbursed: Boolean = false,
    val imagePathsString: String = "",
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): ContingenciaEntry {
        val imageList = if (imagePathsString.isBlank()) {
            emptyList()
        } else {
            imagePathsString.split(";;;").filter { it.isNotBlank() }
        }
        return ContingenciaEntry(
            id = id,
            organizationId = organizationId,
            title = title,
            reason = reason,
            solution = solution,
            dateEpochMillis = dateEpochMillis,
            taxiCost = taxiCost,
            taxiDetails = taxiDetails,
            isReimbursed = isReimbursed,
            imagePaths = imageList,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(domain: ContingenciaEntry): ContingenciaEntity = ContingenciaEntity(
            id = domain.id,
            organizationId = domain.organizationId,
            title = domain.title,
            reason = domain.reason,
            solution = domain.solution,
            dateEpochMillis = domain.dateEpochMillis,
            taxiCost = domain.taxiCost,
            taxiDetails = domain.taxiDetails,
            isReimbursed = domain.isReimbursed,
            imagePathsString = domain.imagePaths.joinToString(";;;"),
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }
}

