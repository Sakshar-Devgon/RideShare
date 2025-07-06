package com.beast.rideshare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.ui.Screens.RideScreens.OfferRides.RideViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun WaitingScreen(
    rideViewModel: RideViewModel,
    navController: NavController
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var userRides by remember { mutableStateOf<List<Ride>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 🔁 Load user's rides
    LaunchedEffect(userId) {
        userId?.let {
            rideViewModel.getUserRides(it) { rides ->
                userRides = rides
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Ride Status", style = MaterialTheme.typography.headlineSmall)
            }

            if (userRides.isEmpty()) {
                item {
                    Text("You haven't offered any rides yet.")
                }
            } else {
                items(userRides) { ride ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("From: ${ride.from}")
                            Text("To: ${ride.to}")
                            Spacer(modifier = Modifier.height(4.dp))

                            if (ride.accepted && ride.acceptedByName.isNotEmpty() && ride.acceptedByPhone != null) {
                                Text("🎉 Ride accepted!", style = MaterialTheme.typography.bodyLarge)
                                Text("Accepted by: ${ride.acceptedByName}")
                                Text("Phone: ${ride.acceptedByPhone}")

                                // ✅ Only the user who offered the ride can complete it
                                if (ride.userId == userId) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            rideViewModel.completeRide(
                                                ride = ride,
                                                onSuccess = {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("✅ Ride completed and moved to history.")
                                                    }
                                                    // Refresh rides
                                                    rideViewModel.getUserRides(userId!!) { rides ->
                                                        userRides = rides
                                                    }
                                                },
                                                onFailure = {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("❌ Failed: ${it.message}")
                                                    }
                                                }
                                            )
                                        }
                                    ) {
                                        Text("Ride Completed")
                                    }
                                }
                            } else {
                                Text("⏳ Waiting for a rider to accept...", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
