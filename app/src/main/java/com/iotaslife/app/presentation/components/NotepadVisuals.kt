// File: app/src/main/java/com/iotaslife/app/presentation/components/NotepadVisuals.kt
// Description: Skeuomorphic notebook visual components combining physical notepad aesthetics with Material You dynamic colors.
// All Rights Reserved isgaar

package com.iotaslife.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Realistic notebook spiral coil binding with metallic wire loops and punch holes.
 */
@Composable
fun NotebookSpiralHeader(
    modifier: Modifier = Modifier,
    ringCount: Int = 14,
    height: Dp = 22.dp
) {
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    // Metallic ring gradient
    val ringGradient = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color(0xFF888888),
                Color(0xFFCCCCCC),
                Color(0xFF555555),
                Color(0xFF2A2A2A)
            )
        } else {
            listOf(
                Color(0xFFA0A5AA),
                Color(0xFFFFFFFF),
                Color(0xFF7E848B),
                Color(0xFF4C5158)
            )
        }
    )

    val holeColor = if (isDark) Color(0xFF101010) else Color(0xFFD0CEC9)
    val holeHighlight = if (isDark) Color(0xFF333333) else Color(0xFFFFFFFF)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height
        val step = width / (ringCount + 1)
        val holeWidth = 8.dp.toPx()
        val holeHeight = 12.dp.toPx()
        val wireWidth = 3.5.dp.toPx()

        for (i in 1..ringCount) {
            val centerX = i * step
            val holeTop = (canvasHeight - holeHeight) / 2

            // Draw punch hole with subtle 3D depth
            drawRoundRect(
                color = holeHighlight,
                topLeft = Offset(centerX - holeWidth / 2, holeTop + 1f),
                size = Size(holeWidth, holeHeight),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
            drawRoundRect(
                color = holeColor,
                topLeft = Offset(centerX - holeWidth / 2, holeTop),
                size = Size(holeWidth, holeHeight),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Draw metallic wire loop threading through hole
            val wirePath = Path().apply {
                moveTo(centerX - holeWidth / 2 - 2f, canvasHeight)
                cubicTo(
                    centerX - holeWidth / 2 - 4f, 2f,
                    centerX + holeWidth / 2 + 4f, 2f,
                    centerX + holeWidth / 2 + 2f, canvasHeight
                )
            }

            // Wire shadow
            drawPath(
                path = wirePath,
                color = Color.Black.copy(alpha = if (isDark) 0.5f else 0.25f),
                style = Stroke(width = wireWidth + 1.5f, cap = StrokeCap.Round)
            )

            // Wire metallic stroke
            drawPath(
                path = wirePath,
                brush = ringGradient,
                style = Stroke(width = wireWidth, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Skeuomorphic perforated tear-off top edge.
 */
@Composable
fun PerforatedTearEdge(
    modifier: Modifier = Modifier,
    toothRadius: Dp = 3.5.dp,
    spacing: Dp = 10.dp
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val radiusPx = toothRadius.toPx()
        val spacingPx = spacing.toPx()
        val toothCount = (size.width / (spacingPx)).toInt()

        for (i in 0..toothCount) {
            val cx = i * spacingPx + spacingPx / 2
            drawCircle(
                color = if (isDark) Color(0xFF141414) else Color(0xFFE6E4DF),
                radius = radiusPx,
                center = Offset(cx, 0f)
            )
        }

        // Dashed perforation guide line
        drawLine(
            color = outlineColor,
            start = Offset(0f, 4.dp.toPx()),
            end = Offset(size.width, 4.dp.toPx()),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )
    }
}

/**
 * Modifier that draws authentic notebook ruled lines with an optional vertical margin rule.
 */
fun Modifier.notepadRuledPaper(
    lineSpacing: Dp = 26.dp,
    marginOffset: Dp = 34.dp,
    lineColor: Color,
    marginColor: Color,
    showMargin: Boolean = true
): Modifier = this.drawBehind {
    val spacingPx = lineSpacing.toPx()
    val marginPx = marginOffset.toPx()
    var currentY = spacingPx

    // Draw horizontal notebook rules
    while (currentY < size.height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, currentY),
            end = Offset(size.width, currentY),
            strokeWidth = 1f
        )
        currentY += spacingPx
    }

    // Draw vertical margin line
    if (showMargin && marginPx < size.width) {
        drawLine(
            color = marginColor,
            start = Offset(marginPx, 0f),
            end = Offset(marginPx, size.height),
            strokeWidth = 1.2f
        )
    }
}

/**
 * Skeuomorphic notepad card container with layered tactile elevation and subtle borders.
 */
@Composable
fun NotepadSheetCard(
    modifier: Modifier = Modifier,
    hasSpiral: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f
    val surfaceColor = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFFAFAF7) // Natural ivory notebook paper
    }

    val topBorderHighlight = if (isDark) Color(0xFF383838) else Color(0xFFFFFFFF)
    val bottomBorderShadow = if (isDark) Color(0xFF121212) else Color(0xFFD6D4CD)

    val borderBrush = Brush.verticalGradient(
        listOf(topBorderHighlight, bottomBorderShadow)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 4.dp else 3.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .border(1.dp, borderBrush, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (hasSpiral) {
                NotebookSpiralHeader(
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                    ringCount = 12
                )
                PerforatedTearEdge()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (hasSpiral) 14.dp else 16.dp),
                content = content
            )
        }
    }
}

/**
 * Skeuomorphic Sticky Note / Post-It card for pending tasks and activities.
 */
@Composable
fun StickyNoteItem(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    onAccentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.red < 0.5f

    val noteBg = if (isDark) {
        // Dark amber/charcoal warm note
        Color(0xFF28231A)
    } else {
        // Classic light pastel post-it yellow
        Color(0xFFFFF9DB)
    }

    val tapeColor = if (isDark) {
        Color(0x33FFFFFF)
    } else {
        Color(0x55FFFFFF)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .background(noteBg, RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isDark) Color(0xFF3F3628) else Color(0xFFEDE4BD),
                RoundedCornerShape(12.dp)
            )
    ) {
        // Translucent tape strip at the top center
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(48.dp)
                .height(10.dp)
                .background(tapeColor, RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
        )

        Box(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

/**
 * Notebook section divider tab styling.
 */
@Composable
fun NotebookTabHeader(
    title: String,
    badgeText: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Notebook bookmark / marker pill
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
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
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
