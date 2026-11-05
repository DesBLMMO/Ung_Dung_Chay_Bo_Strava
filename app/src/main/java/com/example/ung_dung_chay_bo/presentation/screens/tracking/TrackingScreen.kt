package com.example.ung_dung_chay_bo.presentation.screens.tracking

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ung_dung_chay_bo.core.theme.ErrorRed
import com.example.ung_dung_chay_bo.core.theme.PauseYellow
import com.example.ung_dung_chay_bo.core.theme.PureWhite
import com.example.ung_dung_chay_bo.core.theme.SportOrange
import com.example.ung_dung_chay_bo.core.utils.Constants
import com.example.ung_dung_chay_bo.core.utils.GpsUtils
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.SuggestedRoute
import com.example.ung_dung_chay_bo.presentation.screens.maps.components.MapTilerLayerStyle
import com.example.ung_dung_chay_bo.service.TrackingState
import kotlinx.coroutines.delay
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    onBackClick: () -> Unit,
    onFinishSession: (sessionId: String) -> Unit = {},
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.trackingState.collectAsStateWithLifecycle()
    val selectedSport by viewModel.selectedSport.collectAsStateWithLifecycle()
    val selectedRoute by viewModel.selectedRoute.collectAsStateWithLifecycle()
    val availableRoutes by viewModel.availableRoutes.collectAsStateWithLifecycle()
    val layerStyle by viewModel.layerStyle.collectAsStateWithLifecycle()
    val is3D by viewModel.is3D.collectAsStateWithLifecycle()

    var showSportSheet by remember { mutableStateOf(false) }
    var showRouteSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTrendMessage by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (hasLocationPermission) {
            viewModel.startTracking()
        }
    }

    if (showFinishDialog) {
        FinishConfirmDialog(
            state = state,
            onConfirm = {
                showFinishDialog = false
                viewModel.stopTracking()
                onFinishSession(state.sessionId)
            },
            onDismiss = { showFinishDialog = false }
        )
    }

    if (showSportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSportSheet = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                )
            }
        ) {
            SportSelectionSheetContent(
                selectedSport = selectedSport,
                onSelectSport = { sport ->
                    viewModel.selectSport(sport)
                    showSportSheet = false
                }
            )
        }
    }

    if (showRouteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRouteSheet = false },
            containerColor = PureWhite,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                )
            }
        ) {
            RouteSelectionSheetContent(
                routes = availableRoutes,
                selectedRoute = selectedRoute,
                onSelectRoute = { route ->
                    viewModel.selectRoute(route)
                    showRouteSheet = false
                },
                onClearRoute = {
                    viewModel.selectRoute(null)
                    showRouteSheet = false
                }
            )
        }
    }

    if (showSettingsDialog) {
        TrackingSettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    LaunchedEffect(showTrendMessage) {
        if (showTrendMessage) {
            delay(2500)
            showTrendMessage = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TrackingMapSection(
            state = state,
            selectedRoute = selectedRoute,
            layerStyle = layerStyle,
            is3D = is3D,
            onMapReady = { mapLibreMapInstance = it },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StravaIconButton(
                icon = Icons.Default.KeyboardArrowDown,
                contentDescription = "Quay lại",
                onClick = {
                    if (state.isTracking || state.isPaused) {
                        showFinishDialog = true
                    } else {
                        onBackClick()
                    }
                }
            )

            TrendPillBadge(onClick = { showTrendMessage = true })

            StravaIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Cài đặt",
                onClick = { showSettingsDialog = true }
            )
        }

        AnimatedVisibility(
            visible = showTrendMessage,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
        ) {
            Surface(
                color = Color(0xFF1E232A),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 10.dp,
                modifier = Modifier.padding(horizontal = 28.dp)
            ) {
                Text(
                    text = "🔥 Bản đồ nhiệt hàng tuần: Phân tích các cung đường được cộng đồng chạy nhiều nhất!",
                    color = PureWhite,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .offset(y = (-45).dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapLayerButton(onClick = { viewModel.cycleLayerStyle() })

            Map3DButton(is3D = is3D, onClick = { viewModel.toggle3D() })

            StravaIconButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Vị trí của tôi",
                onClick = {
                    val map = mapLibreMapInstance ?: return@StravaIconButton
                    val currentPos = state.currentLocation ?: Pair(21.0025, 105.8185)
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(currentPos.first, currentPos.second),
                            16.5
                        ),
                        600
                    )
                }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            FloatingMetricsCard(
                state = state,
                sportType = selectedSport,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            BottomControlPanel(
                state = state,
                selectedSport = selectedSport,
                hasSelectedRoute = selectedRoute != null,
                onSportClick = { showSportSheet = true },
                onRouteChange = { showRouteSheet = true },
                onStart = {
                    if (!hasLocationPermission) {
                        val perms = buildList {
                            add(Manifest.permission.ACCESS_FINE_LOCATION)
                            add(Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        permissionLauncher.launch(perms.toTypedArray())
                    } else {
                        viewModel.startTracking()
                    }
                },
                onPause = { viewModel.pauseTracking() },
                onResume = { viewModel.resumeTracking() },
                onFinish = { showFinishDialog = true }
            )
        }
    }
}

@Composable
private fun StravaIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = Color(0x33000000),
                spotColor = Color(0x33000000)
            )
            .clip(CircleShape)
            .background(PureWhite)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = SportOrange.copy(alpha = 0.2f))
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFF1F2937),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun TrendPillBadge(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0x22000000),
                spotColor = Color(0x22000000)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(PureWhite)
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF9A62).copy(alpha = 0.6f),
                        Color(0xFFFFE0D0).copy(alpha = 0.8f),
                        Color(0xFFFFB899).copy(alpha = 0.6f)
                    )
                ),
                RoundedCornerShape(26.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = SportOrange.copy(alpha = 0.15f))
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFF22C55E),
                                Color(0xFFEAB308),
                                Color(0xFFFC4C02),
                                Color(0xFF3B82F6),
                                Color(0xFF22C55E)
                            )
                        )
                    )
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(PureWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(13.dp)
                )
            }

            Column {
                Text(
                    text = "Xem xu hướng",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    lineHeight = 15.sp
                )
                Text(
                    text = "Nhấn để thêm Bản đồ nhiệt hàng tuần",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MapLayerButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(6.dp, CircleShape, ambientColor = Color(0x33000000), spotColor = Color(0x33000000))
            .clip(CircleShape)
            .background(PureWhite)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = SportOrange.copy(alpha = 0.2f))
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = "Lớp bản đồ",
            tint = Color(0xFF1F2937),
            modifier = Modifier.size(24.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 1.dp, y = (-1).dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF1F2937)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "4",
                color = PureWhite,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun Map3DButton(is3D: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(6.dp, CircleShape, ambientColor = Color(0x33000000), spotColor = Color(0x33000000))
            .clip(CircleShape)
            .background(if (is3D) Color(0xFFFFF0EA) else PureWhite)
            .border(
                if (is3D) 1.5.dp else 0.dp,
                if (is3D) SportOrange else Color.Transparent,
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = SportOrange.copy(alpha = 0.2f))
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (is3D) "2D" else "3D",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = if (is3D) SportOrange else Color(0xFF1F2937)
        )
    }
}

@Composable
private fun TrackingMapSection(
    state: TrackingState,
    selectedRoute: SuggestedRoute?,
    layerStyle: MapTilerLayerStyle,
    is3D: Boolean,
    onMapReady: (MapLibreMap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentTrackingPolyline by remember { mutableStateOf<Polyline?>(null) }
    var suggestedRoutePolyline by remember { mutableStateOf<Polyline?>(null) }
    var userLocationMarker by remember { mutableStateOf<Marker?>(null) }

    val defaultLatLng = LatLng(21.0025, 105.8185)
    val currentLatLng = state.currentLocation?.let { LatLng(it.first, it.second) }

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

    LaunchedEffect(layerStyle) {
        mapLibreMap?.setStyle(Style.Builder().fromUri(layerStyle.url))
    }

    LaunchedEffect(is3D) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val tilt = if (is3D) 55.0 else 0.0
        val currentCam = map.cameraPosition
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder(currentCam).tilt(tilt).build()
            ),
            500
        )
    }

    LaunchedEffect(currentLatLng, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        currentLatLng?.let { pos ->
            userLocationMarker?.let { map.removeMarker(it) }
            userLocationMarker = map.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title("Vị trí của bạn")
                    .snippet("GPS đang hoạt động")
            )

            if (state.isTracking) {
                map.animateCamera(CameraUpdateFactory.newLatLng(pos), 600)
            }
        }
    }

    LaunchedEffect(selectedRoute, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        suggestedRoutePolyline?.let {
            map.removePolyline(it)
            suggestedRoutePolyline = null
        }

        selectedRoute?.let { route ->
            if (route.routePoints.size >= 2) {
                val points = route.routePoints.map { LatLng(it.first, it.second) }
                suggestedRoutePolyline = map.addPolyline(
                    PolylineOptions()
                        .addAll(points)
                        .color(AndroidColor.parseColor("#3B82F6"))
                        .width(7f)
                )

                val startPos = LatLng(route.startPoint.first, route.startPoint.second)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(startPos, 14.5), 800)
            }
        }
    }

    LaunchedEffect(state.path, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (state.path.size >= 2) {
            currentTrackingPolyline?.let {
                map.removePolyline(it)
                currentTrackingPolyline = null
            }
            val polylinePoints = state.path.map { LatLng(it.first, it.second) }
            currentTrackingPolyline = map.addPolyline(
                PolylineOptions()
                    .addAll(polylinePoints)
                    .color(AndroidColor.parseColor("#FC4C02"))
                    .width(8f)
            )
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    mapLibreMap = map
                    onMapReady(map)

                    map.setStyle(Style.Builder().fromUri(layerStyle.url)) {
                        val initialPosition = CameraPosition.Builder()
                            .target(currentLatLng ?: defaultLatLng)
                            .zoom(16.0)
                            .tilt(if (is3D) 55.0 else 0.0)
                            .build()
                        map.cameraPosition = initialPosition

                        map.uiSettings.isRotateGesturesEnabled = true
                        map.uiSettings.isTiltGesturesEnabled = true
                        map.uiSettings.isZoomGesturesEnabled = true
                        map.uiSettings.isScrollGesturesEnabled = true
                        map.uiSettings.isCompassEnabled = false
                    }
                }
            }
        }
    )
}

@Composable
private fun FloatingMetricsCard(
    state: TrackingState,
    sportType: SportType,
    modifier: Modifier = Modifier
) {
    val speedKmH = if (state.isTracking && state.elapsedSeconds > 0) {
        (state.distanceKm / (state.elapsedSeconds / 3600.0)).toFloat().coerceAtLeast(0f)
    } else {
        (state.currentSpeedMs * 3.6f).coerceAtLeast(0f)
    }

    val speedLabel = when (sportType) {
        SportType.RIDE -> "Tốc độ trung bình (km/h)"
        SportType.RUN -> "Pace / Tốc độ (km/h)"
        SportType.WALK -> "Tốc độ đi bộ (km/h)"
        SportType.TRAIL_RUN -> "Tốc độ địa hình (km/h)"
    }

    Surface(
        modifier = modifier.shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(22.dp),
            ambientColor = Color(0x22000000),
            spotColor = Color(0x22000000)
        ),
        color = PureWhite,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.size(28.dp))

                Text(
                    text = sportType.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.Gray.copy(alpha = 0.2f))
                        ) {},
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Mở rộng",
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = GpsUtils.formatDuration(state.elapsedSeconds),
                        fontSize = 25.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Thời gian",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(
                    modifier = Modifier.weight(1.3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (speedKmH > 0.1f) "%.1f".format(speedKmH).replace(".", ",") else "0,0",
                        fontSize = 44.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111827),
                        lineHeight = 46.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = speedLabel,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "%.2f".format(state.distanceKm).replace(".", ","),
                        fontSize = 25.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Quãng đường (km)",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomControlPanel(
    state: TrackingState,
    selectedSport: SportType,
    hasSelectedRoute: Boolean,
    onSportClick: () -> Unit,
    onRouteChange: () -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PureWhite,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        shadowElevation = 18.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 10.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1D5DB))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSportClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = Color(0x18000000),
                                spotColor = Color(0x18000000)
                            )
                            .clip(CircleShape)
                            .background(Color(0xFFFFF0EB))
                            .border(1.dp, Color(0xFFFFDCD0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val sportIcon = when (selectedSport) {
                            SportType.RIDE -> Icons.AutoMirrored.Filled.DirectionsBike
                            SportType.RUN -> Icons.AutoMirrored.Filled.DirectionsRun
                            SportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
                            SportType.TRAIL_RUN -> Icons.Default.Terrain
                        }
                        Icon(
                            imageVector = sportIcon,
                            contentDescription = selectedSport.title,
                            tint = SportOrange,
                            modifier = Modifier.size(28.dp)
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-3).dp, y = 3.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(PureWhite)
                                .padding(1.5.dp)
                                .clip(CircleShape)
                                .background(SportOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = selectedSport.title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )
                }

                when {
                    !state.isTracking && !state.isPaused && state.elapsedSeconds == 0L -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onStart() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = CircleShape,
                                        ambientColor = SportOrange,
                                        spotColor = SportOrange.copy(alpha = 0.6f)
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFFFF6224),
                                                Color(0xFFFC4C02),
                                                Color(0xFFD93B00)
                                            )
                                        )
                                    )
                                    .border(2.dp, Color(0x33FC4C02), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Bắt đầu",
                                    tint = PureWhite,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .offset(x = 2.5.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(7.dp))

                            Text(
                                text = "Bắt đầu",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SportOrange
                            )
                        }
                    }

                    state.isTracking && !state.isPaused -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onPause() }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .shadow(6.dp, CircleShape, ambientColor = Color(0x22000000), spotColor = Color(0x22000000))
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFF9E6))
                                        .border(1.5.dp, Color(0xFFFFD54F), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Pause,
                                        contentDescription = "Tạm dừng",
                                        tint = Color(0xFFF57C00),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(7.dp))
                                Text(
                                    "Tạm dừng",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF57C00),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LongPressFinishButton(onFinish = onFinish)
                        }
                    }

                    state.isPaused -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onResume() }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .shadow(8.dp, CircleShape, ambientColor = SportOrange, spotColor = SportOrange.copy(alpha = 0.4f))
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFFFF6224), Color(0xFFFC4C02))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Tiếp tục",
                                        tint = PureWhite,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .offset(x = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(7.dp))
                                Text(
                                    "Tiếp tục",
                                    fontSize = 12.sp,
                                    color = SportOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LongPressFinishButton(onFinish = onFinish)
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRouteChange() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = Color(0x18000000),
                                spotColor = Color(0x18000000)
                            )
                            .clip(CircleShape)
                            .background(if (hasSelectedRoute) Color(0xFFEEF4FF) else Color(0xFFF3F4F6))
                            .border(
                                1.dp,
                                if (hasSelectedRoute) Color(0xFF3B82F6) else Color(0xFFE5E7EB),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.AltRoute,
                            contentDescription = "Lộ trình",
                            tint = if (hasSelectedRoute) Color(0xFF2563EB) else Color(0xFF374151),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = if (hasSelectedRoute) "Đã gắn lộ trình" else "Thêm Lộ trình",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasSelectedRoute) Color(0xFF2563EB) else Color(0xFF1F2937)
                    )
                }
            }
        }
    }
}

@Composable
private fun LongPressFinishButton(onFinish: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100),
        label = "finish_long_press"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            progress = 0f
            val totalMs = 1500L
            val steps = 30
            val stepMs = totalMs / steps
            for (i in 1..steps) {
                delay(stepMs)
                progress = i.toFloat() / steps
                if (i == steps) {
                    onFinish()
                    isPressed = false
                    progress = 0f
                }
            }
        } else {
            progress = 0f
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(76.dp)
    ) {
        if (animatedProgress > 0f) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(76.dp),
                color = ErrorRed,
                strokeWidth = 3.5.dp,
                trackColor = Color.Transparent
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(62.dp)
                .shadow(6.dp, CircleShape, ambientColor = ErrorRed.copy(alpha = 0.3f), spotColor = ErrorRed.copy(alpha = 0.3f))
                .clip(CircleShape)
                .background(Color(0xFFFFEBEE))
                .border(
                    1.5.dp,
                    if (isPressed) ErrorRed else ErrorRed.copy(alpha = 0.5f),
                    CircleShape
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("■", fontSize = 17.sp, color = ErrorRed)
                Text(
                    text = if (isPressed) "..." else "GIỮ",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ErrorRed,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun SportSelectionSheetContent(
    selectedSport: SportType,
    onSelectSport: (SportType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Chọn môn thể thao",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Text(
            text = "Bản đồ và số liệu sẽ tự động tối ưu tương ứng",
            fontSize = 12.5.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(18.dp))

        val sports = listOf(
            Triple(SportType.RIDE, Icons.AutoMirrored.Filled.DirectionsBike, "Đạp xe thể thao"),
            Triple(SportType.RUN, Icons.AutoMirrored.Filled.DirectionsRun, "Chạy bộ rèn luyện"),
            Triple(SportType.WALK, Icons.AutoMirrored.Filled.DirectionsWalk, "Đi bộ hàng ngày"),
            Triple(SportType.TRAIL_RUN, Icons.Default.Terrain, "Chạy địa hình leo núi")
        )

        sports.forEach { (sport, icon, desc) ->
            val isSelected = sport == selectedSport
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectSport(sport) },
                color = if (isSelected) Color(0xFFFFF0EB) else Color(0xFFF9FAFB),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) SportOrange else Color(0xFFE5E7EB)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SportOrange else Color(0xFFE5E7EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (isSelected) PureWhite else Color(0xFF4B5563),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = sport.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SportOrange else Color(0xFF111827)
                            )
                            Text(text = desc, fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SportOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RouteSelectionSheetContent(
    routes: List<SuggestedRoute>,
    selectedRoute: SuggestedRoute?,
    onSelectRoute: (SuggestedRoute) -> Unit,
    onClearRoute: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Lộ trình gợi ý gần bạn",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Chọn lộ trình để hiển thị đường dẫn trên bản đồ",
                    fontSize = 12.5.sp,
                    color = Color(0xFF6B7280)
                )
            }

            if (selectedRoute != null) {
                TextButton(onClick = onClearRoute) {
                    Text("Gỡ bỏ", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(routes) { route ->
                val isSelected = route.id == selectedRoute?.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectRoute(route) },
                    color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF9FAFB),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF3B82F6) else Color(0xFFE5E7EB)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = route.title,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${route.areaName} • ${route.distanceKm} km • ${route.durationFormatted}",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TrackingSettingsDialog(onDismiss: () -> Unit) {
    var autoPause by remember { mutableStateOf(true) }
    var audioCues by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Cài đặt ghi hoạt động",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tự động tạm dừng", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
                        Text("Tạm dừng khi bạn dừng đèn đỏ hoặc nghỉ", fontSize = 11.5.sp, color = Color(0xFF6B7280))
                    }
                    Switch(
                        checked = autoPause,
                        onCheckedChange = { autoPause = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = SportOrange)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Thông báo giọng nói mỗi km", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
                        Text("Đọc tốc độ pace và khoảng cách", fontSize = 11.5.sp, color = Color(0xFF6B7280))
                    }
                    Switch(
                        checked = audioCues,
                        onCheckedChange = { audioCues = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = SportOrange)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SportOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hoàn tất", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun FinishConfirmDialog(
    state: TrackingState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Kết thúc hoạt động?",
                color = Color(0xFF111827),
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "📍 %.2f km · ⏱ %s".format(
                        state.distanceKm,
                        GpsUtils.formatDuration(state.elapsedSeconds)
                    ),
                    color = SportOrange,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "🔥 ${state.caloriesBurned} kcal · ${state.currentPace}",
                    color = Color(0xFF4B5563),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Bạn có muốn lưu lại buổi tập này để chia sẻ lên bảng tin không?",
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu buổi tập", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tiếp tục chạy", color = SportOrange, fontWeight = FontWeight.Bold)
            }
        }
    )
}
