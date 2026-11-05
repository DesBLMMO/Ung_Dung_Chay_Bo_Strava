package com.example.ung_dung_chay_bo.data.repository

import android.util.Log
import com.example.ung_dung_chay_bo.core.utils.Constants
import com.example.ung_dung_chay_bo.core.utils.Resource
import com.example.ung_dung_chay_bo.domain.model.User
import com.example.ung_dung_chay_bo.domain.repository.AuthRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override val currentUser: User?
        get() = firebaseAuth.currentUser?.toUser()

    override fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    override suspend fun loginWithEmail(email: String, password: String): Resource<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user?.toUser() ?: return Resource.Error("Đăng nhập không thành công")

            try {
                val userDoc = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "lastLoginAt" to com.google.firebase.Timestamp.now()
                )
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(user.uid)
                    .set(userDoc, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Lỗi đồng bộ Firestore khi đăng nhập: ${e.message}")
            }

            Resource.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}", e)
            Resource.Error(mapFirebaseError(e), e)
        }
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Resource<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = result.user ?: return Resource.Error("Không thể tạo tài khoản người dùng")

            val trimmedName = displayName.trim().ifEmpty { "Runner" }
            try {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmedName)
                    .build()
                firebaseUser.updateProfile(profileUpdate).await()
            } catch (profileError: Exception) {
                Log.w(TAG, "Không thể cập nhật tên hiển thị: ${profileError.message}")
            }

            try {
                val userMap = hashMapOf(
                    "uid" to firebaseUser.uid,
                    "displayName" to trimmedName,
                    "email" to email.trim(),
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "role" to "Runner",
                    "weeklyDistanceKm" to 0.0,
                    "followersCount" to 0,
                    "followingCount" to 0,
                    "isOnboardingCompleted" to false
                )
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .set(userMap, SetOptions.merge())
                    .await()
            } catch (firestoreError: Exception) {
                Log.w(TAG, "Không thể lưu user profile lên Firestore: ${firestoreError.message}")
            }

            Resource.Success(firebaseUser.toUser().copy(displayName = trimmedName))
        } catch (e: Exception) {
            Log.e(TAG, "Register failed: ${e.message}", e)
            Resource.Error(mapFirebaseError(e), e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user?.toUser() ?: return Resource.Error("Đăng nhập bằng Google thất bại")

            try {
                val userDoc = hashMapOf(
                    "uid" to user.uid,
                    "email" to user.email,
                    "displayName" to user.displayName.ifEmpty { "Runner" },
                    "photoUrl" to (user.photoUrl ?: ""),
                    "lastLoginAt" to com.google.firebase.Timestamp.now()
                )
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(user.uid)
                    .set(userDoc, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Lỗi đồng bộ Google user lên Firestore: ${e.message}")
            }

            Resource.Success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed: ${e.message}", e)
            Resource.Error(mapFirebaseError(e), e)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Logout error: ${e.message}", e)
            Resource.Error(e.localizedMessage ?: "Đăng xuất không thành công")
        }
    }

    private fun FirebaseUser.toUser() = User(
        uid = uid,
        displayName = displayName ?: "",
        email = email ?: "",
        photoUrl = photoUrl?.toString(),
        isEmailVerified = isEmailVerified
    )

    private fun mapFirebaseError(exception: Exception): String {
        val message = exception.message ?: ""
        val errorCode = (exception as? FirebaseAuthException)?.errorCode ?: ""

        return when {
            errorCode == "ERROR_OPERATION_NOT_ALLOWED" ||
            message.contains("operation is not allowed", ignoreCase = true) ||
            message.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ->
                "Lỗi: Bạn chưa bật 'Email/Password' trong Firebase Console > Authentication > Sign-in method!"

            errorCode == "ERROR_EMAIL_ALREADY_IN_USE" ||
            message.contains("email address is already in use", ignoreCase = true) ->
                "Email này đã được đăng ký tài khoản khác. Vui lòng đăng nhập hoặc dùng email mới!"

            errorCode == "ERROR_WEAK_PASSWORD" ||
            message.contains("password is too weak", ignoreCase = true) ->
                "Mật khẩu quá yếu! Vui lòng đặt mật khẩu từ 6 ký tự trở lên."

            errorCode == "ERROR_WRONG_PASSWORD" ||
            errorCode == "ERROR_USER_NOT_FOUND" ||
            errorCode == "ERROR_INVALID_CREDENTIAL" ||
            message.contains("invalid credential", ignoreCase = true) ||
            message.contains("no user record", ignoreCase = true) ||
            message.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ->
                "Email hoặc mật khẩu không chính xác!"

            errorCode == "ERROR_INVALID_EMAIL" ||
            message.contains("badly formatted", ignoreCase = true) ->
                "Địa chỉ email không đúng định dạng!"

            errorCode == "ERROR_TOO_MANY_REQUESTS" ||
            message.contains("too many requests", ignoreCase = true) ->
                "Quá nhiều lần thử thất bại. Vui lòng đợi ít phút trước khi thử lại!"

            message.contains("network", ignoreCase = true) ->
                "Không có kết nối Internet. Vui lòng kiểm tra lại mạng wifi/4G!"

            exception.localizedMessage != null && exception.localizedMessage!!.isNotBlank() ->
                "Lỗi đăng ký: ${exception.localizedMessage}"

            else -> "Thao tác không thành công. Vui lòng kiểm tra lại!"
        }
    }
}
