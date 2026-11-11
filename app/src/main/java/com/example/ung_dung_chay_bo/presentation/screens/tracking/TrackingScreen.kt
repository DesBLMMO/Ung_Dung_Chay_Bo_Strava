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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
    val initialLocation by viewModel.initialLocation.collectAsStateWithLifecycle()

    var showSportSheet by remember { mutableStateOf(false) }
    var showRouteSheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showTrendMessage by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }

    var mapLibreMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        )
    }

    var isStartingTrackingRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fineGranted || coarseGranted
        if (hasLocationPermission) {
            // Tự động truy cập lấy vị trí GPS ngay khi người dùng ấn Đồng ý
            viewModel.fetchInitialLocation()

            if (isStartingTrackingRequested) {
                isStartingTrackingRequested = false
                viewModel.startTracking()
            }
        }
    }

    // Tự động yêu cầu quyền vị trí ngay khi vào màn hình nếu chưa được cấp
    LaunchedEffect(Unit) {
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
            viewModel.fetchInitialLocation()
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
        TrackingSettingsDialog(
            isAutoPauseEnabled = state.isAutoPauseEnabled,
            isAudioCuesEnabled = state.isAudioCuesEnabled,
            onToggleAutoPause = { viewModel.toggleAutoPause() },
            onToggleAudioCues = { viewModel.toggleAudioCues() },
            onDismiss = { showSettingsDialog = false }
        )
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
            initialLocation = initialLocation,
            layerStyle = layerStyle,
            is3D = is3D,
            onMapReady = { mapLibreMapInstance = it },
            modifier = Modifier.fillMaxSize()
        )

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StravaIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
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
                icon = Icons.Default.Tune,
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

        AnimatedVisibility(
            visible = state.isAutoPaused,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 96.dp)
        ) {
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PauseCircleFilled,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⏸ Tự động tạm dừng (đang đứng yên)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Right floating map action controls
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp)
                .offset(y = (-10).dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group 1: Map Layers & 3D Capsule
            MapControlsCapsule(
                is3D = is3D,
                onCycleLayer = { viewModel.cycleLayerStyle() },
                onToggle3D = { viewModel.toggle3D() }
            )

            // Group 2: Audio cues button
            StravaIconButton(
                icon = if (state.isAudioCuesEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                contentDescription = "Giọng nói hướng dẫn",
                isActive = state.isAudioCuesEnabled,
                onClick = { viewModel.toggleAudioCues() }
            )

            // Group 3: Auto-pause toggle button
            StravaIconButton(
                icon = if (state.isAutoPauseEnabled) Icons.Default.PauseCircleFilled else Icons.Default.PauseCircleOutline,
                contentDescription = "Tự động tạm dừng",
                isActive = state.isAutoPauseEnabled,
                onClick = { viewModel.toggleAutoPause() }
            )

            // Group 4: My Location button
            StravaIconButton(
                icon = Icons.Default.MyLocation,
                contentDescription = "Vị trí của tôi",
                size = 44.dp,
                iconSize = 22.dp,
                tint = Color(0xFF2563EB),
                onClick = {
                    val map = mapLibreMapInstance ?: return@StravaIconButton
                    val currentPos = state.currentLocation
                        ?: initialLocation
                        ?: Pair(21.0025, 105.8185)
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
                        isStartingTrackingRequested = true
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
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 42.dp,
    iconSize: androidx.compose.ui.unit.Dp = 21.dp,
    tint: Color = if (isActive) SportOrange else Color(0xFF334155),
    backgroundColor: Color = if (isActive) Color(0xFFFFF2EB) else PureWhite,
    borderColor: Color = if (isActive) Color(0xFFFFD4C0) else Color(0xFFE2E8F0)
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 5.dp,
                shape = CircleShape,
                ambientColor = Color(0x15000000),
                spotColor = Color(0x20000000)
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .border(0.8.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = SportOrange.copy(alpha = 0.2f))
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun TrendPillBadge(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x15000000),
                spotColor = Color(0x18000000)
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = SportOrange.copy(alpha = 0.15f))
            ) { onClick() },
        color = PureWhite,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD4C0))
    ) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF7A45), Color(0xFFFC4C02))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Whatshot,
                    contentDescription = null,
                    tint = PureWhite,
                    modifier = Modifier.size(15.dp)
                )
            }

            Text(
                text = "Bản đồ xu hướng",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun MapControlsCapsule(
    is3D: Boolean,
    onCycleLayer: () -> Unit,
    onToggle3D: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(22.dp),
            ambientColor = Color(0x18000000),
            spotColor = Color(0x20000000)
        ),
        shape = RoundedCornerShape(22.dp),
        color = PureWhite,
        border = BorderStroke(0.8.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(44.dp)
        ) {
            // Nút chuyển đổi kiểu lớp bản đồ
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = SportOrange.copy(alpha = 0.2f))
                    ) { onCycleLayer() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Đổi kiểu bản đồ",
                    tint = Color(0xFF1E293B),
                    modifier = Modifier.size(21.dp)
                )

                // Dấu chấm cam tinh tế báo hiệu chế độ layer
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(SportOrange)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 7.dp),
                thickness = 0.8.dp,
                color = Color(0xFFF1F5F9)
            )

            // Nút bật/tắt 3D/2D
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                    .background(if (is3D) Color(0xFFFFF2EB) else PureWhite)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = SportOrange.copy(alpha = 0.2f))
                    ) { onToggle3D() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (is3D) "3D" else "2D",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = if (is3D) SportOrange else Color(0xFF475569)
                )
            }
        }
    }
}

@Composable
private fun TrackingMapSection(
    state: TrackingState,
    selectedRoute: SuggestedRoute?,
    initialLocation: Pair<Double, Double>?,
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

    // Theo dõi xem đã di chuyển camera đến vị trí GPS thực chưa
    var hasCenteredOnRealLocation by remember { mutableStateOf(false) }

    val defaultLatLng = LatLng(21.0025, 105.8185)
    val currentLatLng = state.currentLocation?.let { LatLng(it.first, it.second) }
    val initialLatLng = initialLocation?.let { LatLng(it.first, it.second) }

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

    // Khi nhận được initialLocation từ GPS (trước khi nhấn Bắt đầu),
    // tự động di chuyển camera đến vị trí thực của người dùng
    LaunchedEffect(initialLatLng, mapLibreMap) {
        val map = mapLibreMap ?: return@LaunchedEffect
        if (initialLatLng != null && currentLatLng == null) {
            // Cập nhật marker vị trí người dùng
            userLocationMarker?.let { map.removeMarker(it) }
            userLocationMarker = map.addMarker(
                MarkerOptions()
                    .position(initialLatLng)
                    .title("Vị trí của bạn")
                    .snippet("GPS đang hoạt động")
            )

            if (!hasCenteredOnRealLocation) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(initialLatLng, 16.0),
                    800
                )
                hasCenteredOnRealLocation = true
            }
        }
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

            // Di chuyển camera lần đầu khi nhận GPS từ TrackingService
            if (!hasCenteredOnRealLocation) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(pos, 16.0),
                    800
                )
                hasCenteredOnRealLocation = true
            } else if (state.isTracking) {
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
                        // Ưu tiên: currentLocation > initialLocation > default
                        val bestTarget = currentLatLng
                            ?: initialLatLng
                            ?: defaultLatLng

                        if (currentLatLng != null || initialLatLng != null) {
                            hasCenteredOnRealLocation = true
                        }

                        val initialPosition = CameraPosition.Builder()
                            .target(bestTarget)
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
        SportType.RIDE -> "Tốc độ TB (km/h)"
        SportType.RUN -> "Pace / Tốc độ (km/h)"
        SportType.WALK -> "Tốc độ đi bộ (km/h)"
        SportType.TRAIL_RUN -> "Tốc độ địa hình (km/h)"
    }

    val sportIcon = when (sportType) {
        SportType.RIDE -> Icons.AutoMirrored.Filled.DirectionsBike
        SportType.RUN -> Icons.AutoMirrored.Filled.DirectionsRun
        SportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
        SportType.TRAIL_RUN -> Icons.Default.Terrain
    }

    Surface(
        modifier = modifier.shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(24.dp),
            ambientColor = Color(0x18000000),
            spotColor = Color(0x22000000)
        ),
        color = PureWhite,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(0.8.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar inside card: Sport badge + Expand icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color(0xFFFFF7F2),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, Color(0xFFFFD4C0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = sportIcon,
                            contentDescription = null,
                            tint = SportOrange,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = sportType.title,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

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
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3 metrics: Duration, Speed/Pace, Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Thời gian
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = GpsUtils.formatDuration(state.elapsedSeconds),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (state.isAutoPaused || state.isPaused) PauseYellow else Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Thời gian",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Tốc độ chính giữa (To, đậm, nổi bật)
                Column(
                    modifier = Modifier.weight(1.3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (speedKmH > 0.1f) "%.1f".format(speedKmH).replace(".", ",") else "0,0",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A),
                        lineHeight = 44.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = speedLabel,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                // Quãng đường
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "%.2f".format(state.distanceKm).replace(".", ","),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (state.isAutoPaused || state.isPaused) PauseYellow else Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Quãng đường (km)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
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
                                elevation = 5.dp,
                                shape = CircleShape,
                                ambientColor = Color(0x15000000),
                                spotColor = Color(0x20000000)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFFF7F2), Color(0xFFFFECE2))
                                )
                            )
                            .border(1.2.dp, Color(0xFFFFD5C0), CircleShape),
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
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = selectedSport.title,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
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
                            // Vòng ngoài phát quang + Nút hero chính
                            Box(
                                modifier = Modifier
                                    .size(82.dp)
                                    .clip(CircleShape)
                                    .background(SportOrange.copy(alpha = 0.15f))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
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
                                                    Color(0xFFFF6F33),
                                                    Color(0xFFFC4C02),
                                                    Color(0xFFD63600)
                                                )
                                            )
                                        )
                                        .border(1.5.dp, Color(0x44FFFFFF), CircleShape),
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
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Bắt đầu",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
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
                                elevation = 5.dp,
                                shape = CircleShape,
                                ambientColor = Color(0x15000000),
                                spotColor = Color(0x20000000)
                            )
                            .clip(CircleShape)
                            .background(if (hasSelectedRoute) Color(0xFFEFF6FF) else PureWhite)
                            .border(
                                1.2.dp,
                                if (hasSelectedRoute) Color(0xFF3B82F6) else Color(0xFFE2E8F0),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.AltRoute,
                            contentDescription = "Lộ trình",
                            tint = if (hasSelectedRoute) Color(0xFF2563EB) else Color(0xFF334155),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = if (hasSelectedRoute) "Đã có lộ trình" else "Thêm Lộ trình",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasSelectedRoute) Color(0xFF2563EB) else Color(0xFF1E293B)
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
                .size(64.dp)
                .shadow(6.dp, CircleShape, ambientColor = ErrorRed.copy(alpha = 0.25f), spotColor = ErrorRed.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(Color(0xFFFEF2F2))
                .border(
                    1.5.dp,
                    if (isPressed) ErrorRed else Color(0xFFFECACA),
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
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Kết thúc",
                    tint = ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isPressed) "..." else "GIỮ",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ErrorRed,
                    letterSpacing = 0.8.sp
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
                                text = if (route.runnersCount > 0) {
                                    "${route.areaName} • ${route.distanceKm} km • ${route.runnersCountFormatted}"
                                } else {
                                    "${route.areaName} • ${route.distanceKm} km • ${route.durationFormatted}"
                                },
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
private fun TrackingSettingsDialog(
    isAutoPauseEnabled: Boolean,
    isAudioCuesEnabled: Boolean,
    onToggleAutoPause: () -> Unit,
    onToggleAudioCues: () -> Unit,
    onDismiss: () -> Unit
) {
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
                        Text("Tự động tạm dừng (Auto-Pause)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
                        Text("Tạm dừng khi bạn dừng đèn đỏ hoặc nghỉ (< 2.2 km/h)", fontSize = 11.5.sp, color = Color(0xFF6B7280))
                    }
                    Switch(
                        checked = isAutoPauseEnabled,
                        onCheckedChange = { onToggleAutoPause() },
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
                        Text("Đọc tốc độ pace, thời gian chặng và khoảng cách", fontSize = 11.5.sp, color = Color(0xFF6B7280))
                    }
                    Switch(
                        checked = isAudioCuesEnabled,
                        onCheckedChange = { onToggleAudioCues() },
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
