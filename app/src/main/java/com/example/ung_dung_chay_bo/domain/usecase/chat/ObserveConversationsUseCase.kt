package com.example.ung_dung_chay_bo.domain.usecase.chat

import com.example.ung_dung_chay_bo.domain.model.ChatConversation
import com.example.ung_dung_chay_bo.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveConversationsUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(currentUid: String): Flow<List<ChatConversation>> {
        return chatRepository.observeConversations(currentUid)
    }
}
