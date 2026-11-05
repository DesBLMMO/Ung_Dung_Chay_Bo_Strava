package com.example.ung_dung_chay_bo.domain.usecase.chat

import com.example.ung_dung_chay_bo.domain.model.ChatDetailMessage
import com.example.ung_dung_chay_bo.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(conversationId: String, currentUid: String): Flow<List<ChatDetailMessage>> {
        return chatRepository.observeMessages(conversationId, currentUid)
    }
}
