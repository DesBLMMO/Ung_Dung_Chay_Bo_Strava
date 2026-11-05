package com.example.ung_dung_chay_bo.domain.usecase.post

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.FriendPost
import com.example.ung_dung_chay_bo.domain.repository.PostRepository
import javax.inject.Inject

class PublishPostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(post: FriendPost): Resource<Unit> {
        if (post.title.isBlank()) return Resource.Error("Vui lòng nhập tên hoạt động")
        if (post.authorUid.isBlank()) return Resource.Error("Bạn cần đăng nhập để đăng bài")
        if (post.distanceKm <= 0) return Resource.Error("Quãng đường phải lớn hơn 0")
        return postRepository.publishPost(post)
    }
}
