// File: app/src/main/java/com/iotaslife/app/domain/model/ContingenciaEntry.kt
// Description: Immutable domain model representing urgent on-site contingency visits and taxi expense justification.
// All Rights Reserved isgaar

package com.iotaslife.app.domain.model

data class ContingenciaEntry(
    val id: Long = 0,
    val organizationId: Long,
    val title: String,
    val reason: String,
    val solution: String,
    val dateEpochMillis: Long,
    val taxiCost: Double,
    val taxiDetails: String = "",
    val isReimbursed: Boolean = false,
    val imagePaths: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

