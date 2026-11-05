// File: app/src/main/java/com/iotaslife/app/domain/model/BitacoraEntry.kt
// Description: Domain model representing an immutable daily work log ticket including work done, observations, actions taken, and image evidence paths.
// All Rights Reserved isgaar

package com.iotaslife.app.domain.model

data class BitacoraEntry(
    val id: Long = 0,
    val organizationId: Long,
    val ticketCode: String,
    val title: String,
    val dateEpochMillis: Long,
    val workDone: String,
    val observations: String,
    val actionsTaken: String,
    val status: BitacoraStatus = BitacoraStatus.IN_PROGRESS,
    val imagePaths: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
