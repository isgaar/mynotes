// File: app/src/main/java/com/iotaslife/app/domain/model/Organization.kt
// Description: Immutable domain entity representing a company or organization workspace that isolates daily work logs and pending tasks.
// All Rights Reserved isgaar

package com.iotaslife.app.domain.model

data class Organization(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconEmoji: String = "CORP",
    val colorHex: String = "#333333",
    val createdAt: Long = System.currentTimeMillis()
)
