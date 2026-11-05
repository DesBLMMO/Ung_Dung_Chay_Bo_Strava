package com.example.ung_dung_chay_bo.domain.repository

import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.FriendPost
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun observeFeed(currentUid: String): Flow<List<FriendPost>>

    suspend fun publishPost(post: FriendPost): Resource<Unit>

    suspend fun toggleKudos(postId: String, currentUid: String): Resource<Unit>
}
