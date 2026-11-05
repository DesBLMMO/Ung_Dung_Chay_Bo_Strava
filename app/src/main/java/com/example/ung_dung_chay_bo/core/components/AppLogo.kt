package com.example.ung_dung_chay_bo.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.core.theme.SportOrangeDark
import com.example.ung_dung_chay_bo.core.theme.SportOrangeLight

@Composable
fun AppLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SportOrange, SportOrangeDark)
                ),
                shape = RoundedCornerShape(size * 0.28f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.62f)) {
            val width = this.size.width
            val height = this.size.height

            val path = Path().apply {
                moveTo(width * 0.25f, height * 0.82f)
                lineTo(width * 0.55f, height * 0.18f)
                lineTo(width * 0.85f, height * 0.82f)
                lineTo(width * 0.65f, height * 0.82f)
                lineTo(width * 0.55f, height * 0.50f)
                lineTo(width * 0.45f, height * 0.82f)
                close()
            }

            drawPath(
                path = path,
                color = Color.White
            )

            val accentPath = Path().apply {
                moveTo(width * 0.55f, height * 0.62f)
                lineTo(width * 0.70f, height * 0.95f)
            }

            drawPath(
                path = accentPath,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun GoogleLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val blueBar = Path().apply {
            moveTo(w * 0.5f, h * 0.38f)
            lineTo(w * 0.95f, h * 0.38f)
            lineTo(w * 0.95f, h * 0.62f)
            lineTo(w * 0.5f, h * 0.62f)
            close()
        }
        drawPath(blueBar, color = Color(0xFF4285F4))

        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 0f,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.84f),
            style = Stroke(width = w * 0.18f, cap = StrokeCap.Butt)
        )

        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.84f),
            style = Stroke(width = w * 0.18f, cap = StrokeCap.Butt)
        )

        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 145f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.84f),
            style = Stroke(width = w * 0.18f, cap = StrokeCap.Butt)
        )

        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 235f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.84f),
            style = Stroke(width = w * 0.18f, cap = StrokeCap.Butt)
        )
    }
}
