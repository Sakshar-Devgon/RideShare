package com.beast.rideshare.ui.Screens.RideScreens.FindRide

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.beast.rideshare.ui.components.*
import com.beast.rideshare.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindRideScreen(
    navController: NavController,
    findRideViewModel: FindRideViewModel
) {
    val context = LocalContext.current
    var rides by remember { mutableStateOf<List<Ride>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Female Only", "Under ₹200"
    var maxPriceFilter by remember { mutableFloatStateOf(500f) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Bookmarked ride ids
    val bookmarkedRideIds = remember { mutableStateListOf<String>() }

    // Booking Bottom Sheet State
    var selectedRideForBooking by remember { mutableStateOf<Ride?>(null) }
    var acceptorName by remember {
        mutableStateOf(FirebaseAuth.getInstance().currentUser?.displayName ?: "")
    }
    var acceptorPhone by remember { mutableStateOf("") }
    var isBooking by remember { mutableStateOf(false) }

    fun refreshRides() {
        isLoading = true
        findRideViewModel.fetchAllRides(
            onSuccess = {
                rides = it
                isLoading = false
            },
            onFailure = {
                isLoading = false
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        )
    }

    LaunchedEffect(Unit) {
        refreshRides()
    }

    val filteredRides = remember(rides, searchQuery, selectedFilter, maxPriceFilter) {
        rides.filter { ride ->
            val matchesSearch = searchQuery.isBlank() ||
                    ride.from.contains(searchQuery, ignoreCase = true) ||
                    ride.to.contains(searchQuery, ignoreCase = true) ||
                    ride.userName.contains(searchQuery, ignoreCase = true)

            val rideAmount = ride.amount.toIntOrNull() ?: 150
            val matchesMaxPrice = rideAmount <= maxPriceFilter

            val matchesFilter = when (selectedFilter) {
                "Female Only" -> ride.rideType == "femaleOnly"
                "Under ₹200" -> rideAmount <= 200
                else -> true
            }

            matchesSearch && matchesFilter && matchesMaxPrice
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            OrichuBottomNavBar(
                currentRoute = "find",
                onNavigate = { route ->
                    when (route) {
                        "home" -> navController.navigate(Routes.Home.route)
                        "find" -> Unit
                        "offer1" -> navController.navigate(Routes.OfferRide.route)
                        "activity" -> navController.navigate(Routes.WaitingScreen.route)
                        "profile" -> navController.navigate(Routes.Profile.route)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // .h Header: Back button, Title & Subtitle, Filter button (.ib with sliders icon)
            OrichuHeader(
                title = "Find a Ride",
                subtitle = "${filteredRides.size} journey${if (filteredRides.size == 1) "" else "s"} available",
                onBackClick = { navController.popBackStack() },
                trailingContent = {
                    OrichuIconButton(
                        icon = Icons.Default.Tune,
                        onClick = { showFilterSheet = true },
                        contentDescription = "Filters"
                    )
                }
            )

            // Search Input (.f with search icon)
            OrichuTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search by origin, destination or driver...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Chips Row (.chips: All, Female Only, Under ₹200)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filterOptions = listOf("All", "Female Only", "Under ₹200")
                items(filterOptions) { filter ->
                    OrichuChip(
                        text = filter,
                        selected = selectedFilter == filter,
                        onClick = {
                            selectedFilter = filter
                        }
                    )
                }
            }

            // Ride List or Loading / Empty States
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrichuPrimary)
                    }
                }

                filteredRides.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Text(
                            text = "No rides match your search or filters.",
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
                        items(filteredRides, key = { it.rideId.ifBlank { it.userId + it.date + it.time } }) { ride ->
                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                            val isReservedByMe = (ride.accepted && ride.acceptedBy == currentUserId)
                            val isBookmarked = bookmarkedRideIds.contains(ride.rideId)

                            OrichuCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                contentPadding = PaddingValues(18.dp)
                            ) {
                                // Driver Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OrichuAvatar(
                                        initial = ride.userName.ifBlank { "S" },
                                        size = 40.dp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ride.userName.ifBlank { "Driver" },
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = "Verified Driver · ★ 4.9",
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }

                                    // Bookmark Icon Button
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) OrichuPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clickable {
                                                if (isBookmarked) bookmarkedRideIds.remove(ride.rideId)
                                                else bookmarkedRideIds.add(ride.rideId)
                                            }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Route Timeline (.rt)
                                OrichuRouteTimeline(
                                    origin = ride.from,
                                    destination = ride.to
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Footer details: Date/Time + Seats (.ft)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                                Spacer(modifier = Modifier.height(12.dp))

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
                                            text = "${ride.seatsAvailable} seats",
                                            style = TextStyle(
                                                fontSize = 12.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Price per seat Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Price per seat",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )

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

                                // Action Button: Reserve Seat or Seat Reserved
                                if (isReservedByMe) {
                                    OrichuButton(
                                        text = "Seat Reserved",
                                        leadingIcon = Icons.Default.Check,
                                        onClick = {
                                            navController.navigate(Routes.RideDetail.createRoute(ride.rideId))
                                        },
                                        style = OrichuButtonStyle.SOFT
                                    )
                                } else {
                                    OrichuButton(
                                        text = "Reserve Seat",
                                        onClick = { selectedRideForBooking = ride }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Filters Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
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
                    text = "Filters",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                Text(
                    text = "Maximum price per seat: ₹${maxPriceFilter.toInt()}",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = maxPriceFilter,
                    onValueChange = { maxPriceFilter = it },
                    valueRange = 50f..500f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = OrichuPrimary,
                        activeTrackColor = OrichuPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                OrichuButton(
                    text = "Apply filters",
                    onClick = { showFilterSheet = false }
                )
            }
        }
    }

    // Confirm Reservation Bottom Sheet
    selectedRideForBooking?.let { ride ->
        ModalBottomSheet(
            onDismissRequest = { selectedRideForBooking = null },
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
                    text = "Confirm reservation",
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // Recap Card
                OrichuCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1 seat · ${ride.date}, ${ride.time}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${ride.from} to ${ride.to}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "₹${ride.amount.ifBlank { "150" }}",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrichuPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Passenger Name & Phone inputs
                OrichuTextField(
                    value = acceptorName,
                    onValueChange = { acceptorName = it },
                    placeholder = "Your Full Name",
                    leadingIcon = Icons.Default.Person
                )

                Spacer(modifier = Modifier.height(10.dp))

                OrichuTextField(
                    value = acceptorPhone,
                    onValueChange = { acceptorPhone = it },
                    placeholder = "Your Phone Number",
                    leadingIcon = Icons.Default.Phone
                )

                Spacer(modifier = Modifier.height(16.dp))

                OrichuButton(
                    text = "Pay & reserve",
                    loading = isBooking,
                    onClick = {
                        if (acceptorName.isNotBlank() && acceptorPhone.isNotBlank()) {
                            isBooking = true
                            findRideViewModel.acceptRide(
                                rideId = ride.rideId,
                                acceptorName = acceptorName.trim(),
                                acceptorPhone = acceptorPhone.trim(),
                                onSuccess = {
                                    isBooking = false
                                    val bookedId = ride.rideId
                                    selectedRideForBooking = null
                                    Toast.makeText(context, "Seat reserved with ${ride.userName}!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Routes.RideDetail.createRoute(bookedId))
                                },
                                onFailure = { err ->
                                    isBooking = false
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Please enter your name and contact phone number.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}
