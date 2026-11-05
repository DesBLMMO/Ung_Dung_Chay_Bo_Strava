package com.example.ung_dung_chay_bo.domain.model

enum class SportType(val title: String) {
    RUN("Chạy bộ"),
    WALK("Đi bộ"),
    RIDE("Đạp xe"),
    TRAIL_RUN("Chạy địa hình")
}

enum class PostVisibility(val title: String) {
    EVERYONE("Mọi người"),
    FOLLOWERS("Người theo dõi"),
    ONLY_ME("Chỉ mình tôi")
}

data class FriendPost(
    val id: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,
    val postedTimeAgo: String = "",
    val createdAt: Long = 0L,
    val title: String = "",
    val caption: String = "",
    val sportType: SportType = SportType.RUN,
    val distanceKm: Double = 0.0,
    val avgPace: String = "",
    val duration: String = "",
    val elevationGainM: Int = 0,
    val caloriesBurned: Int = 0,
    val perceivedEffort: Int = 5,
    val mediaUri: String? = null,
    val kudosCount: Int = 0,
    val commentCount: Int = 0,
    val isKudosGiven: Boolean = false,
    val routePoints: List<Pair<Float, Float>> = emptyList()
)

data class SuggestedUser(
    val id: String,
    val name: String,
    val username: String = "",
    val role: String,
    val location: String = "Việt Nam",
    val mutualFriendsCount: Int = 0,
    val weeklyDistanceKm: Double,
    val isFollowing: Boolean = false
)

data class RunningClub(
    val id: String,
    val name: String,
    val location: String,
    val memberCount: Int,
    val sportType: SportType = SportType.RUN,
    val description: String = "",
    val isJoined: Boolean = false
)

data class AppNotification(
    val id: String,
    val senderName: String,
    val actionText: String,
    val timeAgo: String,
    val isRead: Boolean = false
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val messageText: String,
    val timeAgo: String,
    val unreadCount: Int = 0
)

data class ChatDetailMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isFromMe: Boolean = false,
    val time: String = ""
)

data class ChatConversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val friendName: String = "",
    val friendUid: String = "",
    val isOnline: Boolean = true,
    val messages: List<ChatDetailMessage> = emptyList()
) {
    val lastMessage: String
        get() = messages.lastOrNull()?.text ?: ""
    val timeAgo: String
        get() = messages.lastOrNull()?.time ?: ""
}
