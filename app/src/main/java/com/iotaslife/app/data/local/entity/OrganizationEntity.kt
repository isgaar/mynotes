// File: app/src/main/java/com/iotaslife/app/data/local/entity/OrganizationEntity.kt
// Description: Room entity mapping organizations to SQLite records with unique identification, metadata tags, and timestamp tracking.
// All Rights Reserved isgaar

package com.iotaslife.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iotaslife.app.domain.model.Organization

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val colorHex: String,
    val createdAt: Long
) {
    fun toDomain(): Organization = Organization(
        id = id,
        name = name,
        description = description,
        iconEmoji = iconEmoji,
        colorHex = colorHex,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: Organization): OrganizationEntity = OrganizationEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            iconEmoji = domain.iconEmoji,
            colorHex = domain.colorHex,
            createdAt = domain.createdAt
        )
    }
}

