package com.example.ung_dung_chay_bo.domain.repository

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.ChatConversation
import com.example.ung_dung_chay_bo.domain.model.ChatDetailMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeConversations(currentUid: String): Flow<List<ChatConversation>>

    fun observeMessages(conversationId: String, currentUid: String): Flow<List<ChatDetailMessage>>

    suspend fun sendMessage(
        conversationId: String,
        currentUid: String,
        currentName: String,
        friendUid: String,
        friendName: String,
        text: String
    ): Resource<String>

    suspend fun getOrCreateConversation(
        currentUid: String,
        currentName: String,
        friendUid: String,
        friendName: String
    ): Resource<String>
}
