package com.example.ung_dung_chay_bo.presentation.screens.tracking

import android.content.Context
import android.content.Intent
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

    private val _selectedSport = MutableStateFlow(SportType.RIDE)
    val selectedSport: StateFlow<SportType> = _selectedSport.asStateFlow()

    private val _selectedRoute = MutableStateFlow<SuggestedRoute?>(null)
    val selectedRoute: StateFlow<SuggestedRoute?> = _selectedRoute.asStateFlow()

    private val _availableRoutes = MutableStateFlow(RouteCatalog.getDefaultRoutes())
    val availableRoutes: StateFlow<List<SuggestedRoute>> = _availableRoutes.asStateFlow()

    private val _layerStyle = MutableStateFlow(MapTilerLayerStyle.OUTDOOR)
    val layerStyle: StateFlow<MapTilerLayerStyle> = _layerStyle.asStateFlow()

    private val _is3D = MutableStateFlow(false)
    val is3D: StateFlow<Boolean> = _is3D.asStateFlow()

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
