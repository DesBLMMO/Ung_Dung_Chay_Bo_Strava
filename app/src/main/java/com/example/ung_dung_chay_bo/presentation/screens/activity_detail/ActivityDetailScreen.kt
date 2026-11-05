package com.example.ung_dung_chay_bo.presentation.screens.activity_detail

import android.content.Intent
import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ung_dung_chay_bo.core.theme.*
import com.example.ung_dung_chay_bo.core.utils.Constants
import com.example.ung_dung_chay_bo.core.utils.GpsUtils
import com.example.ung_dung_chay_bo.domain.model.SportType
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ActivityDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chi tiết hoạt động",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareUri = viewModel.getShareUri()
                            if (shareUri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    putExtra(Intent.EXTRA_TEXT, "🏃 Tôi vừa hoàn thành buổi tập cùng Strava Runner!")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Chia sẻ thành tích"))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Chia sẻ",
                            tint = SportOrange
                        )
                    }

                    IconButton(
                        onClick = {
                            val gpxUri = viewModel.getGpxExportUri()
                            if (gpxUri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/gpx+xml"
                                    putExtra(Intent.EXTRA_STREAM, gpxUri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Lộ trình GPX: ${uiState.record?.title}")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Xuất file GPX"))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Xuất file GPX",
                            tint = Color(0xFF4B5563)
                        )
                    }

                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Xóa hoạt động",
                            tint = ErrorRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        },
        containerColor = LightBackground
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SportOrange)
            }
        } else if (uiState.record == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "Không tìm thấy dữ liệu",
                    color = TextMuted,
                    fontSize = 15.sp
                )
            }
        } else {
            val record = uiState.record!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Tiêu đề và Thể loại
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(SportOrangeSoft, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (uiState.sportType) {
                                            SportType.RIDE -> Icons.AutoMirrored.Filled.DirectionsBike
                                            SportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
                                            else -> Icons.AutoMirrored.Filled.DirectionsRun
                                        },
                                        contentDescription = null,
                                        tint = SportOrange,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${uiState.formattedDate} · ${uiState.formattedTime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            if (record.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = record.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // 2. Bản đồ lộ trình trực quan
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (uiState.routePoints.size >= 2) {
                                ActivityMapDetailView(
                                    points = uiState.routePoints,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                ActivityFallbackCanvas(
                                    points = uiState.routePoints,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // 3. Lưới chỉ số chính (Key Metrics)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "THỐNG KÊ HOẠT ĐỘNG",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                MetricBox(
                                    label = "Quãng đường",
                                    value = "%.2f".format(record.distanceKm),
                                    unit = "km",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    label = "Thời gian",
                                    value = GpsUtils.formatDuration(record.durationSeconds),
                                    unit = "",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    label = "Pace TB",
                                    value = record.avgPace.replace(" /km", ""),
                                    unit = "/km",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = LightBorder, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                MetricBox(
                                    label = "Tốc độ TB",
                                    value = "%.1f".format(record.avgSpeedKmh),
                                    unit = "km/h",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    label = "Tốc độ tối đa",
                                    value = "%.1f".format(uiState.maxSpeedKmh),
                                    unit = "km/h",
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBox(
                                    label = "Lượng Calo",
                                    value = "${record.calories}",
                                    unit = "kcal",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 4. Phân tích chi tiết từng Km (Km Splits)
                if (uiState.splits.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PHÂN TÍCH TỪNG KM (SPLITS)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Nhanh nhất: ${uiState.fastestPace}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                val maxPaceSec = uiState.splits.maxOfOrNull { it.paceSeconds }?.coerceAtLeast(1L) ?: 400L
                                val minPaceSec = uiState.splits.minOfOrNull { it.paceSeconds }?.coerceAtLeast(1L) ?: 200L

                                uiState.splits.forEach { split ->
                                    KmSplitRow(
                                        split = split,
                                        isFastest = split.paceSeconds == minPaceSec,
                                        maxPaceSec = maxPaceSec
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }

                // 5. Nút chia sẻ thành tích lớn
                item {
                    Button(
                        onClick = {
                            val shareUri = viewModel.getShareUri()
                            if (shareUri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    putExtra(Intent.EXTRA_TEXT, "🏃 Xem buổi tập ${record.title} của tôi trên Strava Runner!")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Chia sẻ thành tích"))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SportOrange,
                            contentColor = PureWhite
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tạo poster chia sẻ mạng xã hội",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "Xóa hoạt động này?", fontWeight = FontWeight.Bold) },
            text = { Text("Dữ liệu buổi tập và lộ trình đã lưu sẽ bị xóa vĩnh viễn. Bạn có chắc chắn muốn xóa?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteActivity {
                            onBackClick()
                        }
                    }
                ) {
                    Text(text = "Xóa", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = "Hủy", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            if (unit.isNotBlank()) {
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun KmSplitRow(
    split: KmSplit,
    isFastest: Boolean,
    maxPaceSec: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cột số Km
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(
                    if (isFastest) SuccessGreen.copy(alpha = 0.15f) else LightBackground,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${split.kmNumber}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFastest) SuccessGreen else TextPrimary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Cột Bar so sánh
        val fraction = (split.paceSeconds.toFloat() / maxPaceSec.toFloat()).coerceIn(0.2f, 1.0f)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .background(LightBackground, RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        if (isFastest) SuccessGreen else SportOrange
                    )
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Cột Pace
        Text(
            text = split.paceFormatted,
            fontSize = 13.sp,
            fontWeight = if (isFastest) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isFastest) SuccessGreen else TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
private fun ActivityMapDetailView(
    points: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    mapLibreMap = map
                    map.setStyle(
                        Style.Builder().fromUri(Constants.MAPTILER_STYLE_OUTDOOR)
                    ) { style ->
                        val latLngList = points.map { LatLng(it.first, it.second) }
                        if (latLngList.size >= 2) {
                            map.addPolyline(
                                PolylineOptions()
                                    .addAll(latLngList)
                                    .color(AndroidColor.parseColor("#FC4C02"))
                                    .width(6f)
                            )

                            // Start marker
                            map.addMarker(
                                MarkerOptions()
                                    .position(latLngList.first())
                                    .title("Xuất phát")
                            )

                            // Finish marker
                            map.addMarker(
                                MarkerOptions()
                                    .position(latLngList.last())
                                    .title("Đích đến")
                            )

                            val boundsBuilder = LatLngBounds.Builder()
                            latLngList.forEach { boundsBuilder.include(it) }
                            runCatching {
                                val bounds = boundsBuilder.build()
                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60))
                            }.onFailure {
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        latLngList.first(),
                                        14.0
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ActivityFallbackCanvas(
    points: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1E293B)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val w = size.width
            val h = size.height

            if (points.size >= 2) {
                val minLat = points.minOf { it.first }
                val maxLat = points.maxOf { it.first }
                val minLng = points.minOf { it.second }
                val maxLng = points.maxOf { it.second }

                val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
                val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)

                val path = Path()
                points.forEachIndexed { index, pt ->
                    val x = ((pt.second - minLng) / lngSpan * w).toFloat()
                    val y = (h - ((pt.first - minLat) / latSpan * h)).toFloat()
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = SportOrange,
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                // Start point
                val startX = ((points.first().second - minLng) / lngSpan * w).toFloat()
                val startY = (h - ((points.first().first - minLat) / latSpan * h)).toFloat()
                drawCircle(color = SuccessGreen, radius = 12f, center = Offset(startX, startY))

                // End point
                val endX = ((points.last().second - minLng) / lngSpan * w).toFloat()
                val endY = (h - ((points.last().first - minLat) / latSpan * h)).toFloat()
                drawCircle(color = Color(0xFFEF4444), radius = 12f, center = Offset(endX, endY))
            } else {
                drawCircle(color = SportOrange, radius = 14f, center = Offset(w / 2, h / 2))
            }
        }
    }
}
