package com.beast.rideshare

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.ui.components.*
import com.beast.rideshare.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryScreen() {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var rideHistory by remember { mutableStateOf<List<Ride>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("All") } // "All", "As Driver", "As Passenger"

    fun loadHistory() {
        if (currentUserId == null) {
            error = "User not logged in"
            loading = false
            return
        }

        loading = true
        val db = FirebaseFirestore.getInstance()
        val allUserRides = mutableListOf<Ride>()
        var completedQueries = 0
        val totalQueries = 2

        fun checkCompletion() {
            completedQueries++
            if (completedQueries == totalQueries) {
                val uniqueRides = allUserRides.distinctBy { it.rideId }
                rideHistory = uniqueRides.sortedByDescending { "${it.date} ${it.time}" }
                loading = false
            }
        }

        db.collection("rideHistory")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                allUserRides.addAll(snapshot.toObjects(Ride::class.java))
                checkCompletion()
            }
            .addOnFailureListener {
                error = it.message
                loading = false
            }

        db.collection("rideHistory")
            .whereEqualTo("acceptedBy", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                allUserRides.addAll(snapshot.toObjects(Ride::class.java))
                checkCompletion()
            }
            .addOnFailureListener {
                error = it.message
                loading = false
            }
    }

    LaunchedEffect(currentUserId) {
        loadHistory()
    }

    val filteredRides = remember(rideHistory, selectedTab, currentUserId) {
        when (selectedTab) {
            "As Driver" -> rideHistory.filter { it.userId == currentUserId }
            "As Passenger" -> rideHistory.filter { it.acceptedBy == currentUserId }
            else -> rideHistory
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Header
            OrichuHeader(
                title = "Ride History",
                subtitle = "Your past journeys",
                trailingContent = {
                    OrichuIconButton(
                        icon = Icons.Default.Refresh,
                        onClick = { loadHistory() },
                        contentDescription = "Refresh"
                    )
                }
            )

            // Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("All", "As Driver", "As Passenger")
                items(tabs) { tab ->
                    OrichuChip(
                        text = tab,
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }

            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrichuPrimary)
                    }
                }

                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error ?: "Error", color = OrichuBad)
                    }
                }

                filteredRides.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No rides yet. Reserve or offer a ride to see it here.",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(filteredRides, key = { it.rideId }) { ride ->
                            val isDriver = ride.userId == currentUserId

                            OrichuCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                contentPadding = PaddingValues(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isDriver) "Offered with ${ride.acceptedByName.ifBlank { "Rider" }}" else "Reserved with ${ride.userName.ifBlank { "Driver" }}",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )

                                    OrichuPill(text = "Completed", isGreen = true)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OrichuRouteTimeline(origin = ride.from, destination = ride.to)

                                Spacer(modifier = Modifier.height(12.dp))

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "${ride.date}, ${ride.time}",
                                            style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }

                                    if (ride.amount.isNotBlank()) {
                                        Text(
                                            text = "₹${ride.amount}",
                                            style = TextStyle(
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OrichuPrimary
                                            )
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
