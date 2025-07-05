package com.beast.rideshare.Model
data class Ride(
    val userId: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val from: String = "",
    val to: String = "",
    val date: String = "",
    val time: String = "",
    val seatsAvailable: Int = 1,
    val notes: String = "",

    // For acceptance tracking
    val rideId: String = "", // Firestore document ID
    val accepted: Boolean = false,
    val acceptedByName: String? = null,
    val acceptedByPhone: String? = null
)
