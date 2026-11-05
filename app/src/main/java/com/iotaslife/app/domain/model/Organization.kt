package com.iotaslife.app.domain.model

data class Organization(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconEmoji: String = "🏢",
    val colorHex: String = "#333333",
    val createdAt: Long = System.currentTimeMillis()
)

