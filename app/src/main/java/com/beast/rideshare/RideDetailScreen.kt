package com.beast.rideshare

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beast.rideshare.Model.Ride

@Composable
fun RideDetailScreen(ride: Ride) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text("🎉 Ride Confirmed!", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(20.dp))

        Text("Driver Name: ${ride.userName}")
        Text("Driver Phone: ${ride.phoneNumber}")
        Spacer(modifier = Modifier.height(12.dp))

        Text("Pickup: ${ride.from}")
        Text("Drop: ${ride.to}")
        Spacer(modifier = Modifier.height(12.dp))

        Text("Date: ${ride.date}")
        Text("Time: ${ride.time}")
        Spacer(modifier = Modifier.height(20.dp))

        Divider()

        Spacer(modifier = Modifier.height(20.dp))
        Text("✅ Thank you for choosing this ride. Please contact the ride provider directly for further coordination.",
            style = MaterialTheme.typography.bodyMedium)
    }
}
