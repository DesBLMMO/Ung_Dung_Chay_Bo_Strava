package com.example.ung_dung_chay_bo.domain.usecase.post

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.repository.PostRepository
import javax.inject.Inject

class ToggleKudosUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, currentUid: String): Resource<Unit> {
        if (currentUid.isBlank()) return Resource.Error("Bạn cần đăng nhập")
        return postRepository.toggleKudos(postId, currentUid)
    }
}
