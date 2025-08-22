package com.beast.rideshare.ui.Screens.Profile

import androidx.lifecycle.ViewModel
import com.beast.rideshare.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val auth = FirebaseAuth.getInstance()

    // Get current user profile
    fun getUserProfile(
        onSuccess: (User) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in.")
            return
        }

        usersCollection.document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        // Create default user profile if doesn't exist
                        val defaultUser = User(
                            userId = currentUser.uid,
                            name = currentUser.displayName ?: "",
                            email = currentUser.email ?: "",
                            phoneNumber = currentUser.phoneNumber ?: "",
                            gender = "",
                            femaleMode = false
                        )
                        onSuccess(defaultUser)
                    }
                } else {
                    // Create default user profile if document doesn't exist
                    val defaultUser = User(
                        userId = currentUser.uid,
                        name = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        phoneNumber = currentUser.phoneNumber ?: "",
                        gender = "",
                        femaleMode = false
                    )
                    onSuccess(defaultUser)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Failed to load profile")
            }
    }

    // Update user profile
    fun updateUserProfile(
        user: User,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in.")
            return
        }

        usersCollection.document(currentUser.uid)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Failed to update profile")
            }
    }

    // Update only gender and female mode
    fun updateGenderAndFemaleMode(
        gender: String,
        femaleMode: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in. Please sign in again.")
            return
        }

        // Debug: Log user info
        println("DEBUG: Updating profile for user: ${currentUser.uid}")
        println("DEBUG: User email: ${currentUser.email}")
        println("DEBUG: Is email verified: ${currentUser.isEmailVerified}")

        // First check if document exists, if not create it
        usersCollection.document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Document exists, update it
                    val updates = mapOf(
                        "gender" to gender,
                        "femaleMode" to femaleMode
                    )

                    usersCollection.document(currentUser.uid)
                        .update(updates)
                        .addOnSuccessListener {
                            println("DEBUG: Profile updated successfully")
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            println("DEBUG: Update failed: ${exception.message}")
                            onFailure("Update failed: ${exception.message}")
                        }
                } else {
                    // Document doesn't exist, create it
                    val newUser = User(
                        userId = currentUser.uid,
                        name = currentUser.displayName ?: "",
                        email = currentUser.email ?: "",
                        phoneNumber = currentUser.phoneNumber ?: "",
                        gender = gender,
                        femaleMode = femaleMode
                    )

                    usersCollection.document(currentUser.uid)
                        .set(newUser)
                        .addOnSuccessListener {
                            println("DEBUG: Profile created successfully")
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            println("DEBUG: Create failed: ${exception.message}")
                            onFailure("Create failed: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                println("DEBUG: Failed to check document: ${exception.message}")
                onFailure("Failed to check user document: ${exception.message}")
            }
    }

    // Create user profile (for new users)
    fun createUserProfile(
        name: String,
        email: String,
        gender: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in.")
            return
        }

        val user = User(
            userId = currentUser.uid,
            name = name,
            email = email,
            phoneNumber = currentUser.phoneNumber ?: "",
            gender = gender,
            femaleMode = false
        )

        usersCollection.document(currentUser.uid)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Failed to create profile")
            }
    }
}
