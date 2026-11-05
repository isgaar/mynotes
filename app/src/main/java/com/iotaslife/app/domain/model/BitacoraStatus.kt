// File: app/src/main/java/com/iotaslife/app/domain/model/BitacoraStatus.kt
// Description: Enumeration defining lifecycle states for daily work log tickets with dedicated hex color codes and localized labels.
// All Rights Reserved isgaar

package com.iotaslife.app.domain.model

import androidx.annotation.StringRes
import com.iotaslife.app.R

enum class BitacoraStatus(@StringRes val labelRes: Int, val hexColor: Long, val bgHexColor: Long) {
    IN_PROGRESS(R.string.status_in_progress, 0xFF1F78B4, 0xFFEBF5FB),
    COMPLETED(R.string.status_completed, 0xFF2E7D32, 0xFFEAF7EE),
    BLOCKED(R.string.status_blocked, 0xFFC62828, 0xFFFDECEB),
    REVIEW(R.string.status_review, 0xFFB78103, 0xFFFDF6E2)
}

