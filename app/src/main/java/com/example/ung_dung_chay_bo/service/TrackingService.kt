package com.example.ung_dung_chay_bo.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ung_dung_chay_bo.MainActivity
import com.example.ung_dung_chay_bo.core.utils.AudioCueHelper
import com.example.ung_dung_chay_bo.core.utils.Constants
import com.example.ung_dung_chay_bo.core.utils.GpsUtils
import com.example.ung_dung_chay_bo.data.local.dao.TrackPointDao
import com.example.ung_dung_chay_bo.data.local.entity.TrackPointEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : Service() {

    companion object {
        private const val TAG = "TrackingService"

        const val ACTION_START = "ACTION_START_TRACKING"
        const val ACTION_PAUSE = "ACTION_PAUSE_TRACKING"
        const val ACTION_RESUME = "ACTION_RESUME_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_TRACKING"
        const val ACTION_TOGGLE_AUTO_PAUSE = "ACTION_TOGGLE_AUTO_PAUSE"
        const val ACTION_TOGGLE_AUDIO_CUES = "ACTION_TOGGLE_AUDIO_CUES"

        private val _trackingState = MutableStateFlow(TrackingState())
        val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

        fun resetState() {
            _trackingState.value = TrackingState(
                isAutoPauseEnabled = _trackingState.value.isAutoPauseEnabled,
                isAudioCuesEnabled = _trackingState.value.isAudioCuesEnabled
            )
        }
    }

    @Inject
    lateinit var trackPointDao: TrackPointDao

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var audioCueHelper: AudioCueHelper
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var sessionId: String = UUID.randomUUID().toString()
    private var lastLocation: Location? = null
    private var timerJob: kotlinx.coroutines.Job? = null

    // Auto-pause and milestone counters
    private var stationaryUpdateCount = 0
    private var movingUpdateCount = 0
    private var lastKmMilestone = 0
    private var lastKmElapsedSeconds = 0L

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        audioCueHelper = AudioCueHelper(this)
        setupLocationCallback()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
            ACTION_TOGGLE_AUTO_PAUSE -> toggleAutoPause()
            ACTION_TOGGLE_AUDIO_CUES -> toggleAudioCues()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        sessionId = UUID.randomUUID().toString()
        lastLocation = null
        stationaryUpdateCount = 0
        movingUpdateCount = 0
        lastKmMilestone = 0
        lastKmElapsedSeconds = 0L

        _trackingState.value = _trackingState.value.copy(
            sessionId = sessionId,
            isTracking = true,
            isPaused = false,
            isAutoPaused = false,
            isStopped = false,
            elapsedSeconds = 0L,
            totalDistanceMeters = 0.0,
            path = emptyList(),
            currentLocation = null
        )

        startForegroundNotification()
        requestLocationUpdates()
        startTimer()

        audioCueHelper.speak("Bắt đầu bài tập", _trackingState.value.isAudioCuesEnabled)
        Log.d(TAG, "Tracking STARTED — session: $sessionId")
    }

    private fun pauseTracking() {
        _trackingState.value = _trackingState.value.copy(isTracking = false, isPaused = true, isAutoPaused = false)
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        updateNotification()
        audioCueHelper.speak("Đã tạm dừng bài tập", _trackingState.value.isAudioCuesEnabled)
        Log.d(TAG, "Tracking PAUSED")
    }

    private fun resumeTracking() {
        _trackingState.value = _trackingState.value.copy(isTracking = true, isPaused = false, isAutoPaused = false)
        requestLocationUpdates()
        startTimer()
        updateNotification()
        audioCueHelper.speak("Tiếp tục bài tập", _trackingState.value.isAudioCuesEnabled)
        Log.d(TAG, "Tracking RESUMED")
    }

    private fun stopTracking() {
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        val finalState = _trackingState.value
        _trackingState.value = finalState.copy(
            isTracking = false,
            isPaused = false,
            isAutoPaused = false,
            isStopped = true
        )

        val distKmStr = "%.2f km".format(finalState.distanceKm)
        audioCueHelper.speak(
            "Đã hoàn thành bài tập. Tổng quãng đường: $distKmStr. Thời gian: ${GpsUtils.formatDuration(finalState.elapsedSeconds)}.",
            finalState.isAudioCuesEnabled
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "Tracking STOPPED — session: $sessionId")
    }

    private fun toggleAutoPause() {
        val next = !_trackingState.value.isAutoPauseEnabled
        _trackingState.value = _trackingState.value.copy(isAutoPauseEnabled = next)
        val speech = if (next) "Đã bật tự động tạm dừng" else "Đã tắt tự động tạm dừng"
        audioCueHelper.speak(speech, _trackingState.value.isAudioCuesEnabled)
    }

    private fun toggleAudioCues() {
        val next = !_trackingState.value.isAudioCuesEnabled
        _trackingState.value = _trackingState.value.copy(isAudioCuesEnabled = next)
        if (next) {
            audioCueHelper.speak("Đã bật thông báo giọng nói", true)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                val current = _trackingState.value
                if (current.isTimerActive) {
                    _trackingState.value = current.copy(
                        elapsedSeconds = current.elapsedSeconds + 1
                    )
                    updateNotification()
                }
            }
        }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                processNewLocation(location)
            }
        }
    }

    @Suppress("MissingPermission")
    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            Constants.LOCATION_UPDATE_INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(Constants.LOCATION_FASTEST_INTERVAL_MS)
            .setMinUpdateDistanceMeters(Constants.LOCATION_MIN_DISTANCE_METERS)
            .build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun processNewLocation(location: Location) {
        val current = _trackingState.value
        val speedMs = if (location.hasSpeed()) location.speed else 0f
        val currentPace = GpsUtils.calculatePace(speedMs)

        // 1. Kiểm tra Auto-Pause nếu bật tính năng
        if (current.isTracking && !current.isPaused && current.isAutoPauseEnabled) {
            if (speedMs < Constants.AUTO_PAUSE_SPEED_THRESHOLD_MS) {
                stationaryUpdateCount++
                movingUpdateCount = 0
                if (stationaryUpdateCount >= Constants.AUTO_PAUSE_TRIGGER_SECONDS && !current.isAutoPaused) {
                    _trackingState.value = _trackingState.value.copy(isAutoPaused = true)
                    audioCueHelper.speak("Tự động tạm dừng", current.isAudioCuesEnabled)
                    audioCueHelper.vibrate(120)
                    updateNotification()
                    Log.d(TAG, "Auto-Pause TRIGGERED: speed $speedMs m/s")
                }
            } else if (speedMs >= Constants.AUTO_RESUME_SPEED_THRESHOLD_MS) {
                movingUpdateCount++
                stationaryUpdateCount = 0
                if (current.isAutoPaused && movingUpdateCount >= 1) {
                    _trackingState.value = _trackingState.value.copy(isAutoPaused = false)
                    audioCueHelper.speak("Tiếp tục chạy", current.isAudioCuesEnabled)
                    audioCueHelper.vibrate(120)
                    updateNotification()
                    Log.d(TAG, "Auto-Resume TRIGGERED: speed $speedMs m/s")
                }
            }
        }

        // 2. Tính toán khoảng cách
        val isPausedOrAutoPaused = current.isPaused || current.isAutoPaused
        val addedDistanceMeters = if (!isPausedOrAutoPaused) {
            lastLocation?.let { prev ->
                GpsUtils.haversineDistance(
                    prev.latitude, prev.longitude,
                    location.latitude, location.longitude
                )
            } ?: 0.0
        } else 0.0

        val isFirstPoint = current.path.isEmpty()
        val shouldRecord = (isFirstPoint || addedDistanceMeters >= 3.0) && !isPausedOrAutoPaused

        val newDistanceMeters = current.totalDistanceMeters + (if (shouldRecord && !isFirstPoint) addedDistanceMeters else 0.0)

        val newPath = if (shouldRecord) {
            current.path + Pair(location.latitude, location.longitude)
        } else {
            current.path
        }

        _trackingState.value = _trackingState.value.copy(
            totalDistanceMeters = newDistanceMeters,
            currentSpeedMs = speedMs,
            currentPace = currentPace,
            path = newPath,
            currentLocation = Pair(location.latitude, location.longitude)
        )

        lastLocation = location

        // 3. Thông báo hoàn thành từng Km (Audio Cue Milestone)
        if (shouldRecord) {
            val currentKm = (newDistanceMeters / 1000.0).toInt()
            if (currentKm > lastKmMilestone && currentKm > 0) {
                val splitSec = current.elapsedSeconds - lastKmElapsedSeconds
                lastKmElapsedSeconds = current.elapsedSeconds
                lastKmMilestone = currentKm

                audioCueHelper.vibrate(Constants.KM_MILESTONE_VIBRATION_MS)
                val splitMin = splitSec / 60
                val splitRemSec = splitSec % 60
                val paceText = currentPace.replace(" /km", "")
                val speech = "Cây số $currentKm. Thời gian $splitMin phút $splitRemSec giây. Tốc độ hiện tại $paceText một cây số."
                audioCueHelper.speak(speech, current.isAudioCuesEnabled)
                Log.d(TAG, "Milestone Km $currentKm completed in ${splitMin}m${splitRemSec}s")
            }

            // Ghi tọa độ vào Room Database
            serviceScope.launch {
                trackPointDao.insertPoint(
                    TrackPointEntity(
                        sessionId = sessionId,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        speedMs = speedMs,
                        accuracy = location.accuracy,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.TRACKING_SERVICE_NOTIFICATION_CHANNEL_ID,
                "Đang theo dõi hoạt động",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị thông tin buổi chạy bộ đang diễn ra"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification() = run {
        val state = _trackingState.value
        val distanceKm = state.totalDistanceMeters / 1000.0
        val contentText = "📍 %.2f km · ⏱ %s · %s".format(
            distanceKm,
            GpsUtils.formatDuration(state.elapsedSeconds),
            state.currentPace
        )

        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            state.isAutoPaused -> "⏸ Tự động tạm dừng (Đứng yên)"
            state.isPaused -> "⏸ Tạm dừng"
            else -> "🏃 Đang chạy bộ"
        }

        NotificationCompat.Builder(this, Constants.TRACKING_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(openIntent)
            .build()
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Constants.TRACKING_SERVICE_NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(Constants.TRACKING_SERVICE_NOTIFICATION_ID, buildNotification())
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(Constants.TRACKING_SERVICE_NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        audioCueHelper.shutdown()
    }
}
