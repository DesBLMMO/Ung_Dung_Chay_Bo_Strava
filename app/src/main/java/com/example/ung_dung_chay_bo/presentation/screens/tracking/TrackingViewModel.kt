package com.example.ung_dung_chay_bo.presentation.screens.tracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.core.utils.GpsUtils
import com.example.ung_dung_chay_bo.core.utils.RouteCatalog
import com.example.ung_dung_chay_bo.data.local.dao.TrackPointDao
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.model.SuggestedRoute
import com.example.ung_dung_chay_bo.presentation.screens.maps.components.MapTilerLayerStyle
import com.example.ung_dung_chay_bo.service.TrackingService
import com.example.ung_dung_chay_bo.service.TrackingState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackPointDao: TrackPointDao
) : ViewModel() {

    val trackingState: StateFlow<TrackingState> = TrackingService.trackingState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TrackingService.trackingState.value
        )

    private val _selectedSport = MutableStateFlow(SportType.RUN)
    val selectedSport: StateFlow<SportType> = _selectedSport.asStateFlow()

    private val _selectedRoute = MutableStateFlow<SuggestedRoute?>(null)
    val selectedRoute: StateFlow<SuggestedRoute?> = _selectedRoute.asStateFlow()

    private val _availableRoutes = MutableStateFlow(RouteCatalog.getDefaultRoutes())
    val availableRoutes: StateFlow<List<SuggestedRoute>> = _availableRoutes.asStateFlow()

    private val _layerStyle = MutableStateFlow(MapTilerLayerStyle.OUTDOOR)
    val layerStyle: StateFlow<MapTilerLayerStyle> = _layerStyle.asStateFlow()

    private val _is3D = MutableStateFlow(false)
    val is3D: StateFlow<Boolean> = _is3D.asStateFlow()

    /** Vị trí GPS ban đầu lấy ngay khi mở màn hình, để map center đúng chỗ */
    private val _initialLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val initialLocation: StateFlow<Pair<Double, Double>?> = _initialLocation.asStateFlow()

    init {
        fetchInitialLocation()
    }

    /**
     * Lấy vị trí hiện tại ngay lập tức (getLastLocation hoặc getCurrentLocation)
     * để map có thể center đúng vị trí thực trước khi nhấn "Bắt đầu".
     */
    fun fetchInitialLocation() {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        // Thử lấy lastLocation trước (nhanh nếu hệ thống đã có cache)
        try {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _initialLocation.value = Pair(location.latitude, location.longitude)
                }
                // Dù có hay chưa có lastLocation, vẫn kích hoạt fresh location để lấy tọa độ mới nhất trên máy thật
                requestFreshLocation(fusedClient)
            }.addOnFailureListener {
                requestFreshLocation(fusedClient)
            }
        } catch (_: SecurityException) {
            // Không có quyền – bỏ qua
        }
    }

    @Suppress("MissingPermission")
    private fun requestFreshLocation(
        fusedClient: com.google.android.gms.location.FusedLocationProviderClient
    ) {
        val cts = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    _initialLocation.value = Pair(location.latitude, location.longitude)
                }
            }

        // Đăng ký 1 update duy nhất phòng trường hợp máy thật vừa bật GPS và cần thời gian fix vệ tinh
        val request = com.google.android.gms.location.LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000L
        ).setMaxUpdates(1).build()

        val callback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                result.lastLocation?.let { loc ->
                    _initialLocation.value = Pair(loc.latitude, loc.longitude)
                }
                fusedClient.removeLocationUpdates(this)
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                request,
                callback,
                android.os.Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            // ignore
        }
    }

    fun selectSport(sport: SportType) {
        _selectedSport.value = sport
    }

    fun selectRoute(route: SuggestedRoute?) {
        _selectedRoute.value = route
    }

    fun toggle3D() {
        _is3D.value = !_is3D.value
    }

    fun cycleLayerStyle() {
        _layerStyle.value = when (_layerStyle.value) {
            MapTilerLayerStyle.OUTDOOR -> MapTilerLayerStyle.STREETS
            MapTilerLayerStyle.STREETS -> MapTilerLayerStyle.SATELLITE
            MapTilerLayerStyle.SATELLITE -> MapTilerLayerStyle.OUTDOOR
        }
    }

    fun startTracking() {
        sendServiceAction(TrackingService.ACTION_START)
    }

    fun pauseTracking() {
        sendServiceAction(TrackingService.ACTION_PAUSE)
    }

    fun resumeTracking() {
        sendServiceAction(TrackingService.ACTION_RESUME)
    }

    fun stopTracking() {
        sendServiceAction(TrackingService.ACTION_STOP)
    }

    fun toggleAutoPause() {
        sendServiceAction(TrackingService.ACTION_TOGGLE_AUTO_PAUSE)
    }

    fun toggleAudioCues() {
        sendServiceAction(TrackingService.ACTION_TOGGLE_AUDIO_CUES)
    }

    fun clearSessionData() {
        val state = trackingState.value
        if (state.sessionId.isNotBlank()) {
            viewModelScope.launch {
                trackPointDao.deleteSession(state.sessionId)
            }
        }
        TrackingService.resetState()
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(context, TrackingService::class.java).apply {
            this.action = action
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
