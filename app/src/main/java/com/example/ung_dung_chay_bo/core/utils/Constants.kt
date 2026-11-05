package com.example.ung_dung_chay_bo.core.utils

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

    const val MAPTILER_API_KEY = "ZbjMPaEz0nGzX8JnWXNA"
    const val MAPTILER_STYLE_OUTDOOR = "https://api.maptiler.com/maps/outdoor-v2/style.json?key=ZbjMPaEz0nGzX8JnWXNA"
    const val MAPTILER_STYLE_STREETS = "https://api.maptiler.com/maps/streets-v2/style.json?key=ZbjMPaEz0nGzX8JnWXNA"
    const val MAPTILER_STYLE_SATELLITE = "https://api.maptiler.com/maps/satellite/style.json?key=ZbjMPaEz0nGzX8JnWXNA"
    const val DEFAULT_MAP_ZOOM = 16f
    const val POLYLINE_WIDTH = 8f

    const val LONG_PRESS_DURATION_MS = 1500L
    const val KM_MILESTONE_VIBRATION_MS = 200L
}
