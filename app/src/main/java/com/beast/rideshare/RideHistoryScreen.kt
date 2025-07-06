package com.beast.rideshare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beast.rideshare.Model.Ride
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen() {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var rideHistory by remember { mutableStateOf<List<Ride>>(emptyList()) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("rideHistory")
                .get()
                .await()

            val allRides = snapshot.toObjects(Ride::class.java)

            // ✅ Filter rides where current user was either offerer or acceptor
            val filtered = allRides.filter {
                it.userId == currentUserId || it.acceptedBy == currentUserId
            }

            // ✅ Sort by most recent (latest first) — if needed by date/time
            val sorted = filtered.sortedByDescending { it.date + it.time }

            rideHistory = sorted
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ride History") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (rideHistory.isEmpty()) {
                item {
                    Text("No completed rides found.")
                }
            } else {
                items(rideHistory) { ride ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🧍 Ride offered by: ${ride.offeredByName}")
                            Text("✅ Ride accepted by: ${ride.acceptedByName}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("📍 From: ${ride.from}")
                            Text("🏁 To: ${ride.to}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("🕒 Date: ${ride.date}, Time: ${ride.time}")
                        }
                    }
                }
            }
        }
    }
}
