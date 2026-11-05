package com.example.ung_dung_chay_bo.data.repository

import android.util.Log
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.Challenge
import com.example.ung_dung_chay_bo.domain.model.Club
import com.example.ung_dung_chay_bo.domain.model.ClubPost
import com.example.ung_dung_chay_bo.domain.model.CommunityEvent
import com.example.ung_dung_chay_bo.domain.model.SportType
import com.example.ung_dung_chay_bo.domain.repository.CommunityRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommunityRepository {

    companion object {
        private const val TAG = "CommunityRepository"
        private const val CHALLENGES_COLLECTION = "challenges"
        private const val CLUBS_COLLECTION = "clubs"
        private const val EVENTS_COLLECTION = "community_events"
    }

    override fun observeChallenges(currentUid: String): Flow<List<Challenge>> = callbackFlow {
        val listener = firestore.collection(CHALLENGES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lấy challenges: ${error.message}")
                    trySend(getDefaultChallenges(currentUid))
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    seedInitialChallenges()
                    trySend(getDefaultChallenges(currentUid))
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val participantUids = (doc.get("participantUids") as? List<*>)?.map { it.toString() } ?: emptyList()
                        val isJoined = currentUid.isNotEmpty() && currentUid in participantUids
                        val baseCount = doc.getLong("baseParticipantsCount")?.toInt() ?: 120
                        val totalParticipants = baseCount + participantUids.size

                        Challenge(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            bannerUrl = doc.getString("bannerUrl") ?: "",
                            targetKm = doc.getDouble("targetKm") ?: 50.0,
                            currentKm = if (isJoined) (doc.getDouble("currentKm") ?: 12.5) else 0.0,
                            daysLeft = doc.getLong("daysLeft")?.toInt() ?: 14,
                            participantsCount = totalParticipants,
                            isJoined = isJoined,
                            sportType = runCatching { SportType.valueOf(doc.getString("sportType") ?: "RUN") }.getOrDefault(SportType.RUN),
                            badgeName = doc.getString("badgeName") ?: "Huy hiệu Vàng",
                            clubId = doc.getString("clubId"),
                            clubName = doc.getString("clubName"),
                            creatorUid = doc.getString("creatorUid")
                        )
                    }.getOrNull()
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override fun observeClubs(currentUid: String): Flow<List<Club>> = callbackFlow {
        val listener = firestore.collection(CLUBS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lấy clubs: ${error.message}")
                    trySend(getDefaultClubs(currentUid))
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    seedInitialClubs()
                    trySend(getDefaultClubs(currentUid))
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val memberUids = (doc.get("memberUids") as? List<*>)?.map { it.toString() } ?: emptyList()
                        val leaderUid = doc.getString("leaderUid") ?: ""
                        val leaderName = doc.getString("leaderName") ?: "Ban Quản Trị"
                        val isLeader = currentUid.isNotEmpty() && currentUid == leaderUid
                        val isJoined = (currentUid.isNotEmpty() && currentUid in memberUids) || isLeader
                        val baseMembers = doc.getLong("baseMemberCount")?.toInt() ?: 0
                        val totalMembers = maxOf(1, baseMembers + memberUids.size)
                        val memberNames = (doc.get("memberNames") as? List<*>)?.map { it.toString() } ?: emptyList()

                        Club(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            category = doc.getString("category") ?: "Chạy bộ",
                            memberCount = totalMembers,
                            weeklyDistanceKm = doc.getDouble("weeklyDistanceKm") ?: 0.0,
                            location = doc.getString("location") ?: "Việt Nam",
                            avatarUrl = doc.getString("avatarUrl") ?: "",
                            bannerUrl = doc.getString("bannerUrl") ?: "",
                            isJoined = isJoined,
                            description = doc.getString("description") ?: "",
                            sportType = runCatching { SportType.valueOf(doc.getString("sportType") ?: "RUN") }.getOrDefault(SportType.RUN),
                            leaderUid = leaderUid,
                            leaderName = leaderName,
                            isLeader = isLeader,
                            memberNames = memberNames,
                            recentPost = ClubPost(
                                authorName = doc.getString("recentAuthor") ?: leaderName,
                                authorAvatar = "",
                                isLeader = true,
                                timeAgo = "Gần đây",
                                content = doc.getString("recentPostContent") ?: "Chào mừng bạn tham gia câu lạc bộ!",
                                kudosCount = 12,
                                commentsCount = 3
                            )
                        )
                    }.getOrNull()
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override fun observeEvents(currentUid: String): Flow<List<CommunityEvent>> = callbackFlow {
        val listener = firestore.collection(EVENTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Lỗi lấy events: ${error.message}")
                    trySend(getDefaultEvents(currentUid))
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    seedInitialEvents()
                    trySend(getDefaultEvents(currentUid))
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val attendeeUids = (doc.get("attendeeUids") as? List<*>)?.map { it.toString() } ?: emptyList()
                        val isGoing = currentUid.isNotEmpty() && currentUid in attendeeUids
                        val baseAttendees = doc.getLong("baseParticipantsCount")?.toInt() ?: 45
                        val totalParticipants = baseAttendees + attendeeUids.size

                        CommunityEvent(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            clubName = doc.getString("clubName") ?: "",
                            clubAvatar = doc.getString("clubAvatar") ?: "",
                            dateTimeFormatted = doc.getString("dateTimeFormatted") ?: "Cuối tuần này",
                            location = doc.getString("location") ?: "Hà Nội",
                            distanceKm = doc.getDouble("distanceKm") ?: 10.0,
                            participantsCount = totalParticipants,
                            isGoing = isGoing,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            sportType = runCatching { SportType.valueOf(doc.getString("sportType") ?: "RUN") }.getOrDefault(SportType.RUN)
                        )
                    }.getOrNull()
                }
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun toggleJoinChallenge(challengeId: String, currentUid: String): Resource<Boolean> {
        if (currentUid.isEmpty()) return Resource.Error("Vui lòng đăng nhập để tham gia thử thách")
        return try {
            val docRef = firestore.collection(CHALLENGES_COLLECTION).document(challengeId)
            var isNowJoined = false
            firestore.runTransaction { tx ->
                val snap = tx.get(docRef)
                val participants = (snap.get("participantUids") as? List<*>)?.map { it.toString() } ?: emptyList()
                if (currentUid in participants) {
                    tx.update(docRef, "participantUids", FieldValue.arrayRemove(currentUid))
                    isNowJoined = false
                } else {
                    tx.update(docRef, "participantUids", FieldValue.arrayUnion(currentUid))
                    isNowJoined = true
                }
            }.await()
            Resource.Success(isNowJoined)
        } catch (e: Exception) {
            Log.e(TAG, "toggleJoinChallenge failed: ${e.message}", e)
            Resource.Error(e.message ?: "Lỗi tham gia thử thách")
        }
    }

    override suspend fun toggleJoinClub(clubId: String, currentUid: String): Resource<Boolean> {
        if (currentUid.isEmpty()) return Resource.Error("Vui lòng đăng nhập để tham gia CLB")
        return try {
            val docRef = firestore.collection(CLUBS_COLLECTION).document(clubId)
            var isNowJoined = false
            firestore.runTransaction { tx ->
                val snap = tx.get(docRef)
                val members = (snap.get("memberUids") as? List<*>)?.map { it.toString() } ?: emptyList()
                if (currentUid in members) {
                    tx.update(docRef, "memberUids", FieldValue.arrayRemove(currentUid))
                    isNowJoined = false
                } else {
                    tx.update(docRef, "memberUids", FieldValue.arrayUnion(currentUid))
                    isNowJoined = true
                }
            }.await()
            Resource.Success(isNowJoined)
        } catch (e: Exception) {
            Log.e(TAG, "toggleJoinClub failed: ${e.message}", e)
            Resource.Error(e.message ?: "Lỗi tham gia câu lạc bộ")
        }
    }

    override suspend fun toggleEventGoing(eventId: String, currentUid: String): Resource<Boolean> {
        if (currentUid.isEmpty()) return Resource.Error("Vui lòng đăng nhập để đăng ký sự kiện")
        return try {
            val docRef = firestore.collection(EVENTS_COLLECTION).document(eventId)
            var isNowGoing = false
            firestore.runTransaction { tx ->
                val snap = tx.get(docRef)
                val attendees = (snap.get("attendeeUids") as? List<*>)?.map { it.toString() } ?: emptyList()
                if (currentUid in attendees) {
                    tx.update(docRef, "attendeeUids", FieldValue.arrayRemove(currentUid))
                    isNowGoing = false
                } else {
                    tx.update(docRef, "attendeeUids", FieldValue.arrayUnion(currentUid))
                    isNowGoing = true
                }
            }.await()
            Resource.Success(isNowGoing)
        } catch (e: Exception) {
            Log.e(TAG, "toggleEventGoing failed: ${e.message}", e)
            Resource.Error(e.message ?: "Lỗi cập nhật sự kiện")
        }
    }

    override fun observeClubPosts(clubId: String): Flow<List<ClubPost>> = callbackFlow {
        val listener = firestore.collection(CLUBS_COLLECTION).document(clubId)
            .collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val posts = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        ClubPost(
                            id = doc.id,
                            authorUid = doc.getString("authorUid") ?: "",
                            authorName = doc.getString("authorName") ?: "Thành viên",
                            authorAvatar = doc.getString("authorAvatar") ?: "",
                            isLeader = doc.getBoolean("isLeader") ?: false,
                            isAnnouncement = doc.getBoolean("isAnnouncement") ?: false,
                            timeAgo = formatTimeAgo(ts),
                            content = doc.getString("content") ?: "",
                            distanceKm = doc.getDouble("distanceKm"),
                            durationFormatted = doc.getString("durationFormatted"),
                            pace = doc.getString("pace"),
                            kudosCount = doc.getLong("kudosCount")?.toInt() ?: 0,
                            commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0,
                            timestamp = ts
                        )
                    }.getOrNull()
                }
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun createClub(
        name: String,
        description: String,
        category: String,
        location: String,
        sportType: SportType,
        avatarUrl: String,
        bannerUrl: String,
        leaderUid: String,
        leaderName: String
    ): Resource<String> {
        if (leaderUid.isEmpty()) return Resource.Error("Vui lòng đăng nhập để tạo nhóm")
        if (name.isBlank()) return Resource.Error("Tên câu lạc bộ không được để trống")

        return try {
            val docRef = firestore.collection(CLUBS_COLLECTION).document()
            val clubId = docRef.id

            val defaultBanner = bannerUrl.ifBlank {
                "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?auto=format&fit=crop&w=800&q=80"
            }
            val defaultAvatar = avatarUrl.ifBlank {
                "https://images.unsplash.com/photo-1571008887538-b36bb32f4571?auto=format&fit=crop&w=300&q=80"
            }

            val clubData = hashMapOf(
                "name" to name.trim(),
                "description" to description.trim(),
                "category" to category.ifBlank { "Chạy bộ" },
                "location" to location.ifBlank { "Hà Nội, Việt Nam" },
                "sportType" to sportType.name,
                "avatarUrl" to defaultAvatar,
                "bannerUrl" to defaultBanner,
                "leaderUid" to leaderUid,
                "leaderName" to leaderName.ifBlank { "Thành viên" },
                "baseMemberCount" to 0,
                "weeklyDistanceKm" to 0.0,
                "memberUids" to listOf(leaderUid),
                "memberNames" to listOf(leaderName),
                "createdAt" to System.currentTimeMillis(),
                "recentAuthor" to leaderName,
                "recentPostContent" to "Chào mừng mọi người đến với CLB $name! Hãy cùng nhau rèn luyện thể thao nhé!"
            )

            docRef.set(clubData).await()

            val postRef = docRef.collection("posts").document()
            val welcomePost = hashMapOf(
                "authorUid" to leaderUid,
                "authorName" to leaderName,
                "authorAvatar" to defaultAvatar,
                "isLeader" to true,
                "isAnnouncement" to true,
                "content" to "Chào mừng các thành viên đến với câu lạc bộ $name! Bảng tin này sẽ là nơi cập nhật lịch chạy, các sự kiện và thử thách của nhóm. Mọi người cùng giao lưu và chia sẻ hành trình chạy bộ của mình nhé!",
                "kudosCount" to 1,
                "commentsCount" to 0,
                "timestamp" to System.currentTimeMillis()
            )
            postRef.set(welcomePost).await()

            Resource.Success(clubId)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi tạo club: ${e.message}", e)
            Resource.Error(e.message ?: "Không thể tạo câu lạc bộ")
        }
    }

    override suspend fun postToClub(
        clubId: String,
        content: String,
        authorUid: String,
        authorName: String,
        authorAvatar: String,
        isLeader: Boolean,
        isAnnouncement: Boolean,
        distanceKm: Double?,
        durationFormatted: String?,
        pace: String?
    ): Resource<Boolean> {
        if (authorUid.isEmpty()) return Resource.Error("Vui lòng đăng nhập để đăng bài")
        if (content.isBlank()) return Resource.Error("Nội dung không được để trống")

        return try {
            val clubDoc = firestore.collection(CLUBS_COLLECTION).document(clubId)
            val postRef = clubDoc.collection("posts").document()

            val postMap = hashMapOf<String, Any?>(
                "authorUid" to authorUid,
                "authorName" to authorName,
                "authorAvatar" to authorAvatar,
                "isLeader" to isLeader,
                "isAnnouncement" to isAnnouncement,
                "content" to content.trim(),
                "distanceKm" to distanceKm,
                "durationFormatted" to durationFormatted,
                "pace" to pace,
                "kudosCount" to 0,
                "commentsCount" to 0,
                "timestamp" to System.currentTimeMillis()
            )
            postRef.set(postMap.filterValues { it != null }).await()

            clubDoc.update(
                mapOf(
                    "recentAuthor" to authorName,
                    "recentPostContent" to content.trim()
                )
            ).await()

            Resource.Success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi đăng bài CLB: ${e.message}", e)
            Resource.Error(e.message ?: "Lỗi đăng bài viết")
        }
    }

    override suspend fun createClubChallenge(
        clubId: String,
        clubName: String,
        title: String,
        description: String,
        targetKm: Double,
        daysLeft: Int,
        sportType: SportType,
        badgeName: String,
        bannerUrl: String,
        creatorUid: String
    ): Resource<String> {
        if (creatorUid.isEmpty()) return Resource.Error("Vui lòng đăng nhập")
        if (title.isBlank()) return Resource.Error("Tên thử thách không được để trống")

        return try {
            val challengeRef = firestore.collection(CHALLENGES_COLLECTION).document()
            val challengeId = challengeRef.id

            val defaultBanner = bannerUrl.ifBlank {
                "https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=800&q=80"
            }

            val data = hashMapOf(
                "title" to title.trim(),
                "description" to description.trim(),
                "bannerUrl" to defaultBanner,
                "targetKm" to targetKm,
                "daysLeft" to daysLeft,
                "baseParticipantsCount" to 1,
                "sportType" to sportType.name,
                "badgeName" to badgeName.ifBlank { "Huy hiệu Finisher $title" },
                "participantUids" to listOf(creatorUid),
                "clubId" to clubId,
                "clubName" to clubName,
                "creatorUid" to creatorUid,
                "createdAt" to System.currentTimeMillis()
            )
            challengeRef.set(data).await()

            val clubDoc = firestore.collection(CLUBS_COLLECTION).document(clubId)
            val notifContent = "THỬ THÁCH MỚI CỦA CLB: \"$title\" (${"%.0f".format(targetKm)} km trong $daysLeft ngày)! Các thành viên hãy cùng tham gia tranh tài để nhận ${badgeName.ifBlank { "Huy hiệu đặc biệt" }} nhé!"
            val postRef = clubDoc.collection("posts").document()
            postRef.set(
                hashMapOf(
                    "authorUid" to creatorUid,
                    "authorName" to "Ban Quản Trị",
                    "authorAvatar" to "",
                    "isLeader" to true,
                    "content" to notifContent,
                    "kudosCount" to 1,
                    "commentsCount" to 0,
                    "timestamp" to System.currentTimeMillis()
                )
            ).await()

            clubDoc.update(
                mapOf(
                    "recentAuthor" to "Ban Quản Trị",
                    "recentPostContent" to notifContent
                )
            ).await()

            Resource.Success(challengeId)
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi tạo thử thách CLB: ${e.message}", e)
            Resource.Error(e.message ?: "Lỗi tạo thử thách")
        }
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)
        return when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "$minutes phút trước"
            hours < 24 -> "$hours giờ trước"
            days < 7 -> "$days ngày trước"
            else -> "Tuần trước"
        }
    }

    private fun seedInitialChallenges() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val challenges = listOf(
                    hashMapOf(
                        "title" to "Thử thách 50km Tháng 10",
                        "description" to "Ghi lại 50 km hoạt động chạy bộ từ ngày 1 đến ngày 31 tháng 10 để nhận huy hiệu số danh giá từ Strava.",
                        "bannerUrl" to "https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?auto=format&fit=crop&w=800&q=80",
                        "targetKm" to 50.0,
                        "daysLeft" to 6,
                        "baseParticipantsCount" to 14250,
                        "sportType" to "RUN",
                        "badgeName" to "Huy hiệu Strava 50K Tháng 10",
                        "participantUids" to emptyList<String>()
                    ),
                    hashMapOf(
                        "title" to "Chinh phục 100km Gran Fondo",
                        "description" to "Đạp xe một quãng đường 100 km trong một chuyến đi duy nhất và mở khóa khả năng mua áo thi đấu phiên bản giới hạn.",
                        "bannerUrl" to "https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=800&q=80",
                        "targetKm" to 100.0,
                        "daysLeft" to 12,
                        "baseParticipantsCount" to 8920,
                        "sportType" to "RIDE",
                        "badgeName" to "Huy hiệu Gran Fondo 100K",
                        "participantUids" to emptyList<String>()
                    ),
                    hashMapOf(
                        "title" to "Chinh phục Bán Marathon 21K",
                        "description" to "Chạy 21.1 km trong một buổi chạy để hoàn thành cự ly Half Marathon danh giá.",
                        "bannerUrl" to "https://images.unsplash.com/photo-1502680390469-be75c86b636f?auto=format&fit=crop&w=800&q=80",
                        "targetKm" to 21.1,
                        "daysLeft" to 18,
                        "baseParticipantsCount" to 21540,
                        "sportType" to "RUN",
                        "badgeName" to "Huy hiệu Half Marathon Finisher",
                        "participantUids" to emptyList<String>()
                    )
                )

                challenges.forEachIndexed { index, map ->
                    firestore.collection(CHALLENGES_COLLECTION)
                        .document("challenge_${index + 1}")
                        .set(map, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Lỗi seed challenges: ${e.message}")
            }
        }
    }

    private fun seedInitialClubs() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val clubs = listOf(
                    hashMapOf(
                        "name" to "Hà Nội Runners Club (HRC)",
                        "category" to "Chạy bộ đường trường",
                        "baseMemberCount" to 3420,
                        "weeklyDistanceKm" to 8450.0,
                        "location" to "Hồ Tây & Hoàn Kiếm, Hà Nội",
                        "avatarUrl" to "",
                        "bannerUrl" to "https://images.unsplash.com/photo-1571008887538-b36bb32f4571?auto=format&fit=crop&w=800&q=80",
                        "description" to "Cộng đồng chạy bộ lâu đời và đông đảo nhất thủ đô. Sinh hoạt định kỳ sáng thứ 7 quanh Hồ Tây và Chủ nhật phố đi bộ.",
                        "sportType" to "RUN",
                        "recentAuthor" to "Trần Hoàng Minh (Pacer)",
                        "recentPostContent" to "Cuối tuần này CLB có buổi Long Run 21K đón gió mùa đông bắc. Hẹn cả nhà 5:00 AM tại Vườn hoa Lý Tự Trọng!",
                        "memberUids" to emptyList<String>()
                    ),
                    hashMapOf(
                        "name" to "Sài Gòn Trail & Ultra Runners",
                        "category" to "Chạy địa hình & Trail",
                        "baseMemberCount" to 1890,
                        "weeklyDistanceKm" to 4200.0,
                        "location" to "TP. Hồ Chí Minh & Núi Dinh",
                        "avatarUrl" to "",
                        "bannerUrl" to "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?auto=format&fit=crop&w=800&q=80",
                        "description" to "Dành cho các chiến binh đam mê leo dốc, băng rừng và chinh phục các giải chạy Ultra Trail khắc nghiệt tại Đà Lạt, Mộc Châu, Sa Pa.",
                        "sportType" to "TRAIL_RUN",
                        "recentAuthor" to "Vũ Quang Dũng",
                        "recentPostContent" to "Buổi tập dốc đồi Thứ 7 tại Núi Dinh chuẩn bị cho VMM 70K. Đăng ký xe chung trong nhóm Zalo nhé!",
                        "memberUids" to emptyList<String>()
                    ),
                    hashMapOf(
                        "name" to "Vietnam Cycling & Tri Club",
                        "category" to "Đạp xe & Triathlon",
                        "baseMemberCount" to 2150,
                        "weeklyDistanceKm" to 12800.0,
                        "location" to "Toàn quốc",
                        "avatarUrl" to "",
                        "bannerUrl" to "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?auto=format&fit=crop&w=800&q=80",
                        "description" to "Nơi hội tụ những người yêu thích xe đạp Road, Gravel và 3 môn phối hợp Bơi - Đạp - Chạy.",
                        "sportType" to "RIDE",
                        "recentAuthor" to "Lê Mai Phương",
                        "recentPostContent" to "Cung đường Đại lộ Thăng Long 60km chủ nhật này đã có 35 cua-rơ đăng ký tham gia.",
                        "memberUids" to emptyList<String>()
                    )
                )

                clubs.forEachIndexed { index, map ->
                    firestore.collection(CLUBS_COLLECTION)
                        .document("club_${index + 1}")
                        .set(map, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Lỗi seed clubs: ${e.message}")
            }
        }
    }

    private fun seedInitialEvents() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val events = listOf(
                    hashMapOf(
                        "title" to "Chạy Long Run Hồ Tây 15K - 21K",
                        "clubName" to "Hà Nội Runners Club (HRC)",
                        "clubAvatar" to "",
                        "dateTimeFormatted" to "Thứ Bảy, 31 Th10 • 05:30",
                        "location" to "Vườn hoa Lý Tự Trọng, Tây Hồ, Hà Nội",
                        "distanceKm" to 15.0,
                        "baseParticipantsCount" to 86,
                        "imageUrl" to "https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?auto=format&fit=crop&w=800&q=80",
                        "sportType" to "RUN",
                        "attendeeUids" to emptyList<String>()
                    ),
                    hashMapOf(
                        "title" to "Đạp xe ngắm bình minh Cầu Nhật Tân 30K",
                        "clubName" to "Vietnam Cycling & Tri Club",
                        "clubAvatar" to "",
                        "dateTimeFormatted" to "Chủ Nhật, 01 Th11 • 05:00",
                        "location" to "Chân cầu Nhật Tân, Tây Hồ, Hà Nội",
                        "distanceKm" to 30.0,
                        "baseParticipantsCount" to 42,
                        "imageUrl" to "https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=800&q=80",
                        "sportType" to "RIDE",
                        "attendeeUids" to emptyList<String>()
                    )
                )

                events.forEachIndexed { index, map ->
                    firestore.collection(EVENTS_COLLECTION)
                        .document("event_${index + 1}")
                        .set(map, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Lỗi seed events: ${e.message}")
            }
        }
    }

    private fun getDefaultChallenges(currentUid: String) = listOf(
        Challenge(
            id = "challenge_1",
            title = "Thử thách 50km Tháng 10",
            description = "Ghi lại 50 km hoạt động chạy bộ để nhận huy hiệu số danh giá từ Strava.",
            bannerUrl = "https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?auto=format&fit=crop&w=800&q=80",
            targetKm = 50.0,
            currentKm = 14.5,
            daysLeft = 6,
            participantsCount = 14250,
            isJoined = true,
            sportType = SportType.RUN,
            badgeName = "Huy hiệu Strava 50K Tháng 10"
        ),
        Challenge(
            id = "challenge_2",
            title = "Chinh phục 100km Gran Fondo",
            description = "Đạp xe một quãng đường 100 km trong một chuyến đi duy nhất.",
            bannerUrl = "https://images.unsplash.com/photo-1544197150-b99a580bb7a8?auto=format&fit=crop&w=800&q=80",
            targetKm = 100.0,
            currentKm = 0.0,
            daysLeft = 12,
            participantsCount = 8920,
            isJoined = false,
            sportType = SportType.RIDE,
            badgeName = "Huy hiệu Gran Fondo 100K"
        )
    )

    private fun getDefaultClubs(currentUid: String) = listOf(
        Club(
            id = "club_1",
            name = "Hà Nội Runners Club (HRC)",
            category = "Chạy bộ đường trường",
            memberCount = 3420,
            weeklyDistanceKm = 8450.0,
            location = "Hồ Tây & Hoàn Kiếm, Hà Nội",
            avatarUrl = "",
            bannerUrl = "https://images.unsplash.com/photo-1571008887538-b36bb32f4571?auto=format&fit=crop&w=800&q=80",
            isJoined = true,
            description = "Cộng đồng chạy bộ đông đảo nhất thủ đô. Sinh hoạt định kỳ thứ 7 và CN.",
            sportType = SportType.RUN
        )
    )

    private fun getDefaultEvents(currentUid: String) = listOf(
        CommunityEvent(
            id = "event_1",
            title = "Chạy Long Run Hồ Tây 15K - 21K",
            clubName = "Hà Nội Runners Club (HRC)",
            clubAvatar = "",
            dateTimeFormatted = "Thứ Bảy, 31 Th10 • 05:30",
            location = "Vườn hoa Lý Tự Trọng, Tây Hồ, Hà Nội",
            distanceKm = 15.0,
            participantsCount = 86,
            isGoing = false,
            imageUrl = "https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?auto=format&fit=crop&w=800&q=80",
            sportType = SportType.RUN
        )
    )
}
