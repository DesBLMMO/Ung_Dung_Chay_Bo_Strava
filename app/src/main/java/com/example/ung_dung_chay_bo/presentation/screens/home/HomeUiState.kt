package com.example.ung_dung_chay_bo.presentation.screens.home

import com.example.ung_dung_chay_bo.domain.model.AppNotification
import com.example.ung_dung_chay_bo.domain.model.ChatMessage
import com.example.ung_dung_chay_bo.domain.model.CommunityChecklistState
import com.example.ung_dung_chay_bo.domain.model.FriendPost
import com.example.ung_dung_chay_bo.domain.model.HealthConnectStatus
import com.example.ung_dung_chay_bo.domain.model.HealthSummary
import com.example.ung_dung_chay_bo.domain.model.SuggestedUser
import com.example.ung_dung_chay_bo.domain.model.UserProfile

data class ChecklistItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean = false
)

data class HomeUiState(
    val userProfile: UserProfile? = null,
    val weeklyStreakWeeks: Int = 0,
    val checklistState: CommunityChecklistState = CommunityChecklistState(),
    val suggestedUsers: List<SuggestedUser> = emptyList(),
    val friendPosts: List<FriendPost> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val isDeviceDialogVisible: Boolean = false,
    val healthConnectStatus: HealthConnectStatus = HealthConnectStatus.NOT_SUPPORTED,
    val isHealthConnectLinked: Boolean = false,
    val healthSummary: HealthSummary? = null,
    val activeDialog: HomeDialogType? = null,
    val isFeedLoading: Boolean = false,
    val isPostPublishing: Boolean = false
)

enum class HomeDialogType {
    SEARCH_FRIENDS,
    MESSAGES,
    NOTIFICATIONS
}
