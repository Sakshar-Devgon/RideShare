package com.beast.rideshare

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beast.rideshare.Model.Ride
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RideDetailScreen(rideId: String) {
    var ride by remember { mutableStateOf<Ride?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(rideId) {
        FirebaseFirestore.getInstance().collection("rides")
            .document(rideId)
            .get()
            .addOnSuccessListener { doc ->
                val fetchedRide = doc.toObject(Ride::class.java)
                if (fetchedRide != null) {
                    ride = fetchedRide.copy(rideId = doc.id)
                } else {
                    error = "Ride not found"
                }
                loading = false
            }
            .addOnFailureListener {
                error = it.message ?: "Error loading ride"
                loading = false
            }
    }

    when {
        loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("❌ ${error}")
            }
        }

        ride != null -> {
            ride?.let {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("🎉 Ride Confirmed!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Driver Name: ${it.userName}")
                    Text("Driver Phone: ${if (it.phoneNumber.isNotBlank()) it.phoneNumber else "N/A"}")
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Pickup: ${it.from}")
                    Text("Drop: ${it.to}")
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Date: ${it.date}")
                    Text("Time: ${it.time}")
                    Spacer(modifier = Modifier.height(20.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "✅ Thank you for choosing this ride. Please contact the ride provider directly for further coordination.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
