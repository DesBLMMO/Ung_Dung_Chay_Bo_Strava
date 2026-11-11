package com.example.ung_dung_chay_bo.domain.model

enum class RouteDifficulty(val title: String) {
    EASY("Dễ dàng"),
    MODERATE("Trung bình"),
    HARD("Thử thách")
}

enum class SurfaceType(val title: String) {
    ALL("Tất cả"),
    PAVED("Đường nhựa"),
    TRAIL("Đường mòn"),
    PARK("Công viên"),
    MIXED("Hỗn hợp")
}

data class SuggestedRoute(
    val id: String,
    val title: String,
    val sportType: SportType,
    val distanceKm: Double,
    val durationMinutes: Int,
    val elevationGainM: Int = 20,
    val difficulty: RouteDifficulty = RouteDifficulty.EASY,
    val surfaceType: SurfaceType = SurfaceType.PAVED,
    val startLocationName: String = "Vị trí hiện tại",
    val areaName: String,
    val isPersonalized: Boolean = true,
    val isSaved: Boolean = false,
    val startPoint: Pair<Double, Double>,
    val routePoints: List<Pair<Double, Double>>,
    val routesInAreaCount: Int = 1,
    val imageUrl: String? = null,
    val runnersCount: Int = 0 // Số lượng người trong cộng đồng đã chạy theo lộ trình này
) {
    val durationFormatted: String
        get() {
            val hours = durationMinutes / 60
            val mins = durationMinutes % 60
            return if (hours > 0) "%d giờ %02d phút".format(hours, mins) else "%d phút".format(mins)
        }

    val runnersCountFormatted: String
        get() = when {
            runnersCount >= 1000 -> "%.1fk người chạy".format(runnersCount / 1000.0).replace(".0k", "k")
            runnersCount > 0 -> "$runnersCount người chạy"
            else -> ""
        }
}
