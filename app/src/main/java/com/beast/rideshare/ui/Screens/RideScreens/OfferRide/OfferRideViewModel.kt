package com.beast.rideshare.ui.Screens.RideScreens.OfferRides

import androidx.lifecycle.ViewModel
import com.beast.rideshare.Model.Ride
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RideViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val ridesCollection = firestore.collection("rides")
    private val historyCollection = firestore.collection("rideHistory")

    // ✅ Upload ride with full user info & defaults for accept tracking
    fun uploadRide(
        from: String,
        to: String,
        date: String,
        time: String,
        seatsAvailable: Int,
        notes: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onFailure("User not logged in.")
            return
        }

        val rideId = ridesCollection.document().id

        val ride = Ride(
            userId = currentUser.uid,
            userName = currentUser.displayName ?: currentUser.email ?: "Unknown User",
            phoneNumber = currentUser.phoneNumber ?: "N/A",
            from = from,
            to = to,
            date = date,
            time = time,
            seatsAvailable = seatsAvailable,
            notes = notes,
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
