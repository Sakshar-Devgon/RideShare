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

            val filtered = allRides.filter {
                it.userId == currentUserId || it.acceptedBy == currentUserId
            }

            val sorted = filtered.sortedByDescending { it.date + it.time }
            rideHistory = sorted
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📜 Ride History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
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
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("😕 No completed rides found.", fontSize = 16.sp, color = Color.Gray)
                    }
                }
            } else {
                items(rideHistory) { ride ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                        elevation = CardDefaults.cardElevation(5.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🧍‍♂️ Offered by: ${ride.offeredByName}", fontWeight = FontWeight.Medium)
                            Text("🙋‍♀️ Accepted by: ${ride.acceptedByName}", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("📍 From", fontSize = 13.sp, color = Color.Gray)
                            Text(ride.from, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("🏁 To", fontSize = 13.sp, color = Color.Gray)
                            Text(ride.to, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("📅 Date: ${ride.date}")
                            Text("⏰ Time: ${ride.time}")
                        }
                    }
                }
            }
        }
    }
}
