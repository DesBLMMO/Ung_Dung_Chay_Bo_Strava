package com.example.ung_dung_chay_bo.service

data class TrackingState(
    val sessionId: String = "",
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val isStopped: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val totalDistanceMeters: Double = 0.0,
    val currentSpeedMs: Float = 0f,
    val currentPace: String = "--:-- /km",
    val path: List<Pair<Double, Double>> = emptyList(),
    val currentLocation: Pair<Double, Double>? = null
) {
    val distanceKm: Double get() = totalDistanceMeters / 1000.0
    val caloriesBurned: Int get() = (totalDistanceMeters / 1000.0 * 65).toInt()
}
