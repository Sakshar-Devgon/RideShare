package com.beast.rideshare.ui.Screens.RideScreens.OfferRide

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.beast.rideshare.Navigation.Routes
import com.beast.rideshare.ui.Screens.RideScreens.OfferRides.RideViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun OfferRideScreen(
    navController: NavController,
    rideViewModel: RideViewModel,
    locationViewModel: RideLocationViewModel
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val pickupAddress = locationViewModel.pickupAddress
    val dropAddress = locationViewModel.dropAddress

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Offer a Ride", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Pickup: ${pickupAddress ?: "Not Selected"}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Drop: ${dropAddress ?: "Not Selected"}")
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            navController.navigate(Routes.OSMMap.route)
        }) {
            Text("Select Location on Map")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (e.g. 01/07/2025)") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (e.g. 10:00 AM)") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = seats,
            onValueChange = { seats = it },
            label = { Text("Seats Available") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (Optional)") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (user != null && pickupAddress != null && dropAddress != null && phoneNumber.isNotBlank()) {
                rideViewModel.uploadRide(
                    from = pickupAddress,
                    to = dropAddress,
                    date = date,
                    time = time,
                    seatsAvailable = seats.toIntOrNull() ?: 1,
                    notes = note,
                    phoneNumber = phoneNumber, // ✅ now passed correctly
                    onSuccess = {
                        Toast.makeText(context, "Ride Offered Successfully!", Toast.LENGTH_SHORT).show()
                        locationViewModel.pickupAddress = null
                        locationViewModel.dropAddress = null
                        navController.navigate(Routes.WaitingScreen.route)
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                Toast.makeText(
                    context,
                    "Please fill all required fields (locations and phone number).",
                    Toast.LENGTH_LONG
                ).show()
            }
        }) {
            Text("Post Ride")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                navController.navigate(Routes.WaitingScreen.route)
            }
        ) {
            Text(text = "Go to Waiting Room")
        }
    }
}
