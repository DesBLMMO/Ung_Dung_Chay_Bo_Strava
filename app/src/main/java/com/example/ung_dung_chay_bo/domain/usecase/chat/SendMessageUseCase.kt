package com.example.ung_dung_chay_bo.domain.usecase.chat

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        currentUid: String,
        currentName: String,
        friendUid: String,
        friendName: String,
        text: String
    ): Resource<String> {
        if (text.isBlank()) return Resource.Error("Tin nhắn không được để trống")
        if (currentUid.isBlank()) return Resource.Error("Bạn cần đăng nhập")
        return chatRepository.sendMessage(
            conversationId = conversationId,
            currentUid = currentUid,
            currentName = currentName,
            friendUid = friendUid,
            friendName = friendName,
            text = text.trim()
        )
    }
}
