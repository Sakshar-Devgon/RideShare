package com.beast.rideshare.ui.Screens.RideScreens.OfferRides

import androidx.lifecycle.ViewModel
import com.beast.rideshare.Model.Ride

import com.google.firebase.firestore.FirebaseFirestore

class RideViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val ridesCollection = firestore.collection("rides")

    // Upload a new ride to Firestore
    fun uploadRide(
        ride: Ride,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        ridesCollection.add(ride)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Unknown error")
            }
    }

    // Get all rides posted by current user
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
}
