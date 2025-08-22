package com.beast.rideshare.ui.Screens.RideScreens.FindRide

import androidx.lifecycle.ViewModel
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindRideViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val ridesCollection = db.collection("rides")
    private val usersCollection = db.collection("users")

    // ✅ Fetch all available rides with gender-based filtering
    fun fetchAllRides(
        onSuccess: (List<Ride>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onFailure("User not logged in.")
            return
        }

        // First, get current user's profile to determine their gender
        usersCollection.document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val userProfile = userDoc.toObject(User::class.java)
                val userGender = userProfile?.gender ?: ""

                // Fetch all rides
                ridesCollection.get()
                    .addOnSuccessListener { snapshot ->
                        val allRides = snapshot.documents.mapNotNull { doc ->
                            val ride = doc.toObject(Ride::class.java)
                            ride?.copy(rideId = doc.id)
                        }

                        // Filter rides based on user's gender and ride type
                        val filteredRides = allRides.filter { ride ->
                            when (ride.rideType) {
                                "femaleOnly" -> userGender == "female" // Only show to female users
                                "normal" -> true // Show to everyone
                                else -> true // Default: show to everyone (for backward compatibility)
                            }
                        }

                        onSuccess(filteredRides)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Error fetching rides")
                    }
            }
            .addOnFailureListener { e ->
                onFailure("Error loading user profile: ${e.message}")
            }
    }

    // ✅ Accept a ride and correctly store acceptor info
    fun acceptRide(
        rideId: String,
        acceptorName: String,
        acceptorPhone: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onFailure("User not logged in.")
            return
        }

        val data = mapOf(
            "accepted" to true,
            "acceptedBy" to currentUser.uid,
            "acceptedByName" to acceptorName,
            "acceptedByPhone" to acceptorPhone
        )

        ridesCollection.document(rideId)
            .update(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to accept ride")
            }
    }

    // Optional utility function
    fun currentUserPhoneNumber(): String {
        return FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "Not Provided"
    }
}
