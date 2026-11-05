package com.example.ung_dung_chay_bo.presentation.screens.save_activity

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.core.utils.GpsUtils
import com.example.ung_dung_chay_bo.core.utils.RoadSnapService
import com.example.ung_dung_chay_bo.core.utils.RouteCatalog
import com.example.ung_dung_chay_bo.core.utils.RouteSnapshotGenerator
import com.example.ung_dung_chay_bo.data.local.dao.ActivityRecordDao
import com.example.ung_dung_chay_bo.data.local.entity.ActivityRecordEntity
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.repository.ActivityRepository
import com.example.ung_dung_chay_bo.domain.repository.UserRepository
import com.example.ung_dung_chay_bo.service.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SaveActivityUiState(
    val sessionId: String = "",
    val title: String = "Hoạt động chạy bộ",
    val description: String = "",
    val sportType: SportType = SportType.RUN,
    val distanceKm: Double = 0.0,
    val durationSeconds: Long = 0L,
    val durationFormatted: String = "00:00",
    val paceOrSpeed: String = "--:--",
    val calories: Int = 0,
    val routePoints: List<Pair<Double, Double>> = emptyList(),
    val userBackgroundImage: Bitmap? = null,
    val mapSnapshot: Bitmap? = null,
    val showcaseBitmap: Bitmap? = null,
    val isSavedSuccess: Boolean = false,
    val isPhotoSavedToGallery: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SaveActivityViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityRecordDao: ActivityRecordDao,
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SaveActivityUiState())
    val uiState: StateFlow<SaveActivityUiState> = _uiState.asStateFlow()

    init {
        loadDataFromTrackingState()
    }

    private fun loadDataFromTrackingState() {
        val trackingState = TrackingService.trackingState.value
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val timeOfDay = when (hour) {
            in 4..10 -> "buổi sáng"
            in 11..13 -> "buổi trưa"
            in 14..17 -> "buổi chiều"
            else -> "buổi tối"
        }

        val defaultTitle = "Chạy bộ $timeOfDay"

        val hasRealRoute = trackingState.path.size >= 2

        val points: List<Pair<Double, Double>>
        val distanceKm: Double
        val durationSeconds: Long
        val durationFormatted: String
        val pace: String
        val calories: Int

        if (hasRealRoute) {
            points = trackingState.path
            distanceKm = trackingState.distanceKm
            durationSeconds = trackingState.elapsedSeconds
            durationFormatted = GpsUtils.formatDuration(durationSeconds)
            pace = trackingState.currentPace
            calories = trackingState.caloriesBurned
        } else {
            val sampleRoute = RouteCatalog.getDefaultRoutes().firstOrNull { it.id == "run_1" }
                ?: RouteCatalog.getDefaultRoutes().first()

            points = sampleRoute.routePoints
            distanceKm = if (trackingState.distanceKm > 0.05) trackingState.distanceKm else sampleRoute.distanceKm
            durationSeconds = if (trackingState.elapsedSeconds > 10L) trackingState.elapsedSeconds else (sampleRoute.durationMinutes * 60L)
            durationFormatted = GpsUtils.formatDuration(durationSeconds)
            pace = if (trackingState.currentPace != "--:-- /km") trackingState.currentPace else "05:30 /km"
            calories = if (trackingState.caloriesBurned > 0) trackingState.caloriesBurned else ((distanceKm * 65).toInt())
        }

        _uiState.update {
            it.copy(
                sessionId = trackingState.sessionId.ifBlank { "session_${System.currentTimeMillis()}" },
                title = defaultTitle,
                distanceKm = distanceKm,
                durationSeconds = durationSeconds,
                durationFormatted = durationFormatted,
                paceOrSpeed = pace,
                calories = calories,
                routePoints = points
            )
        }

        generateShowcaseBitmap()

        viewModelScope.launch {
            try {
                val snapped = RoadSnapService.snapToRealRoads(
                    routeId = _uiState.value.sessionId,
                    waypoints = points,
                    sportType = _uiState.value.sportType
                )
                if (snapped.size >= 2) {
                    _uiState.update { it.copy(routePoints = snapped) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
        generateShowcaseBitmap()
    }

    fun updateDescription(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun updateSportType(sport: SportType) {
        _uiState.update { it.copy(sportType = sport) }
        generateShowcaseBitmap()
    }

    fun setUserBackgroundImage(bitmap: Bitmap?) {
        _uiState.update { it.copy(userBackgroundImage = bitmap) }
        generateShowcaseBitmap()
    }

    fun onMapSnapshotReady(bitmap: Bitmap) {
        _uiState.update { it.copy(mapSnapshot = bitmap) }
        generateShowcaseBitmap()
    }

    private fun generateShowcaseBitmap() {
        val state = _uiState.value
        val bitmap = RouteSnapshotGenerator.createRouteShowcaseBitmap(
            context = context,
            path = state.routePoints,
            distanceKm = state.distanceKm,
            durationFormatted = state.durationFormatted,
            paceOrSpeed = state.paceOrSpeed,
            calories = state.calories,
            sportTitle = state.sportType.title,
            activityTitle = state.title,
            userBackgroundImage = state.userBackgroundImage,
            mapSnapshot = state.mapSnapshot
        )
        _uiState.update { it.copy(showcaseBitmap = bitmap) }
    }

    fun savePhotoToGallery(): Uri? {
        generateShowcaseBitmap()
        val bitmap = _uiState.value.showcaseBitmap ?: return null
        val uri = RouteSnapshotGenerator.saveBitmapToGallery(context, bitmap, _uiState.value.title)
        if (uri != null) {
            _uiState.update { it.copy(isPhotoSavedToGallery = true) }
        }
        return uri
    }

    fun getShareUri(): Uri? {
        generateShowcaseBitmap()
        val bitmap = _uiState.value.showcaseBitmap ?: return null
        return RouteSnapshotGenerator.saveBitmapToCacheForSharing(context, bitmap)
    }

    fun saveActivityRecord() {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                val pointsJson = state.routePoints.joinToString(";") { "${it.first},${it.second}" }

                val record = ActivityRecordEntity(
                    sessionId = state.sessionId.ifBlank { "manual_${System.currentTimeMillis()}" },
                    title = state.title,
                    description = state.description,
                    sportType = state.sportType.name,
                    distanceKm = state.distanceKm,
                    durationSeconds = state.durationSeconds,
                    avgSpeedKmh = if (state.durationSeconds > 0) (state.distanceKm / (state.durationSeconds / 3600.0)).toFloat() else 0f,
                    avgPace = state.paceOrSpeed,
                    calories = state.calories,
                    routePointsJson = pointsJson,
                    timestamp = System.currentTimeMillis()
                )

                val result = activityRepository.saveActivity(
                    record = record,
                    publishToFeed = true,
                    caption = state.description
                )

                userRepository.updateChecklistFirstRun(true)

                TrackingService.resetState()

                _uiState.update { it.copy(isSavedSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Lỗi lưu hoạt động") }
            }
        }
    }
}
