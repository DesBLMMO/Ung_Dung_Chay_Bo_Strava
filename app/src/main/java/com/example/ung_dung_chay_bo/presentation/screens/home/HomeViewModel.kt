package com.example.ung_dung_chay_bo.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.domain.model.AppNotification
import com.example.ung_dung_chay_bo.domain.model.ChatMessage
import com.example.ung_dung_chay_bo.domain.model.FriendPost
import com.example.ung_dung_chay_bo.domain.model.SuggestedUser
import com.example.ung_dung_chay_bo.domain.repository.ChatRepository
import com.example.ung_dung_chay_bo.domain.repository.UserRepository
import com.example.ung_dung_chay_bo.domain.usecase.post.ObserveFeedUseCase
import com.example.ung_dung_chay_bo.domain.usecase.post.PublishPostUseCase
import com.example.ung_dung_chay_bo.domain.usecase.post.ToggleKudosUseCase
import com.example.ung_dung_chay_bo.domain.usecase.user.GetUserProfileUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val observeFeedUseCase: ObserveFeedUseCase,
    private val publishPostUseCase: PublishPostUseCase,
    private val toggleKudosUseCase: ToggleKudosUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val currentUid: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    val currentDisplayName: String
        get() = firebaseAuth.currentUser?.displayName ?: "Runner"

    init {
        loadUserProfile()
        observeFirestoreFeed()
        observeSuggestedUsers()
        observeNotifications()
        observeChatConversations()
        observeCommunityChecklist()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            getUserProfileUseCase().collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
    }

    private fun observeFirestoreFeed() {
        viewModelScope.launch {
            val uid = currentUid
            observeFeedUseCase(uid).collect { posts ->
                _uiState.update { it.copy(friendPosts = posts, isFeedLoading = false) }
            }
        }
    }

    private fun observeSuggestedUsers() {
        viewModelScope.launch {
            val uid = currentUid
            userRepository.getSuggestedUsers(uid).collect { users ->
                _uiState.update { it.copy(suggestedUsers = users) }
            }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            val uid = currentUid
            userRepository.getNotifications(uid).collect { notifs ->
                _uiState.update { it.copy(notifications = notifs) }
            }
        }
    }

    private fun observeChatConversations() {
        viewModelScope.launch {
            val uid = currentUid
            if (uid.isEmpty()) return@launch
            chatRepository.observeConversations(uid).collect { convs ->
                val chats = convs.map { conv ->
                    val lastMsg = conv.messages.firstOrNull()
                    ChatMessage(
                        id = conv.id,
                        senderName = conv.friendName,
                        messageText = lastMsg?.text ?: "Bắt đầu cuộc trò chuyện",
                        timeAgo = lastMsg?.time ?: "Gần đây",
                        unreadCount = 0
                    )
                }
                if (chats.isNotEmpty()) {
                    _uiState.update { it.copy(chatMessages = chats) }
                }
            }
        }
    }

    private fun observeCommunityChecklist() {
        viewModelScope.launch {
            userRepository.getCommunityChecklistState().collect { state ->
                _uiState.update { it.copy(checklistState = state) }
            }
        }
    }

    fun publishPost(post: FriendPost, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPostPublishing = true) }
            val filledPost = post.copy(
                authorUid = currentUid,
                authorName = currentDisplayName
            )
            val result = publishPostUseCase(filledPost)
            _uiState.update { it.copy(isPostPublishing = false) }
            when (result) {
                is com.example.ung_dung_chay_bo.core.utils.Resource.Error -> onError(result.message)
                else -> {
                    userRepository.updateChecklistFirstRun(true)
                }
            }
        }
    }

    fun addNewPost(post: FriendPost) {
        publishPost(post)
    }

    fun onToggleKudos(postId: String) {
        _uiState.update { current ->
            val updated = current.friendPosts.map { p ->
                if (p.id == postId) {
                    val giving = !p.isKudosGiven
                    p.copy(
                        isKudosGiven = giving,
                        kudosCount = if (giving) p.kudosCount + 1 else maxOf(0, p.kudosCount - 1)
                    )
                } else p
            }
            current.copy(friendPosts = updated)
        }
        viewModelScope.launch {
            toggleKudosUseCase(postId, currentUid)
        }
    }

    fun onToggleFollow(userId: String) {
        val target = _uiState.value.suggestedUsers.find { it.id == userId }
        val targetName = target?.name ?: "Runner"

        _uiState.update { current ->
            val updated = current.suggestedUsers.map { u ->
                if (u.id == userId) u.copy(isFollowing = !u.isFollowing) else u
            }
            current.copy(suggestedUsers = updated)
        }

        viewModelScope.launch {
            userRepository.toggleFollow(currentUid, userId, targetName)
        }
    }

    fun openDeviceDialog() = _uiState.update { it.copy(isDeviceDialogVisible = true) }
    fun closeDeviceDialog() = _uiState.update { it.copy(isDeviceDialogVisible = false) }

    fun onDeviceConnected(deviceName: String) {
        viewModelScope.launch { userRepository.updateChecklistDevice(true, deviceName) }
        closeDeviceDialog()
    }

    fun onSkipDevice() {
        viewModelScope.launch { userRepository.updateChecklistDevice(true, null) }
        closeDeviceDialog()
    }

    fun dismissCommunityBanner() {
        viewModelScope.launch { userRepository.dismissCommunityBanner() }
    }

    fun openDialog(dialogType: HomeDialogType) = _uiState.update { it.copy(activeDialog = dialogType) }
    fun closeDialog() = _uiState.update { it.copy(activeDialog = null) }
}
