package com.beast.rideshare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beast.rideshare.Model.Ride
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎉 Ride Confirmed!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("❌ $error", color = Color.Red)
                }
            }

            ride != null -> {
                ride?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("🚗 Driver Info", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("👤 Name: ${it.userName}", fontSize = 16.sp)
                                Text("📞 Phone: ${if (it.phoneNumber.isNotBlank()) it.phoneNumber else "N/A"}")

                                Spacer(modifier = Modifier.height(20.dp))

                                Text("📍 Route Info", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("🟢 Pickup: ${it.from}", fontSize = 16.sp)
                                Text("🔴 Drop: ${it.to}", fontSize = 16.sp)

                                Spacer(modifier = Modifier.height(20.dp))

                                Text("🗓️ Date: ${it.date}", fontSize = 15.sp)
                                Text("⏰ Time: ${it.time}", fontSize = 15.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "✅ Thank you for choosing this ride. Please contact the ride provider directly for further coordination.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}
