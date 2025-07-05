package com.beast.rideshare.ui.Screens.RideScreens.FindRide

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.Navigation.Routes

@Composable
fun FindRideScreen(
    navController: NavController,
    findRideViewModel: FindRideViewModel // ✅ Use the ViewModel passed from NavHost
) {
    val context = LocalContext.current
    var rides by remember { mutableStateOf<List<Ride>>(emptyList()) }

    LaunchedEffect(Unit) {
        findRideViewModel.fetchAllRides(
            onSuccess = { rides = it },
            onFailure = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        )
    }

    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        items(rides.size) { index ->
            val ride = rides[index]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("From: ${ride.from}")
                    Text("To: ${ride.to}")
                    Text("Date: ${ride.date}")
                    Text("Time: ${ride.time}")
                    Text("Seats: ${ride.seatsAvailable}")
                    Text("Posted by: ${ride.userName}")

                    if (!ride.accepted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            findRideViewModel.acceptRide(
                                rideId = ride.rideId,
                                acceptorName = ride.userName,
                                acceptorPhone = ride.phoneNumber,
                                onSuccess = {
                                    Toast.makeText(context, "Ride Accepted!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Routes.Home.route)
                                },
                                onFailure = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }) {
                            Text("Accept Ride")
                        }
                    } else {
                        Text("✅ Already Accepted")
                    }
                }
            }
        }
    }
}
