package com.rvu.campuscompanion.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseSource {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    val currentUserId: String? get() = auth.currentUser?.uid
    val currentUserEmail: String? get() = auth.currentUser?.email
    fun isSignedIn(): Boolean = auth.currentUser != null
}
