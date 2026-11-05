package com.example.ung_dung_chay_bo.data.repository

import android.util.Log
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.ChatConversation
import com.example.ung_dung_chay_bo.domain.model.ChatDetailMessage
import com.example.ung_dung_chay_bo.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    companion object {
        private const val TAG = "ChatRepository"
        private const val CONVERSATIONS = "conversations"
        private const val MESSAGES = "messages"
    }

    override fun observeConversations(currentUid: String): Flow<List<ChatConversation>> =
        callbackFlow {
            val listener = firestore.collection(CONVERSATIONS)
                .whereArrayContains("participantIds", currentUid)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "observeConversations error", error)
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val convs = snapshot?.documents?.mapNotNull { doc ->
                        runCatching {
                            val participantIds = (doc.get("participantIds") as? List<*>)
                                ?.map { it.toString() } ?: emptyList()
                            val names = doc.get("participantNames") as? Map<*, *> ?: emptyMap<String, String>()
                            val friendUid = participantIds.firstOrNull { it != currentUid } ?: ""
                            val friendName = names[friendUid]?.toString() ?: "Bạn bè"
                            val lastMsg = doc.getString("lastMessage") ?: ""
                            val lastTs = doc.getLong("lastTimestamp") ?: 0L
                            ChatConversation(
                                id = doc.id,
                                participantIds = participantIds,
                                friendName = friendName,
                                friendUid = friendUid,
                                isOnline = false,
                                messages = if (lastMsg.isNotBlank()) {
                                    listOf(
                                        ChatDetailMessage(
                                            id = "last",
                                            text = lastMsg,
                                            timestamp = lastTs,
                                            time = formatTimeAgo(lastTs),
                                            isFromMe = false
                                        )
                                    )
                                } else emptyList()
                            )
                        }.onFailure { Log.e(TAG, "Error parsing conversation ${doc.id}", it) }
                            .getOrNull()
                    } ?: emptyList()
                    trySend(convs)
                }
            awaitClose { listener.remove() }
        }

    override fun observeMessages(
        conversationId: String,
        currentUid: String
    ): Flow<List<ChatDetailMessage>> = callbackFlow {
        val listener = firestore.collection(CONVERSATIONS)
            .document(conversationId)
            .collection(MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "observeMessages error for conv $conversationId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        val senderId = doc.getString("senderId") ?: ""
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        ChatDetailMessage(
                            id = doc.id,
                            senderId = senderId,
                            senderName = doc.getString("senderName") ?: "",
                            text = doc.getString("text") ?: "",
                            timestamp = timestamp,
                            time = formatTimeAgo(timestamp),
                            isFromMe = senderId == currentUid
                        )
                    }.onFailure { Log.e(TAG, "Error parsing message ${doc.id}", it) }
                        .getOrNull()
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun sendMessage(
        conversationId: String,
        currentUid: String,
        currentName: String,
        friendUid: String,
        friendName: String,
        text: String
    ): Resource<String> {
        return try {
            val convId = if (conversationId.isNotBlank()) {
                conversationId
            } else {
                getOrCreateConversationId(currentUid, currentName, friendUid, friendName)
            }

            val now = System.currentTimeMillis()
            val messageData = hashMapOf(
                "senderId" to currentUid,
                "senderName" to currentName,
                "text" to text,
                "timestamp" to now
            )
            firestore.collection(CONVERSATIONS)
                .document(convId)
                .collection(MESSAGES)
                .add(messageData)
                .await()

            firestore.collection(CONVERSATIONS)
                .document(convId)
                .update(
                    mapOf(
                        "lastMessage" to text,
                        "lastTimestamp" to now,
                        "updatedAt" to now
                    )
                ).await()

            Resource.Success(convId)
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage failed", e)
            Resource.Error("Gửi tin nhắn thất bại: ${e.message}", e)
        }
    }

    override suspend fun getOrCreateConversation(
        currentUid: String,
        currentName: String,
        friendUid: String,
        friendName: String
    ): Resource<String> {
        return try {
            val id = getOrCreateConversationId(currentUid, currentName, friendUid, friendName)
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error("Không thể tạo cuộc trò chuyện: ${e.message}", e)
        }
    }

    private suspend fun getOrCreateConversationId(
        uid1: String,
        name1: String,
        uid2: String,
        name2: String
    ): String {
        val sortedIds = listOf(uid1, uid2).sorted()
        val deterministicId = "${sortedIds[0]}_${sortedIds[1]}"
        val docRef = firestore.collection(CONVERSATIONS).document(deterministicId)
        val existing = docRef.get().await()
        if (!existing.exists()) {
            val now = System.currentTimeMillis()
            val convData = hashMapOf(
                "participantIds" to listOf(uid1, uid2),
                "participantNames" to mapOf(uid1 to name1, uid2 to name2),
                "lastMessage" to "",
                "lastTimestamp" to now,
                "updatedAt" to now
            )
            docRef.set(convData).await()
        }
        return deterministicId
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
