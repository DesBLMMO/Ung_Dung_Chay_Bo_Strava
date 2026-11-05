package com.example.ung_dung_chay_bo.presentation.screens.save_activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.core.utils.Constants
import com.example.ung_dung_chay_bo.domain.model.SportType
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveActivityScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: SaveActivityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mapLibreInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            Toast.makeText(context, "Đã lưu hoạt động thành công!", Toast.LENGTH_SHORT).show()
            onSaveSuccess()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    viewModel.setUserBackgroundImage(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val ensureSnapshotAndRun: ((() -> Unit) -> Unit) = { action ->
        if (uiState.mapSnapshot == null && uiState.userBackgroundImage == null && mapLibreInstance != null) {
            mapLibreInstance?.snapshot { bitmap ->
                if (bitmap != null) {
                    viewModel.onMapSnapshotReady(bitmap)
                }
                action()
            }
        } else {
            action()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lưu hoạt động",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF111827)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF1F2937)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveActivityRecord() }) {
                        Text(
                            "Lưu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SportOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ảnh lộ trình & thành tích của bạn",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .shadow(10.dp, RoundedCornerShape(22.dp)),
                color = Color.Black,
                shape = RoundedCornerShape(22.dp)
            ) {
                if (uiState.userBackgroundImage != null && uiState.showcaseBitmap != null) {
                    Image(
                        bitmap = uiState.showcaseBitmap!!.asImageBitmap(),
                        contentDescription = "Ảnh lộ trình và thông số",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    RealMapShowcaseCard(
                        routePoints = uiState.routePoints,
                        title = uiState.title,
                        sportType = uiState.sportType,
                        distanceKm = uiState.distanceKm,
                        durationFormatted = uiState.durationFormatted,
                        paceOrSpeed = uiState.paceOrSpeed,
                        calories = uiState.calories,
                        onMapReady = { map ->
                            mapLibreInstance = map
                        },
                        onSnapshotCaptured = { bitmap ->
                            viewModel.onMapSnapshotReady(bitmap)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        ensureSnapshotAndRun {
                            val uri = viewModel.savePhotoToGallery()
                            if (uri != null) {
                                Toast.makeText(context, "📸 Đã lưu ảnh lộ trình bản đồ vào Bộ sưu tập!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Đang xử lý ảnh, vui lòng thử lại!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SportOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SportOrange),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(19.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lưu ảnh máy", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        ensureSnapshotAndRun {
                            val shareUri = viewModel.getShareUri()
                            if (shareUri != null) {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    putExtra(Intent.EXTRA_TEXT, "Tôi vừa hoàn thành buổi chạy ${"%.2f".format(uiState.distanceKm)} km trên Strava! 🏃🔥")
                                    type = "image/jpeg"
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ ảnh lộ trình"))
                            } else {
                                Toast.makeText(context, "Đang xử lý ảnh chia sẻ...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(19.dp), tint = PureWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chia sẻ ảnh", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (uiState.userBackgroundImage != null) {
                            viewModel.setUserBackgroundImage(null)
                        } else {
                            imagePickerLauncher.launch("image/*")
                        }
                    },
                color = PureWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        if (uiState.userBackgroundImage != null) Icons.Outlined.Delete else Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = if (uiState.userBackgroundImage != null) Color(0xFFDC2626) else Color(0xFF4B5563),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.userBackgroundImage != null) "Dùng lại bản đồ lộ trình thực tế" else "Thêm ảnh chụp thực tế vào nền",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (uiState.userBackgroundImage != null) Color(0xFFDC2626) else Color(0xFF1F2937)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PureWhite,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Thông tin chi tiết",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Tiêu đề buổi chạy") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SportOrange,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Bạn cảm thấy thế nào hôm nay?") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SportOrange,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )

                    Text(
                        text = "Môn thể thao",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4B5563)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            Triple(SportType.RUN, Icons.AutoMirrored.Filled.DirectionsRun, "Chạy bộ"),
                            Triple(SportType.RIDE, Icons.AutoMirrored.Filled.DirectionsBike, "Đạp xe"),
                            Triple(SportType.WALK, Icons.AutoMirrored.Filled.DirectionsWalk, "Đi bộ")
                        ).forEach { (sport, icon, name) ->
                            val isSelected = uiState.sportType == sport
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.updateSportType(sport) },
                                color = if (isSelected) Color(0xFFFFECE5) else Color(0xFFF9FAFB),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SportOrange else Color(0xFFE5E7EB)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (isSelected) SportOrange else Color(0xFF6B7280),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) SportOrange else Color(0xFF1F2937)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Cự ly", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text("%.2f km".format(uiState.distanceKm), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Thời gian", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text(uiState.durationFormatted, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pace/Tốc độ", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text(uiState.paceOrSpeed, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Calo", fontSize = 11.sp, color = Color(0xFF6B7280))
                            Text("${uiState.calories}", fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveActivityRecord() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange)
            ) {
                Text(
                    text = "LƯU VÀ HOÀN TẤT",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun RealMapShowcaseCard(
    routePoints: List<Pair<Double, Double>>,
    title: String,
    sportType: SportType,
    distanceKm: Double,
    durationFormatted: String,
    paceOrSpeed: String,
    calories: Int,
    onMapReady: (MapLibreMap) -> Unit,
    onSnapshotCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

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

    val updateMapRoute = { map: MapLibreMap ->
        currentPolyline?.let {
            map.removePolyline(it)
            currentPolyline = null
        }

        if (routePoints.size >= 2) {
            val latLngs = routePoints.map { LatLng(it.first, it.second) }
            val polylineOptions = PolylineOptions()
                .addAll(latLngs)
                .color(AndroidColor.parseColor("#FC4C02"))
                .width(7.5f)

            currentPolyline = map.addPolyline(polylineOptions)

            val builder = LatLngBounds.Builder()
            latLngs.forEach { builder.include(it) }
            val bounds = builder.build()

            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 70), 700)
            } catch (e: Exception) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs.first(), 15.5), 700)
            }
        }
    }

    LaunchedEffect(routePoints, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        updateMapRoute(map)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                mapView.apply {
                    getMapAsync { map ->
                        mapLibreMap = map
                        onMapReady(map)

                        map.uiSettings.isCompassEnabled = false
                        map.uiSettings.isRotateGesturesEnabled = false
                        map.uiSettings.isTiltGesturesEnabled = false
                        map.uiSettings.isAttributionEnabled = false
                        map.uiSettings.isLogoEnabled = false

                        map.setStyle(Style.Builder().fromUri(Constants.MAPTILER_STYLE_OUTDOOR)) {
                            updateMapRoute(map)

                            map.addOnCameraIdleListener {
                                map.snapshot { snapshotBitmap ->
                                    if (snapshotBitmap != null) {
                                        onSnapshotCaptured(snapshotBitmap)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x99000000), Color.Transparent)
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "STRAVA",
                    color = SportOrange,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = " | RUNNER",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            val dateStr = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(Date())
            Text(
                text = dateStr,
                color = Color(0xFFF3F4F6),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .align(Alignment.BottomCenter)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            color = PureWhite,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF111827),
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = Color(0xFFFFECE5),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = sportType.title,
                            color = SportOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = Color(0xFFF3F4F6),
                    thickness = 1.dp
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "%.2f".format(distanceKm),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF111827),
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "km",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SportOrange,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("THỜI GIAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                        Text(durationFormatted, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF1F2937))
                    }
                    Column {
                        Text("PACE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                        Text(paceOrSpeed, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF1F2937))
                    }
                    Column {
                        Text("CALO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                        Text("$calories kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF1F2937))
                    }
                }
            }
        }
    }
}
