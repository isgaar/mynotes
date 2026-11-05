// File: app/src/main/java/com/iotaslife/app/domain/model/TaskPriority.kt
// Description: Enumeration representing task priority tiers with associated pastel background and label colors for Notion-style rendering.
// All Rights Reserved isgaar

package com.iotaslife.app.domain.model

import androidx.annotation.StringRes
import com.iotaslife.app.R

enum class TaskPriority(@StringRes val labelRes: Int, val hexColor: Long, val bgHexColor: Long) {
    LOW(R.string.priority_low, 0xFF2E7D32, 0xFFEAF7EE),
    MEDIUM(R.string.priority_medium, 0xFFB78103, 0xFFFDF6E2),
    HIGH(R.string.priority_high, 0xFFC62828, 0xFFFDECEB)
}

