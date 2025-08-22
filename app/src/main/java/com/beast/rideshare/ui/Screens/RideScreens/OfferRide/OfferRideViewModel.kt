package com.beast.rideshare.ui.Screens.RideScreens.OfferRides

import androidx.lifecycle.ViewModel
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RideViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val ridesCollection = firestore.collection("rides")
    private val historyCollection = firestore.collection("rideHistory")
    private val usersCollection = firestore.collection("users")

    // ✅ Upload ride with full user info including entered phone number and amount
    fun uploadRide(
        from: String,
        to: String,
        date: String,
        time: String,
        seatsAvailable: Int,
        notes: String,
        phoneNumber: String,
        amount: String, // ✅ NEW: Accept ride amount from UI
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onFailure("User not logged in.")
            return
        }

        // First, get user profile to determine ride type
        usersCollection.document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val userProfile = document.toObject(User::class.java)
                val rideType = if (userProfile?.femaleMode == true && userProfile.gender == "female") {
                    "femaleOnly"
                } else {
                    "normal"
                }

                val rideId = ridesCollection.document().id

                val ride = Ride(
                    userId = currentUser.uid,
                    userName = currentUser.displayName ?: currentUser.email ?: "Unknown User",
                    phoneNumber = phoneNumber,
                    from = from,
                    to = to,
                    date = date,
                    time = time,
                    seatsAvailable = seatsAvailable,
                    notes = notes,
                    amount = amount, // ✅ NEW: Save amount in Ride
                    rideType = rideType, // ✅ NEW: Set ride type based on user profile
                    rideId = rideId,
                    accepted = false,
                    acceptedBy = "",
                    acceptedByName = "",
                    acceptedByPhone = "",
                    offeredBy = currentUser.uid,
                    offeredByName = currentUser.displayName ?: currentUser.email ?: "Unknown User"
                )

                ridesCollection.document(rideId)
                    .set(ride)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { exception ->
                        onFailure(exception.message ?: "Unknown error")
                    }
            }
            .addOnFailureListener { exception ->
                onFailure("Failed to load user profile: ${exception.message}")
            }
    }

    // ✅ Get all rides offered by current user (used in Waiting Room)
    fun getUserRides(
        userId: String,
        onResult: (List<Ride>) -> Unit
    ) {
        ridesCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val rides = snapshot.toObjects(Ride::class.java)
                onResult(rides)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // ✅ Complete ride: move to history, delete from active rides
    fun completeRide(
        ride: Ride,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        historyCollection.add(ride)
            .addOnSuccessListener {
                ridesCollection
                    .whereEqualTo("rideId", ride.rideId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val batch = firestore.batch()
                        for (doc in snapshot.documents) {
                            batch.delete(doc.reference)
                        }
                        batch.commit()
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure(it) }
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
}
