package com.example.ung_dung_chay_bo.presentation.screens.maps.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.ung_dung_chay_bo.core.theme.InfoBlue
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.domain.model.SuggestedRoute

@Composable
fun SportVectorMap(
    routes: List<SuggestedRoute>,
    selectedRoute: SuggestedRoute?,
    userLocation: Pair<Double, Double>,
    onSelectRoute: (SuggestedRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val textMeasurer = rememberTextMeasurer()

    val mapBgColor = Color(0xFFF7F6F2)
    val waterColor = Color(0xFFCBE3EE)
    val waterBorderColor = Color(0xFFB5D7E5)
    val majorRoadColor = Color(0xFFFFFFFF)
    val majorRoadBorder = Color(0xFFE4DFD5)
    val highwayColor = Color(0xFFFFECC7)
    val highwayBorder = Color(0xFFECCB92)
    val parkColor = Color(0xFFE3EDDE)

    val centerLat = 21.0150
    val centerLng = 105.8300

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.5f)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
            .pointerInput(routes) {
                detectTapGestures { tapOffset ->
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f + panOffsetX
            val centerY = height / 2f + panOffsetY

            fun gpsToCanvas(lat: Double, lng: Double): Offset {
                val scale = 3200f * zoomScale
                val x = centerX + ((lng - centerLng) * scale).toFloat()
                val y = centerY - ((lat - centerLat) * scale * 1.08f).toFloat()
                return Offset(x, y)
            }

            drawRect(color = mapBgColor)

            val bachThaoPos = gpsToCanvas(21.042, 105.828)
            drawCircle(color = parkColor, radius = 55f * zoomScale, center = bachThaoPos)

            val thongNhatPos = gpsToCanvas(21.015, 105.845)
            drawCircle(color = parkColor, radius = 65f * zoomScale, center = thongNhatPos)

            val yenSoPos = gpsToCanvas(20.975, 105.855)
            drawCircle(color = parkColor, radius = 95f * zoomScale, center = yenSoPos)

            val songHongPath = Path().apply {
                val p0 = gpsToCanvas(21.140, 105.770)
                val p1 = gpsToCanvas(21.110, 105.810)
                val p2 = gpsToCanvas(21.065, 105.865)
                val p3 = gpsToCanvas(21.025, 105.875)
                val p4 = gpsToCanvas(20.980, 105.900)
                moveTo(p0.x, p0.y)
                cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
                lineTo(p4.x, p4.y)
            }
            drawPath(
                path = songHongPath,
                color = waterBorderColor,
                style = Stroke(width = 68f * zoomScale, cap = StrokeCap.Round)
            )
            drawPath(
                path = songHongPath,
                color = waterColor,
                style = Stroke(width = 60f * zoomScale, cap = StrokeCap.Round)
            )

            val hoTayCenter = gpsToCanvas(21.058, 105.822)
            val hoTayPath = Path().apply {
                val r = 110f * zoomScale
                moveTo(hoTayCenter.x - r * 0.9f, hoTayCenter.y)
                cubicTo(
                    hoTayCenter.x - r, hoTayCenter.y - r * 0.8f,
                    hoTayCenter.x + r * 0.4f, hoTayCenter.y - r * 1.1f,
                    hoTayCenter.x + r * 0.9f, hoTayCenter.y - r * 0.2f
                )
                cubicTo(
                    hoTayCenter.x + r * 1.2f, hoTayCenter.y + r * 0.6f,
                    hoTayCenter.x - r * 0.2f, hoTayCenter.y + r * 1.0f,
                    hoTayCenter.x - r * 0.9f, hoTayCenter.y
                )
                close()
            }
            drawPath(path = hoTayPath, color = waterBorderColor, style = Stroke(width = 4f * zoomScale))
            drawPath(path = hoTayPath, color = waterColor, style = Fill)

            val hoanKiemCenter = gpsToCanvas(21.0285, 105.8542)
            drawOval(
                color = waterColor,
                topLeft = Offset(hoanKiemCenter.x - 22f * zoomScale, hoanKiemCenter.y - 45f * zoomScale),
                size = androidx.compose.ui.geometry.Size(44f * zoomScale, 90f * zoomScale)
            )

            val linhDamCenter = gpsToCanvas(20.968, 105.830)
            drawCircle(color = waterColor, radius = 50f * zoomScale, center = linhDamCenter)

            val nhatTanStart = gpsToCanvas(21.090, 105.815)
            val nhatTanEnd = gpsToCanvas(21.115, 105.825)
            drawLine(color = highwayBorder, start = nhatTanStart, end = nhatTanEnd, strokeWidth = 14f * zoomScale)
            drawLine(color = highwayColor, start = nhatTanStart, end = nhatTanEnd, strokeWidth = 10f * zoomScale)

            val ring2Path = Path().apply {
                val pt1 = gpsToCanvas(21.050, 105.805)
                val pt2 = gpsToCanvas(21.035, 105.803)
                val pt3 = gpsToCanvas(21.010, 105.815)
                val pt4 = gpsToCanvas(21.000, 105.845)
                moveTo(pt1.x, pt1.y)
                cubicTo(pt2.x, pt2.y, pt3.x, pt3.y, pt4.x, pt4.y)
            }
            drawPath(path = ring2Path, color = highwayBorder, style = Stroke(width = 12f * zoomScale, cap = StrokeCap.Round))
            drawPath(path = ring2Path, color = highwayColor, style = Stroke(width = 9f * zoomScale, cap = StrokeCap.Round))

            val ring3Path = Path().apply {
                val pt1 = gpsToCanvas(21.045, 105.780)
                val pt2 = gpsToCanvas(21.015, 105.785)
                val pt3 = gpsToCanvas(20.990, 105.805)
                val pt4 = gpsToCanvas(20.975, 105.835)
                moveTo(pt1.x, pt1.y)
                lineTo(pt2.x, pt2.y)
                lineTo(pt3.x, pt3.y)
                lineTo(pt4.x, pt4.y)
            }
            drawPath(path = ring3Path, color = majorRoadBorder, style = Stroke(width = 16f * zoomScale, cap = StrokeCap.Round))
            drawPath(path = ring3Path, color = majorRoadColor, style = Stroke(width = 12f * zoomScale, cap = StrokeCap.Round))

            val nguyenTraiStart = gpsToCanvas(21.015, 105.820)
            val nguyenTraiEnd = gpsToCanvas(20.985, 105.785)
            drawLine(color = majorRoadBorder, start = nguyenTraiStart, end = nguyenTraiEnd, strokeWidth = 12f * zoomScale)
            drawLine(color = majorRoadColor, start = nguyenTraiStart, end = nguyenTraiEnd, strokeWidth = 9f * zoomScale)

            drawMapLabel(textMeasurer, "Sông Hồng", gpsToCanvas(21.080, 105.845), Color(0xFF64748B), 12.sp, zoomScale)
            drawMapLabel(textMeasurer, "Hồ Tây", hoTayCenter, Color(0xFF0369A1), 14.sp, zoomScale)
            drawMapLabel(textMeasurer, "HÀ NỘI", gpsToCanvas(21.028, 105.835), Color(0xFF0F172A), 16.sp, zoomScale, isBold = true)
            drawMapLabel(textMeasurer, "Khương Trung", gpsToCanvas(21.002, 105.818), Color(0xFF475569), 11.sp, zoomScale)
            drawMapLabel(textMeasurer, "Gamuda", gpsToCanvas(20.970, 105.860), Color(0xFF475569), 11.sp, zoomScale)
            drawMapLabel(textMeasurer, "Vân Điển", gpsToCanvas(20.955, 105.840), Color(0xFF475569), 11.sp, zoomScale)

            selectedRoute?.let { route ->
                if (route.routePoints.size >= 2) {
                    val polyPath = Path().apply {
                        val first = gpsToCanvas(route.routePoints.first().first, route.routePoints.first().second)
                        moveTo(first.x, first.y)
                        for (i in 1 until route.routePoints.size) {
                            val pt = gpsToCanvas(route.routePoints[i].first, route.routePoints[i].second)
                            lineTo(pt.x, pt.y)
                        }
                    }

                    drawPath(
                        path = polyPath,
                        color = SportOrange.copy(alpha = 0.35f),
                        style = Stroke(width = 16f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        path = polyPath,
                        color = SportOrange,
                        style = Stroke(width = 8f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            routes.forEach { route ->
                val pos = gpsToCanvas(route.startPoint.first, route.startPoint.second)
                val isSelected = selectedRoute?.id == route.id

                drawRouteBadgeMarker(
                    textMeasurer = textMeasurer,
                    position = pos,
                    title = if (route.routesInAreaCount > 1) "${route.areaName} Routes: ${route.routesInAreaCount}" else route.areaName,
                    isSelected = isSelected,
                    zoom = zoomScale
                )
            }

            val userCanvasPos = gpsToCanvas(userLocation.first, userLocation.second)

            drawCircle(
                color = InfoBlue.copy(alpha = pulseAlpha),
                radius = pulseRadius * zoomScale,
                center = userCanvasPos
            )
            drawCircle(
                color = PureWhite,
                radius = 11f * zoomScale,
                center = userCanvasPos
            )
            drawCircle(
                color = InfoBlue,
                radius = 8f * zoomScale,
                center = userCanvasPos
            )
        }
    }
}

private fun DrawScope.drawRouteBadgeMarker(
    textMeasurer: TextMeasurer,
    position: Offset,
    title: String,
    isSelected: Boolean,
    zoom: Float
) {
    val badgeRadius = if (isSelected) 17f * zoom else 14f * zoom
    val bgColor = if (isSelected) Color(0xFFD93D00) else SportOrange

    drawCircle(
        color = Color(0x33000000),
        radius = badgeRadius + 3f,
        center = Offset(position.x + 2f, position.y + 2f)
    )

    drawCircle(
        color = bgColor,
        radius = badgeRadius,
        center = position
    )

    drawCircle(
        color = PureWhite,
        radius = badgeRadius,
        center = position,
        style = Stroke(width = 2.5f * zoom)
    )

    val iconSize = 7f * zoom
    drawCircle(
        color = PureWhite,
        radius = iconSize * 0.6f,
        center = position
    )

    if (zoom >= 0.8f) {
        val textLayout = textMeasurer.measure(
            text = title,
            style = TextStyle(
                fontSize = (10f * zoom).coerceIn(9f, 13f).sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) Color(0xFFD93D00) else Color(0xFFEA580C)
            )
        )

        val textTopLeft = Offset(position.x + badgeRadius + 4f, position.y - textLayout.size.height / 2f)

        drawRoundRect(
            color = PureWhite.copy(alpha = 0.88f),
            topLeft = Offset(textTopLeft.x - 4f, textTopLeft.y - 2f),
            size = androidx.compose.ui.geometry.Size(textLayout.size.width + 8f, textLayout.size.height + 4f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        drawText(
            textLayoutResult = textLayout,
            topLeft = textTopLeft
        )
    }
}

private fun DrawScope.drawMapLabel(
    textMeasurer: TextMeasurer,
    text: String,
    position: Offset,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    zoom: Float,
    isBold: Boolean = false
) {
    if (zoom < 0.7f && !isBold) return

    val textLayout = textMeasurer.measure(
        text = text,
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Medium,
            color = color
        )
    )

    drawText(
        textLayoutResult = textLayout,
        topLeft = Offset(position.x - textLayout.size.width / 2f, position.y - textLayout.size.height / 2f)
    )
}
