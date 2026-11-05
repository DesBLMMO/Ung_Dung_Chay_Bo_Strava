package com.example.ung_dung_chay_bo.core.utils

import com.example.ung_dung_chay_bo.BuildConfig

object Constants {
    const val USERS_COLLECTION = "users"
    const val ACTIVITIES_COLLECTION = "activities"

    const val USER_PREFERENCES_NAME = "user_prefs"

    const val TRACKING_SERVICE_NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val TRACKING_SERVICE_NOTIFICATION_ID = 1001
    const val LOCATION_UPDATE_INTERVAL_MS = 3000L
    const val LOCATION_FASTEST_INTERVAL_MS = 1000L
    const val LOCATION_MIN_DISTANCE_METERS = 5f

    const val MIN_ACTIVITY_DURATION_SECONDS = 60

    val MAPTILER_API_KEY: String get() = runCatching { BuildConfig.MAPTILER_API_KEY }.getOrDefault("ZbjMPaEz0nGzX8JnWXNA")
    val MAPTILER_STYLE_OUTDOOR: String get() = "https://api.maptiler.com/maps/outdoor-v2/style.json?key=$MAPTILER_API_KEY"
    val MAPTILER_STYLE_STREETS: String get() = "https://api.maptiler.com/maps/streets-v2/style.json?key=$MAPTILER_API_KEY"
    val MAPTILER_STYLE_SATELLITE: String get() = "https://api.maptiler.com/maps/satellite/style.json?key=$MAPTILER_API_KEY"
    const val DEFAULT_MAP_ZOOM = 16f
    const val POLYLINE_WIDTH = 8f

    const val LONG_PRESS_DURATION_MS = 1500L
    const val KM_MILESTONE_VIBRATION_MS = 200L

    const val AUTO_PAUSE_SPEED_THRESHOLD_MS = 0.6f
    const val AUTO_RESUME_SPEED_THRESHOLD_MS = 0.85f
    const val AUTO_PAUSE_TRIGGER_SECONDS = 3
}
