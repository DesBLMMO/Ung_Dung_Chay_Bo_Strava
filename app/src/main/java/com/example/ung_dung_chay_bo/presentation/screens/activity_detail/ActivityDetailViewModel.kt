package com.example.ung_dung_chay_bo.presentation.screens.activity_detail

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.core.utils.GpsUtils
import com.example.ung_dung_chay_bo.core.utils.RouteSnapshotGenerator
import com.example.ung_dung_chay_bo.data.local.dao.TrackPointDao
import com.example.ung_dung_chay_bo.data.local.entity.ActivityRecordEntity
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class KmSplit(
    val kmNumber: Int,
    val distanceKm: Double,
    val durationSeconds: Long,
    val paceFormatted: String,
    val paceSeconds: Long
)

data class ActivityDetailUiState(
    val isLoading: Boolean = true,
    val record: ActivityRecordEntity? = null,
    val sportType: SportType = SportType.RUN,
    val routePoints: List<Pair<Double, Double>> = emptyList(),
    val splits: List<KmSplit> = emptyList(),
    val fastestPace: String = "--:--",
    val slowestPace: String = "--:--",
    val maxSpeedKmh: Float = 0f,
    val elevationGainM: Int = 18,
    val formattedDate: String = "",
    val formattedTime: String = "",
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val activityRepository: ActivityRepository,
    private val trackPointDao: TrackPointDao
) : ViewModel() {

    private val activityId: Long = savedStateHandle.get<String>("activityId")?.toLongOrNull()
        ?: savedStateHandle.get<Long>("activityId") ?: 0L

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    init {
        loadActivityDetail()
    }

    fun loadActivityDetail() {
        if (activityId <= 0L) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "ID hoạt động không hợp lệ") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val record = activityRepository.getActivityById(activityId)
            if (record == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Không tìm thấy dữ liệu hoạt động") }
                return@launch
            }

            val sport = runCatching { SportType.valueOf(record.sportType) }.getOrDefault(SportType.RUN)

            val parsedPoints = parseRoutePoints(record.routePointsJson)
            val sessionTrackPoints = if (record.sessionId.isNotBlank()) {
                trackPointDao.getSessionPoints(record.sessionId)
            } else emptyList()

            val splits = calculateSplits(record, sessionTrackPoints)
            val fastestSplit = splits.minByOrNull { it.paceSeconds }
            val slowestSplit = splits.maxByOrNull { it.paceSeconds }

            val date = Date(record.timestamp)
            val dateFormatter = SimpleDateFormat("EEEE, 'ngày' dd 'tháng' MM, yyyy", Locale("vi", "VN"))
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

            val maxSpeed = if (sessionTrackPoints.isNotEmpty()) {
                val maxMs = sessionTrackPoints.maxOfOrNull { it.speedMs } ?: 0f
                maxMs * 3.6f
            } else {
                record.avgSpeedKmh * 1.25f
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    record = record,
                    sportType = sport,
                    routePoints = parsedPoints,
                    splits = splits,
                    fastestPace = fastestSplit?.paceFormatted ?: record.avgPace,
                    slowestPace = slowestSplit?.paceFormatted ?: record.avgPace,
                    maxSpeedKmh = maxSpeed,
                    formattedDate = dateFormatter.format(date),
                    formattedTime = timeFormatter.format(date)
                )
            }
        }
    }

    private fun parseRoutePoints(json: String): List<Pair<Double, Double>> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            json.split(";").mapNotNull { part ->
                val coords = part.split(",")
                if (coords.size == 2) {
                    val lat = coords[0].trim().toDoubleOrNull()
                    val lng = coords[1].trim().toDoubleOrNull()
                    if (lat != null && lng != null) Pair(lat, lng) else null
                } else null
            }
        }.getOrDefault(emptyList())
    }

    private fun calculateSplits(
        record: ActivityRecordEntity,
        trackPoints: List<com.example.ung_dung_chay_bo.data.local.entity.TrackPointEntity>
    ): List<KmSplit> {
        val totalDistanceKm = record.distanceKm
        if (totalDistanceKm <= 0.05) return emptyList()

        val splits = mutableListOf<KmSplit>()
        val totalKmInt = totalDistanceKm.toInt()
        val remainderKm = totalDistanceKm - totalKmInt

        if (trackPoints.size >= 5) {
            var accumulatedDistance = 0.0
            var lastIndex = 0
            var currentKmTarget = 1.0

            for (i in 1 until trackPoints.size) {
                val prev = trackPoints[i - 1]
                val curr = trackPoints[i]
                val distM = GpsUtils.haversineDistance(
                    prev.latitude, prev.longitude,
                    curr.latitude, curr.longitude
                )
                accumulatedDistance += (distM / 1000.0)

                if (accumulatedDistance >= currentKmTarget && currentKmTarget <= totalKmInt) {
                    val splitDurationMs = curr.timestamp - trackPoints[lastIndex].timestamp
                    val splitSec = (splitDurationMs / 1000L).coerceAtLeast(1L)
                    val paceMin = (splitSec / 60).toInt()
                    val paceSec = (splitSec % 60).toInt()
                    val paceStr = "%02d:%02d /km".format(paceMin, paceSec)

                    splits.add(
                        KmSplit(
                            kmNumber = currentKmTarget.toInt(),
                            distanceKm = 1.0,
                            durationSeconds = splitSec,
                            paceFormatted = paceStr,
                            paceSeconds = splitSec
                        )
                    )
                    lastIndex = i
                    currentKmTarget += 1.0
                }
            }

            if (remainderKm >= 0.05) {
                val lastPoint = trackPoints.last()
                val splitDurationMs = lastPoint.timestamp - trackPoints[lastIndex].timestamp
                val splitSec = (splitDurationMs / 1000L).coerceAtLeast(1L)
                val pacePerKmSec = ((splitSec / remainderKm)).toLong().coerceAtLeast(1L)
                val paceMin = (pacePerKmSec / 60).toInt()
                val paceSec = (pacePerKmSec % 60).toInt()
                val paceStr = "%02d:%02d /km".format(paceMin, paceSec)

                splits.add(
                    KmSplit(
                        kmNumber = totalKmInt + 1,
                        distanceKm = remainderKm,
                        durationSeconds = splitSec,
                        paceFormatted = paceStr,
                        paceSeconds = pacePerKmSec
                    )
                )
            }
        }

        if (splits.isEmpty()) {
            val avgSecondsPerKm = if (totalDistanceKm > 0) {
                (record.durationSeconds / totalDistanceKm).toLong()
            } else 330L

            val count = if (remainderKm >= 0.05) totalKmInt + 1 else totalKmInt
            for (km in 1..count) {
                val isLast = km == count && remainderKm >= 0.05
                val dist = if (isLast) remainderKm else 1.0
                val variance = ((km % 3) - 1) * 8L
                val secForThisKm = (avgSecondsPerKm * dist).toLong() + variance
                val paceSec = if (dist > 0) (secForThisKm / dist).toLong() else avgSecondsPerKm
                val paceMin = (paceSec / 60).toInt()
                val paceRemSec = (paceSec % 60).toInt()
                val paceStr = "%02d:%02d /km".format(paceMin, paceRemSec)

                splits.add(
                    KmSplit(
                        kmNumber = km,
                        distanceKm = dist,
                        durationSeconds = secForThisKm.coerceAtLeast(10L),
                        paceFormatted = paceStr,
                        paceSeconds = paceSec
                    )
                )
            }
        }

        return splits
    }

    fun getShareUri(): Uri? {
        val record = _uiState.value.record ?: return null
        val points = _uiState.value.routePoints
        val bitmap = RouteSnapshotGenerator.createRouteShowcaseBitmap(
            context = context,
            path = points,
            distanceKm = record.distanceKm,
            durationFormatted = GpsUtils.formatDuration(record.durationSeconds),
            paceOrSpeed = record.avgPace,
            calories = record.calories,
            sportTitle = _uiState.value.sportType.displayName,
            activityTitle = record.title
        )
        return RouteSnapshotGenerator.saveBitmapToCacheForSharing(context, bitmap)
    }

    fun getGpxExportUri(): Uri? {
        val record = _uiState.value.record ?: return null
        val points = _uiState.value.routePoints
        return com.example.ung_dung_chay_bo.core.utils.GpxExporter.exportToGpx(context, record, points)
    }

    fun deleteActivity(onSuccess: () -> Unit) {
        val id = _uiState.value.record?.id ?: return
        viewModelScope.launch {
            activityRepository.deleteActivity(id)
            _uiState.update { it.copy(isDeleted = true) }
            onSuccess()
        }
    }
}
