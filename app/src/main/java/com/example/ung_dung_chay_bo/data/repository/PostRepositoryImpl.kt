package com.example.ung_dung_chay_bo.data.repository

import android.net.Uri
import android.util.Log
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.FriendPost
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.repository.PostRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PostRepository {

    companion object {
        private const val TAG = "PostRepository"
        private const val POSTS_COLLECTION = "posts"
    }

    override fun observeFeed(currentUid: String): Flow<List<FriendPost>> = callbackFlow {
        val listener = firestore.collection(POSTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Feed listener error", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        val kudosUids = doc.get("kudosUids") as? List<*> ?: emptyList<String>()
                        val createdAt = doc.getLong("createdAt") ?: 0L
                        FriendPost(
                            id = doc.id,
                            authorUid = doc.getString("authorUid") ?: "",
                            authorName = doc.getString("authorName") ?: "Runner",
                            authorAvatarUrl = doc.getString("authorAvatarUrl"),
                            createdAt = createdAt,
                            postedTimeAgo = formatTimeAgo(createdAt),
                            title = doc.getString("title") ?: "",
                            caption = doc.getString("caption") ?: "",
                            sportType = runCatching {
                                SportType.valueOf(doc.getString("sportType") ?: "RUN")
                            }.getOrDefault(SportType.RUN),
                            distanceKm = doc.getDouble("distanceKm") ?: 0.0,
                            avgPace = doc.getString("avgPace") ?: "",
                            duration = doc.getString("duration") ?: "",
                            elevationGainM = doc.getLong("elevationGainM")?.toInt() ?: 0,
                            caloriesBurned = doc.getLong("caloriesBurned")?.toInt() ?: 0,
                            perceivedEffort = doc.getLong("perceivedEffort")?.toInt() ?: 5,
                            mediaUri = doc.getString("mediaUri"),
                            kudosCount = kudosUids.size,
                            isKudosGiven = currentUid in kudosUids.map { it.toString() },
                            commentCount = doc.getLong("commentCount")?.toInt() ?: 0,
                            routePoints = emptyList()
                        )
                    }.onFailure { Log.e(TAG, "Error parsing post ${doc.id}", it) }
                        .getOrNull()
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun publishPost(post: FriendPost): Resource<Unit> {
        return try {
            var remoteMediaUrl: String? = post.mediaUri
            if (!post.mediaUri.isNullOrBlank() && (post.mediaUri.startsWith("content://") || post.mediaUri.startsWith("file://"))) {
                try {
                    val storageRef = FirebaseStorage.getInstance().reference.child("posts/${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg")
                    val uri = Uri.parse(post.mediaUri)
                    storageRef.putFile(uri).await()
                    remoteMediaUrl = storageRef.downloadUrl.await().toString()
                } catch (storageError: Exception) {
                    Log.w(TAG, "Upload image to Firebase Storage failed, fallback to local uri: ${storageError.message}")
                }
            }

            val data = hashMapOf(
                "authorUid" to post.authorUid,
                "authorName" to post.authorName,
                "authorAvatarUrl" to post.authorAvatarUrl,
                "title" to post.title,
                "caption" to post.caption,
                "sportType" to post.sportType.name,
                "distanceKm" to post.distanceKm,
                "avgPace" to post.avgPace,
                "duration" to post.duration,
                "elevationGainM" to post.elevationGainM,
                "caloriesBurned" to post.caloriesBurned,
                "perceivedEffort" to post.perceivedEffort,
                "mediaUri" to remoteMediaUrl,
                "createdAt" to System.currentTimeMillis(),
                "kudosUids" to listOf<String>(),
                "commentCount" to 0
            )
            firestore.collection(POSTS_COLLECTION).add(data).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "publishPost failed", e)
            Resource.Error("Đăng bài thất bại: ${e.message}", e)
        }
    }

    override suspend fun toggleKudos(postId: String, currentUid: String): Resource<Unit> {
        return try {
            val docRef = firestore.collection(POSTS_COLLECTION).document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val kudosUids = snapshot.get("kudosUids") as? List<*> ?: emptyList<String>()
                val alreadyGiven = currentUid in kudosUids.map { it.toString() }
                if (alreadyGiven) {
                    transaction.update(docRef, "kudosUids", FieldValue.arrayRemove(currentUid))
                } else {
                    transaction.update(docRef, "kudosUids", FieldValue.arrayUnion(currentUid))
                }
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "toggleKudos failed for post $postId", e)
            Resource.Error("Không thể cập nhật kudos", e)
        }
    }

    private fun formatTimeAgo(epochMillis: Long): String {
        if (epochMillis == 0L) return ""
        val now = System.currentTimeMillis()
        val diff = now - epochMillis
        return when {
            diff < 60_000L -> "Vừa xong"
            diff < 3_600_000L -> "${diff / 60_000L} phút trước"
            diff < 86_400_000L -> "${diff / 3_600_000L} giờ trước"
            diff < 172_800_000L -> "Hôm qua"
            else -> {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi"))
                sdf.format(Date(epochMillis))
            }
        }
    }
}
