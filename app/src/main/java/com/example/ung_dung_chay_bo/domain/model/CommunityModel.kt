package com.example.ung_dung_chay_bo.domain.model

enum class CommunityTab(val title: String) {
    CHALLENGES_EVENTS("Thử thách & Sự kiện"),
    CLUBS("Câu lạc bộ")
}

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val bannerUrl: String,
    val targetKm: Double,
    val currentKm: Double = 0.0,
    val daysLeft: Int,
    val participantsCount: Int,
    val isJoined: Boolean = false,
    val sportType: SportType = SportType.RUN,
    val badgeName: String = "Huy hiệu Vàng",
    val clubId: String? = null,
    val clubName: String? = null,
    val creatorUid: String? = null
) {
    val progressPercent: Float
        get() = if (targetKm > 0) (currentKm / targetKm).coerceIn(0.0, 1.0).toFloat() else 0f

    val progressText: String
        get() = "%.1f / %.0f km".format(currentKm, targetKm)
}

data class Club(
    val id: String,
    val name: String,
    val category: String,
    val memberCount: Int,
    val weeklyDistanceKm: Double,
    val location: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val isJoined: Boolean = false,
    val description: String,
    val sportType: SportType = SportType.RUN,
    val leaderUid: String = "",
    val leaderName: String = "",
    val isLeader: Boolean = false,
    val recentPost: ClubPost? = null,
    val posts: List<ClubPost> = emptyList(),
    val memberNames: List<String> = emptyList()
)

data class ClubPost(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String,
    val authorAvatar: String = "",
    val isLeader: Boolean = false,
    val isAnnouncement: Boolean = false,
    val timeAgo: String = "Vừa xong",
    val content: String,
    val distanceKm: Double? = null,
    val durationFormatted: String? = null,
    val pace: String? = null,
    val kudosCount: Int = 0,
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class CommunityEvent(
    val id: String,
    val title: String,
    val clubName: String,
    val clubAvatar: String,
    val dateTimeFormatted: String,
    val location: String,
    val distanceKm: Double,
    val participantsCount: Int,
    val isGoing: Boolean = false,
    val imageUrl: String,
    val sportType: SportType = SportType.RUN
)
