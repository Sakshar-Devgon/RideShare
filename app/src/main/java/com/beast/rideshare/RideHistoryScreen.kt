package com.beast.rideshare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beast.rideshare.Model.Ride
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen() {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var rideHistory by remember { mutableStateOf<List<Ride>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val allUserRides = mutableListOf<Ride>()
                var completedQueries = 0
                val totalQueries = 2

                fun checkCompletion() {
                    completedQueries++
                    if (completedQueries == totalQueries) {
                        try {
                            // Remove duplicates and sort
                            val uniqueRides = allUserRides.distinctBy { it.rideId }
                            val sorted = uniqueRides.sortedByDescending {
                                try {
                                    "${it.date} ${it.time}"
                                } catch (e: Exception) {
                                    ""
                                }
                            }
                            rideHistory = sorted
                            loading = false
                        } catch (e: Exception) {
                            error = "Error processing ride history: ${e.message}"
                            loading = false
                        }
                    }
                }

                // Query 1: Rides created by the user
                db.collection("rideHistory")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        try {
                            val userCreatedRides = snapshot.toObjects(Ride::class.java)
                            allUserRides.addAll(userCreatedRides)
                            checkCompletion()
                        } catch (e: Exception) {
                            error = "Error processing created rides: ${e.message}"
                            loading = false
                        }
                    }
                    .addOnFailureListener { exception ->
                        error = "Failed to load created rides: ${exception.message}"
                        loading = false
                    }

                // Query 2: Rides accepted by the user
                db.collection("rideHistory")
                    .whereEqualTo("acceptedBy", currentUserId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        try {
                            val userAcceptedRides = snapshot.toObjects(Ride::class.java)
                            allUserRides.addAll(userAcceptedRides)
                            checkCompletion()
                        } catch (e: Exception) {
                            error = "Error processing accepted rides: ${e.message}"
                            loading = false
                        }
                    }
                    .addOnFailureListener { exception ->
                        error = "Failed to load accepted rides: ${exception.message}"
                        loading = false
                    }

            } catch (e: Exception) {
                error = "Error accessing database: ${e.message}"
                loading = false
            }
        } else {
            error = "User not logged in"
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📜 Ride History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "😕 Error loading ride history",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = error ?: "Unknown error",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (rideHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "📜 No ride history yet",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Complete some rides to see your history here!",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "💡 Tip: Accept or offer rides, then mark them as completed",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(rideHistory) { ride ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(4.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "🧍‍♂️ Offered by: ${ride.offeredByName}",
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "🙋‍♀️ Accepted by: ${ride.acceptedByName}",
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "📍 From",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = ride.from,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "🏁 To",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = ride.to,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "📅 Date: ${ride.date}",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "⏰ Time: ${ride.time}",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (ride.amount.isNotBlank()) {
                                        Text(
                                            text = "💰 Amount: ₹${ride.amount}",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
