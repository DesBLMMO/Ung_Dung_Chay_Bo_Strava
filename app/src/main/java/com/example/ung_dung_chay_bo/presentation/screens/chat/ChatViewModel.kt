package com.example.ung_dung_chay_bo.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.ChatConversation
import com.example.ung_dung_chay_bo.domain.model.ChatDetailMessage
import com.example.ung_dung_chay_bo.domain.usecase.chat.ObserveConversationsUseCase
import com.example.ung_dung_chay_bo.domain.usecase.chat.ObserveMessagesUseCase
import com.example.ung_dung_chay_bo.domain.usecase.chat.SendMessageUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val activeConversationId: String? = null,
    val messages: List<ChatDetailMessage> = emptyList(),
    val inputText: String = "",
    val isLoadingConversations: Boolean = true,
    val isLoadingMessages: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null
) {
    val activeConversation: ChatConversation?
        get() = conversations.find { it.id == activeConversationId }
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val observeConversationsUseCase: ObserveConversationsUseCase,
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val currentUid: String get() = firebaseAuth.currentUser?.uid ?: ""
    val currentName: String get() = firebaseAuth.currentUser?.displayName ?: "Runner"

    init {
        observeConversations()
    }

    private fun observeConversations() {
        val uid = currentUid
        if (uid.isBlank()) {
            _uiState.update { it.copy(isLoadingConversations = false) }
            return
        }
        viewModelScope.launch {
            observeConversationsUseCase(uid).collect { convs ->
                _uiState.update {
                    it.copy(conversations = convs, isLoadingConversations = false)
                }
            }
        }
    }

    fun openConversation(conversationId: String) {
        _uiState.update { it.copy(activeConversationId = conversationId, messages = emptyList(), isLoadingMessages = true) }
        viewModelScope.launch {
            observeMessagesUseCase(conversationId, currentUid).collect { msgs ->
                _uiState.update { it.copy(messages = msgs, isLoadingMessages = false) }
            }
        }
    }

    fun closeConversation() {
        _uiState.update { it.copy(activeConversationId = null, messages = emptyList()) }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun sendMessage() {
        val state = _uiState.value
        val text = state.inputText.trim()
        if (text.isBlank()) return

        val conv = state.activeConversation ?: return
        _uiState.update { it.copy(isSending = true, inputText = "") }

        viewModelScope.launch {
            val result = sendMessageUseCase(
                conversationId = conv.id,
                currentUid = currentUid,
                currentName = currentName,
                friendUid = conv.friendUid,
                friendName = conv.friendName,
                text = text
            )
            _uiState.update {
                it.copy(
                    isSending = false,
                    errorMessage = if (result is Resource.Error) result.message else null
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
