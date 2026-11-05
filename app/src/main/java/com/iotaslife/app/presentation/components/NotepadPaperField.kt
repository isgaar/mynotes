// File: app/src/main/java/com/iotaslife/app/presentation/components/NotepadPaperField.kt
// Description: Skeuomorphic lined notebook text area with ruled paper background, margin lines, and Material You dynamic theming.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iotaslife.app.presentation.theme.NotionBorder
import com.iotaslife.app.presentation.theme.NotionRed

@Composable
fun NotepadPaperField(
    title: String,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 130.dp,
    isError: Boolean = false,
    headerColor: Color = MaterialTheme.colorScheme.primary,
    badgeText: String? = null
) {
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    // Ruled paper line colors
    val paperBg = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFFCFBF7) // Warm cream notepad paper
    }

    val ruledLineColor = if (isDark) {
        Color(0xFF2E3236).copy(alpha = 0.6f)
    } else {
        Color(0xFFE2E7ED).copy(alpha = 0.8f)
    }

    val marginLineColor = if (isDark) {
        Color(0xFF8B3A3A).copy(alpha = 0.5f)
    } else {
        Color(0xFFF19E9E).copy(alpha = 0.6f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .background(paperBg, RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isError) NotionRed
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        // Section Header with Notebook Tab Marker
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .background(headerColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = headerColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (badgeText != null) {
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(headerColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = headerColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ruled paper canvas behind text area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(10.dp))
                .notepadRuledPaper(
                    lineSpacing = 26.dp,
                    marginOffset = 30.dp,
                    lineColor = ruledLineColor,
                    marginColor = marginLineColor,
                    showMargin = true
                )
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                isError = isError,
                placeholder = {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth().height(height),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = headerColor.copy(alpha = 0.7f),
                    unfocusedBorderColor = NotionBorder.copy(alpha = 0.5f)
                )
            )
        }
    }
}
