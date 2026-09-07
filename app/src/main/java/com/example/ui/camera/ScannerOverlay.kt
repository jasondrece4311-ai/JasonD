package com.example.ui.camera

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.PriceTaggedItem
import com.example.ui.components.FloatingPriceTagBadge
import com.example.ui.theme.CyanHUD
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PriceTagGreen

@Composable
fun ScannerOverlay(
    detectedItems: List<PriceTaggedItem>,
    selectedItem: PriceTaggedItem?,
    isScanning: Boolean,
    onItemClick: (PriceTaggedItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scannerAnim")

    // Laser sweep animation
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    // Pulse glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidth = constraints.maxWidth.toFloat()
        val totalHeight = constraints.maxHeight.toFloat()

        // Canvas HUD drawings: targeting brackets, laser sweep, bounding boxes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Center targeting reticle brackets
            val bracketSize = 36.dp.toPx()
            val bracketPadX = canvasW * 0.12f
            val bracketPadY = canvasH * 0.22f
            val bracketRight = canvasW - bracketPadX
            val bracketBottom = canvasH - bracketPadY
            val strokeWidth = 3.dp.toPx()
            val bracketColor = if (isScanning) EmeraldPrimary else Color.White.copy(alpha = 0.6f)

            // Top-Left bracket
            val tlPath = Path().apply {
                moveTo(bracketPadX, bracketPadY + bracketSize)
                lineTo(bracketPadX, bracketPadY)
                lineTo(bracketPadX + bracketSize, bracketPadY)
            }
            drawPath(tlPath, color = bracketColor, style = Stroke(width = strokeWidth))

            // Top-Right bracket
            val trPath = Path().apply {
                moveTo(bracketRight - bracketSize, bracketPadY)
                lineTo(bracketRight, bracketPadY)
                lineTo(bracketRight, bracketPadY + bracketSize)
            }
            drawPath(trPath, color = bracketColor, style = Stroke(width = strokeWidth))

            // Bottom-Left bracket
            val blPath = Path().apply {
                moveTo(bracketPadX, bracketBottom - bracketSize)
                lineTo(bracketPadX, bracketBottom)
                lineTo(bracketPadX + bracketSize, bracketBottom)
            }
            drawPath(blPath, color = bracketColor, style = Stroke(width = strokeWidth))

            // Bottom-Right bracket
            val brPath = Path().apply {
                moveTo(bracketRight - bracketSize, bracketBottom)
                lineTo(bracketRight, bracketBottom)
                lineTo(bracketRight, bracketBottom - bracketSize)
            }
            drawPath(brPath, color = bracketColor, style = Stroke(width = strokeWidth))

            // Center subtle crosshair
            val centerX = canvasW / 2f
            val centerY = canvasH / 2f
            val crossSize = 10.dp.toPx()
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(centerX - crossSize, centerY),
                end = Offset(centerX + crossSize, centerY),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(centerX, centerY - crossSize),
                end = Offset(centerX, centerY + crossSize),
                strokeWidth = 1.5.dp.toPx()
            )

            // Animated Laser Beam if scanning
            if (isScanning) {
                val laserY = canvasH * laserProgress
                val laserGradient = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        EmeraldPrimary.copy(alpha = glowAlpha),
                        PriceTagGreen.copy(alpha = 0.95f),
                        EmeraldPrimary.copy(alpha = glowAlpha),
                        Color.Transparent
                    )
                )

                // Wide laser aura
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            EmeraldPrimary.copy(alpha = glowAlpha * 0.25f),
                            Color.Transparent
                        ),
                        startY = laserY - 24.dp.toPx(),
                        endY = laserY + 24.dp.toPx()
                    ),
                    topLeft = Offset(0f, laserY - 24.dp.toPx()),
                    size = Size(canvasW, 48.dp.toPx())
                )

                // High-intensity center laser line
                drawLine(
                    brush = laserGradient,
                    start = Offset(bracketPadX, laserY),
                    end = Offset(bracketRight, laserY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Draw bounding boxes for detected items
            detectedItems.forEach { item ->
                val box = item.boxCoordinates
                val left = box.left * canvasW
                val top = box.top * canvasH
                val width = (box.right - box.left) * canvasW
                val height = (box.bottom - box.top) * canvasH
                val isSelected = selectedItem?.id == item.id

                val boxColor = if (isSelected) EmeraldPrimary else CyanHUD.copy(alpha = 0.85f)
                val fillAlpha = if (isSelected) 0.12f else 0.05f

                // Shaded box region
                drawRoundRect(
                    color = boxColor.copy(alpha = fillAlpha),
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )

                // Box border
                drawRoundRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = Size(width, height),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(width = if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx())
                )

                // Anchor pin dot
                drawCircle(
                    color = PriceTagGreen,
                    radius = 4.dp.toPx(),
                    center = Offset(left + width / 2f, top + height)
                )
            }
        }

        // Render Floating Interactive Price Tag Badges
        val density = androidx.compose.ui.platform.LocalDensity.current
        detectedItems.forEach { item ->
            val box = item.boxCoordinates
            val isSelected = selectedItem?.id == item.id

            val tagXPx = with(density) {
                ((box.centerX * totalWidth) - 100.dp.toPx()).coerceIn(
                    16.dp.toPx(),
                    (totalWidth - 250.dp.toPx()).coerceAtLeast(16.dp.toPx())
                ).toInt()
            }

            val tagYPx = with(density) {
                ((box.bottom * totalHeight) + 8.dp.toPx()).coerceIn(
                    totalHeight * 0.15f,
                    (totalHeight - 160.dp.toPx()).coerceAtLeast(totalHeight * 0.15f)
                ).toInt()
            }

            Box(
                modifier = Modifier.offset { IntOffset(tagXPx, tagYPx) }
            ) {
                FloatingPriceTagBadge(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}
