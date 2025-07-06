package com.beast.rideshare.ui.Screens.RideScreens.FindRide

import androidx.lifecycle.ViewModel
import com.beast.rideshare.Model.Ride
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindRideViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val ridesCollection = db.collection("rides")

    // ✅ Fetch all available rides
    fun fetchAllRides(
        onSuccess: (List<Ride>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        ridesCollection.get()
            .addOnSuccessListener { snapshot ->
                val rides = snapshot.documents.mapNotNull { doc ->
                    val ride = doc.toObject(Ride::class.java)
                    ride?.copy(rideId = doc.id)
                }
                onSuccess(rides)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Error fetching rides")
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
