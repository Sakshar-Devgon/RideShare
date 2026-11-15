package com.beast.rideshare

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.Navigation.Routes
import com.beast.rideshare.ui.Screens.RideScreens.OfferRides.RideViewModel
import com.beast.rideshare.ui.components.*
import com.beast.rideshare.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingScreen(
    rideViewModel: RideViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var userRides by remember { mutableStateOf<List<Ride>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Bookings sheet state
    var selectedRideForBookings by remember { mutableStateOf<Ride?>(null) }

    BackHandler {
        navController.popBackStack()
        navController.navigate(Routes.Home.route)
    }

    fun loadRides() {
        userId?.let { uid ->
            isLoading = true
            rideViewModel.getUserRides(uid) { rides ->
                userRides = rides
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) {
        loadRides()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            OrichuBottomNavBar(
                currentRoute = "activity",
                onNavigate = { route ->
                    when (route) {
                        "home" -> {
                            navController.popBackStack()
                            navController.navigate(Routes.Home.route)
                        }
                        "find" -> navController.navigate(Routes.FindRide.route)
                        "offer1" -> navController.navigate(Routes.OfferRide.route)
                        "activity" -> Unit
                        "profile" -> navController.navigate(Routes.Profile.route)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // .h Header: Back button, Title & Subtitle, Refresh button
            OrichuHeader(
                title = "Active Ride Offers",
                subtitle = "Manage your hosted rides",
                onBackClick = {
                    navController.popBackStack()
                    navController.navigate(Routes.Home.route)
                },
                trailingContent = {
                    OrichuIconButton(
                        icon = Icons.Default.Refresh,
                        onClick = { loadRides() },
                        contentDescription = "Refresh"
                    )
                }
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrichuPrimary)
                    }
                }

                userRides.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = OrichuPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = "No Active Offers",
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Text(
                                text = "You haven't posted any rides currently. Offer a ride to connect with riders going your way.",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OrichuButton(
                                text = "Offer a Ride",
                                leadingIcon = Icons.Default.Add,
                                onClick = { navController.navigate(Routes.OfferRide.route) }
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(userRides, key = { it.rideId.ifBlank { it.userId + it.date + it.time } }) { ride ->
                            OrichuCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                contentPadding = PaddingValues(18.dp)
                            ) {
                                // Top row: Status pill + Price
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (ride.accepted) {
                                        OrichuPill(
                                            text = "Confirmed",
                                            isGreen = true,
                                            leadingIcon = Icons.Default.Check
                                        )
                                    } else {
                                        OrichuPill(
                                            text = "★ Waiting for Riders",
                                            isGreen = false
                                        )
                                    }

                                    Text(
                                        text = "₹${ride.amount.ifBlank { "150" }}",
                                        style = TextStyle(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OrichuPrimary
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Route Timeline (.rt with Pickup and Drop-off em labels)
                                OrichuRouteTimeline(
                                    origin = ride.from,
                                    destination = ride.to,
                                    originLabel = "Pickup",
                                    destinationLabel = "Drop-off"
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                                Spacer(modifier = Modifier.height(12.dp))

                                // Footer: Date & Time + Seats offered (.ft)
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
                                            style = TextStyle(
                                                fontSize = 12.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Group,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${ride.seatsAvailable} seats offered",
                                            style = TextStyle(
                                                fontSize = 12.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Two Action buttons (.btn.o and .btn.l)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // View Bookings Button (.btn.o)
                                    OrichuButton(
                                        text = "View Bookings (${if (ride.accepted) 1 else 0})",
                                        leadingIcon = Icons.Default.Group,
                                        style = OrichuButtonStyle.OUTLINE,
                                        modifier = Modifier.weight(1f),
                                        height = 48.dp,
                                        onClick = { selectedRideForBookings = ride }
                                    )

                                    // Details Button (.btn.l)
                                    OrichuButton(
                                        text = "Details",
                                        style = OrichuButtonStyle.SOFT,
                                        modifier = Modifier.widthIn(min = 90.dp),
                                        height = 48.dp,
                                        onClick = {
                                            navController.navigate(Routes.RideDetail.createRoute(ride.rideId))
                                        }
                                    )
                                }

                                // If ride is accepted, show complete option
                                if (ride.accepted) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OrichuButton(
                                        text = "Mark Ride as Completed",
                                        leadingIcon = Icons.Default.CheckCircle,
                                        height = 44.dp,
                                        onClick = {
                                            rideViewModel.completeRide(
                                                ride = ride,
                                                onSuccess = {
                                                    loadRides()
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("Ride marked as completed!")
                                                    }
                                                },
                                                onFailure = { err ->
                                                    Toast.makeText(context, "Error: ${err.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // View Bookings Bottom Sheet
    selectedRideForBookings?.let { ride ->
        ModalBottomSheet(
            onDismissRequest = { selectedRideForBookings = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp)
            ) {
                Text(
                    text = "Bookings (${if (ride.accepted) 1 else 0})",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                if (ride.accepted && ride.acceptedByName.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = OrichuPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${ride.acceptedByName} · 1 seat",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (!ride.acceptedByPhone.isNullOrBlank()) {
                                Text(
                                    text = ride.acceptedByPhone,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        OrichuPill(text = "Confirmed", isGreen = true)

                        if (!ride.acceptedByPhone.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${ride.acceptedByPhone}")
                                }
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call",
                                    tint = OrichuOk,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No bookings yet.",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
        }
    }
}
