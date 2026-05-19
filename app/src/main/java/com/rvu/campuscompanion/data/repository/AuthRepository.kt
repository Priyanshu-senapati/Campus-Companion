package com.rvu.campuscompanion.data.repository

import android.net.Uri
import com.google.firebase.auth.AuthCredential
import com.rvu.campuscompanion.data.model.User
import com.rvu.campuscompanion.data.remote.FirebaseSource
import com.rvu.campuscompanion.utils.Constants
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseSource.auth
    private val firestore = FirebaseSource.firestore
    private val storage = FirebaseSource.storage

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.uid ?: error("Login failed")
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        prn: String,
        branch: String,
        semester: Int,
        photoUri: Uri?
    ): Result<User> = runCatching {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: error("Registration failed")

        val photoUrl = photoUri?.let {
            runCatching { uploadProfilePhoto(uid, it) }.getOrElse { "" }
        } ?: ""
        val user = User(
            uid = uid, name = name, email = email, prn = prn,
            branch = branch, semester = semester, photoUrl = photoUrl
        )
        firestore.collection(Constants.COLL_USERS).document(uid).set(user).await()
        user
    }

    suspend fun loginWithCredential(credential: AuthCredential): Result<User> = runCatching {
        val result = auth.signInWithCredential(credential).await()
        val u = result.user ?: error("Sign-in failed")
        val ref = firestore.collection(Constants.COLL_USERS).document(u.uid)
        val snap = ref.get().await()
        if (!snap.exists()) {
            val user = User(
                uid = u.uid, name = u.displayName ?: "Student",
                email = u.email ?: "", photoUrl = u.photoUrl?.toString() ?: ""
            )
            ref.set(user).await()
            user
        } else {
            snap.toObject(User::class.java) ?: User(uid = u.uid, email = u.email ?: "")
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun uploadProfilePhoto(uid: String, uri: Uri): String {
        val ref = storage.reference.child("avatars/$uid.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun fetchUser(uid: String): User? {
        val snap = firestore.collection(Constants.COLL_USERS).document(uid).get().await()
        return snap.toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {
        firestore.collection(Constants.COLL_USERS).document(user.uid).set(user).await()
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        firestore.collection(Constants.COLL_USERS).document(uid)
            .update("fcmToken", token).await()
    }

    fun signOut() { auth.signOut() }

    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun currentUid(): String? = auth.currentUser?.uid
}
