package com.beast.rideshare.ui.Screens.RideScreens.OfferRide

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
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

    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var seats by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val pickupAddress = locationViewModel.pickupAddress
    val dropAddress = locationViewModel.dropAddress

    val scrollState = rememberScrollState()

    val textFieldModifier = Modifier.fillMaxWidth()
    val textFieldShape: Shape = RoundedCornerShape(12.dp)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF8C52FF),
        unfocusedBorderColor = Color.Gray,
        cursorColor = Color(0xFF8C52FF),
        focusedLabelColor = Color(0xFF8C52FF)
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
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Fill Ride Details",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF8C52FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )

            Text("Pickup: ${pickupAddress ?: "Not Selected"}")
            Text("Drop: ${dropAddress ?: "Not Selected"}")

            Button(
                onClick = { navController.navigate(Routes.OSMMap.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8C52FF),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Location on Map")
            }

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (e.g. 01/07/2025)") },
                modifier = textFieldModifier,
                shape = textFieldShape,
                colors = textFieldColors
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time (e.g. 10:00 AM)") },
                modifier = textFieldModifier,
                shape = textFieldShape,
                colors = textFieldColors
            )

            OutlinedTextField(
                value = seats,
                onValueChange = { seats = it },
                label = { Text("Seats Available") },
                modifier = textFieldModifier,
                shape = textFieldShape,
                colors = textFieldColors
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = textFieldModifier,
                shape = textFieldShape,
                colors = textFieldColors
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (e.g. ₹100)") },
                modifier = textFieldModifier,
                shape = textFieldShape,
                colors = textFieldColors
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = textFieldModifier,
                shape = textFieldShape,
                colors = textFieldColors
            )

            Button(
                onClick = {
                    if (user != null && pickupAddress != null && dropAddress != null && phoneNumber.isNotBlank()) {
                        rideViewModel.uploadRide(
                            from = pickupAddress,
                            to = dropAddress,
                            date = date,
                            time = time,
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
                    containerColor = Color(0xFF8C52FF),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Post Ride")
            }

            Spacer(modifier = Modifier.height(16.dp)) // So button isn't touching the bottom edge
        }
    }
}
