package com.beast.rideshare.ui.Screens.RideScreens.OfferRide

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import com.beast.rideshare.Navigation.Routes
import com.beast.rideshare.ui.Screens.RideScreens.OfferRides.RideViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferRideScreen(
    navController: NavController,
    rideViewModel: RideViewModel,
    locationViewModel: RideLocationViewModel
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    // Auto-set current date
    val currentDate = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
    var date by remember { mutableStateOf(currentDate) }
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAM by remember { mutableStateOf(true) }
    var seats by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Format time for display and storage
    val formattedTime = remember(selectedHour, selectedMinute, isAM) {
        val hour12 = if (selectedHour == 0) 12 else selectedHour
        val amPm = if (isAM) "AM" else "PM"
        String.format("%02d:%02d %s", hour12, selectedMinute, amPm)
    }

    val pickupAddress = locationViewModel.pickupAddress
    val dropAddress = locationViewModel.dropAddress

    val scrollState = rememberScrollState()

    val textFieldModifier = Modifier.fillMaxWidth()
    val textFieldShape: Shape = RoundedCornerShape(12.dp)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offer a Ride", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Waiting Room") },
                            onClick = {
                                expanded = false
                                navController.navigate(Routes.WaitingScreen.route)
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Text(
                text = "Create Ride Offer",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // Location Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Route",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(
                        text = "From: ${pickupAddress ?: "Not Selected"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "To: ${dropAddress ?: "Not Selected"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = { navController.navigate(Routes.OSMMap.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Select Route")
                    }
                }
            }

            // Date & Time Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    // Date Picker
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (DD/MM/YYYY)") },
                        modifier = textFieldModifier,
                        shape = textFieldShape,
                        colors = textFieldColors
                    )

                    // Time Section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Selected Time: $formattedTime",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        // Hour Selector
                        Text(
                            text = "Hour",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items((1..12).toList()) { hour ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selectedHour == hour) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { selectedHour = hour },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hour.toString(),
                                        color = if (selectedHour == hour) Color.White
                                               else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Minute Selector
                        Text(
                            text = "Minutes",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf(0, 15, 30, 45)) { minute ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selectedMinute == minute) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { selectedMinute = minute },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%02d", minute),
                                        color = if (selectedMinute == minute) Color.White
                                               else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // AM/PM Selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(true to "AM", false to "PM").forEach { (amValue, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isAM == amValue) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { isAM = amValue },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isAM == amValue) Color.White
                                               else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Ride Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Ride Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = seats,
                            onValueChange = { seats = it },
                            label = { Text("Seats") },
                            modifier = Modifier.weight(1f),
                            shape = textFieldShape,
                            colors = textFieldColors
                        )

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Price (₹)") },
                            modifier = Modifier.weight(1f),
                            shape = textFieldShape,
                            colors = textFieldColors
                        )
                    }

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Contact Number") },
                        modifier = textFieldModifier,
                        shape = textFieldShape,
                        colors = textFieldColors
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Additional Notes (Optional)") },
                        modifier = textFieldModifier,
                        shape = textFieldShape,
                        colors = textFieldColors,
                        maxLines = 3
                    )
                }
            }

            Button(
                onClick = {
                    if (user != null && pickupAddress != null && dropAddress != null && phoneNumber.isNotBlank()) {
                        rideViewModel.uploadRide(
                            from = pickupAddress,
                            to = dropAddress,
                            date = date,
                            time = formattedTime,
                            seatsAvailable = seats.toIntOrNull() ?: 1,
                            notes = note,
                            phoneNumber = phoneNumber,
                            amount = amount,
                            onSuccess = {
                                Toast.makeText(context, "Ride Offered Successfully!", Toast.LENGTH_SHORT).show()
                                locationViewModel.pickupAddress = null
                                locationViewModel.dropAddress = null
                                navController.navigate(Routes.WaitingScreen.route)
                            },
                            onFailure = { error ->
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Please fill all required fields (locations and phone number).",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Create Ride Offer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // So button isn't touching the bottom edge
        }
    }
}
