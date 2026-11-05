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

        private val _trackingState = MutableStateFlow(TrackingState())
        val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

        fun resetState() {
            _trackingState.value = TrackingState()
        }
    }

    @Inject
    lateinit var trackPointDao: TrackPointDao

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var sessionId: String = UUID.randomUUID().toString()
    private var lastLocation: Location? = null
    private var timerJob: kotlinx.coroutines.Job? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        sessionId = UUID.randomUUID().toString()
        lastLocation = null
        _trackingState.value = TrackingState(
            isTracking = true,
            isPaused = false,
            sessionId = sessionId
        )
        startForegroundNotification()
        requestLocationUpdates()
        startTimer()
        Log.d(TAG, "Tracking STARTED — session: $sessionId")
    }

    private fun pauseTracking() {
        _trackingState.value = _trackingState.value.copy(isTracking = false, isPaused = true)
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        updateNotification()
        Log.d(TAG, "Tracking PAUSED")
    }

    private fun resumeTracking() {
        _trackingState.value = _trackingState.value.copy(isTracking = true, isPaused = false)
        requestLocationUpdates()
        startTimer()
        updateNotification()
        Log.d(TAG, "Tracking RESUMED")
    }

    private fun stopTracking() {
        timerJob?.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _trackingState.value = _trackingState.value.copy(isTracking = false, isPaused = false, isStopped = true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "Tracking STOPPED — session: $sessionId")
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                _trackingState.value = _trackingState.value.copy(
                    elapsedSeconds = _trackingState.value.elapsedSeconds + 1
                )
                updateNotification()
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

        val addedDistanceMeters = lastLocation?.let { prev ->
            GpsUtils.haversineDistance(
                prev.latitude, prev.longitude,
                location.latitude, location.longitude
            )
        } ?: 0.0

        val isFirstPoint = current.path.isEmpty()
        val shouldRecord = isFirstPoint || (addedDistanceMeters >= 3.0)

        val newDistanceMeters = current.totalDistanceMeters + (if (shouldRecord && !isFirstPoint) addedDistanceMeters else 0.0)
        val speedMs = if (location.hasSpeed()) location.speed else 0f
        val currentPace = GpsUtils.calculatePace(speedMs)

        val newPath = if (shouldRecord) {
            current.path + Pair(location.latitude, location.longitude)
        } else {
            current.path
        }

        _trackingState.value = current.copy(
            totalDistanceMeters = newDistanceMeters,
            currentSpeedMs = speedMs,
            currentPace = currentPace,
            path = newPath,
            currentLocation = Pair(location.latitude, location.longitude)
        )

        lastLocation = location

        if (shouldRecord && current.isTracking && !current.isPaused) {
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

        NotificationCompat.Builder(this, Constants.TRACKING_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(if (state.isPaused) "⏸ Tạm dừng" else "🏃 Đang chạy bộ")
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
    }
}
