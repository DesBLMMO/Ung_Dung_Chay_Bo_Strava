package com.example.ung_dung_chay_bo.domain.usecase.post

import com.example.ung_dung_chay_bo.domain.model.FriendPost
import com.example.ung_dung_chay_bo.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFeedUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(currentUid: String): Flow<List<FriendPost>> {
        return postRepository.observeFeed(currentUid)
    }
}
