package com.beast.rideshare
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.ui.Screens.RideScreens.OfferRides.RideViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun WaitingScreen(
    rideViewModel: RideViewModel,
    navController: NavController
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var userRides by remember { mutableStateOf<List<Ride>>(emptyList()) }

    // ✅ Load user's rides
    LaunchedEffect(userId) {
        if (userId != null) {
            rideViewModel.getUserRides(userId) { rides ->
                userRides = rides
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Ride Status", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (userRides.isEmpty()) {
            Text("You haven't offered any rides yet.")
        } else {
            userRides.forEach { ride ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("From: ${ride.from}")
                        Text("To: ${ride.to}")
                        Spacer(modifier = Modifier.height(4.dp))

                        if (ride.accepted && ride.acceptedByName != null && ride.acceptedByPhone != null) {
                            Text("🎉 Ride accepted!", style = MaterialTheme.typography.bodyLarge)
                            Text("Accepted by: ${ride.acceptedByName}")
                            Text("Phone: ${ride.acceptedByPhone}")
                        } else {
                            Text("⏳ Waiting for a rider to accept...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
