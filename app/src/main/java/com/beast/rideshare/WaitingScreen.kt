package com.beast.rideshare

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.ui.Screens.RideScreens.OfferRides.RideViewModel
import com.beast.rideshare.Navigation.Routes
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

    // ⬅️ Back press navigates to Home
    BackHandler {
        navController.popBackStack()
        navController.navigate(Routes.Home.route)
    }

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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "🚗 Your Ride Status",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (userRides.isEmpty()) {
                item {
                    Text("😕 You haven't offered any rides yet.", fontSize = 16.sp)
                }
            } else {
                items(userRides) { ride ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📍 From:", fontWeight = FontWeight.Medium, color = Color.Gray)
                            Text(ride.from, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("🏁 To:", fontWeight = FontWeight.Medium, color = Color.Gray)
                            Text(ride.to, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(12.dp))

                            if (ride.accepted && ride.acceptedByName.isNotEmpty() && ride.acceptedByPhone != null) {
                                Text("✅ Ride Accepted!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                Text("👤 Accepted by: ${ride.acceptedByName}")
                                Text("📞 Phone: ${ride.acceptedByPhone}")

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
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF))
                                    ) {
                                        Text("🎯 Mark Ride as Completed")
                                    }
                                }
                            } else {
                                Text(
                                    "⏳ Waiting for a rider to accept...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
