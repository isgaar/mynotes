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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

