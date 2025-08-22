package com.beast.rideshare.ui.Screens.RideScreens.FindRide

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.Navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindRideScreen(
    navController: NavController,
    findRideViewModel: FindRideViewModel
) {
    val context = LocalContext.current
    var rides by remember { mutableStateOf<List<Ride>>(emptyList()) }

    var acceptorName by remember { mutableStateOf("") }
    var acceptorPhone by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        findRideViewModel.fetchAllRides(
            onSuccess = { rides = it },
            onFailure = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🔍 Available Rides",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (rides.isEmpty()) {
            // 🚫 Show scrollable content even when no rides
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                Text(
                    text = "🚫 No rides available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(rides.size) { index ->
                    val ride = rides[index]

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📍 From: ${ride.from}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Text("🏁 To: ${ride.to}",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📅 Date: ${ride.date}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("⏰ Time: ${ride.time}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("🪑 Seats: ${ride.seatsAvailable}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("👤 Posted by: ${ride.userName}", color = MaterialTheme.colorScheme.onSurfaceVariant)

                            // Show ride type indicator
                            if (ride.rideType == "femaleOnly") {
                                Text(
                                    "👩 Female Only Ride",
                                    color = Color(0xFFE91E63),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            if (ride.amount.isNotBlank()) {
                                Text(
                                    "💰 Amount: ₹${ride.amount}",
                                    color = Color(0xFF8C52FF),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!ride.accepted) {
                                OutlinedTextField(
                                    value = acceptorName,
                                    onValueChange = { acceptorName = it },
                                    label = { Text("Your Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = acceptorPhone,
                                    onValueChange = { acceptorPhone = it },
                                    label = { Text("Your Phone Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (acceptorName.isNotBlank() && acceptorPhone.isNotBlank()) {
                                            findRideViewModel.acceptRide(
                                                rideId = ride.rideId,
                                                acceptorName = acceptorName,
                                                acceptorPhone = acceptorPhone,
                                                onSuccess = {
                                                    navController.navigate(Routes.RideDetail.createRoute(ride.rideId))
                                                },
                                                onFailure = { error ->
                                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            Toast.makeText(context, "Please enter name and phone.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("✅ Accept Ride")
                                }
                            } else {
                                Text(
                                    "✔️ Already Accepted",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
