package com.beast.rideshare.ui.Screens.RideScreens.OfferRide

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.beast.rideshare.Navigation.Routes
import com.beast.rideshare.ui.Screens.RideScreens.OfferRides.RideViewModel
import com.beast.rideshare.ui.components.*
import com.beast.rideshare.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferRideScreen(
    navController: NavController,
    rideViewModel: RideViewModel,
    locationViewModel: RideLocationViewModel
) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    // 2-step navigation state: 1 = schedule & route, 2 = capacity, price & publish
    var currentStep by remember { mutableIntStateOf(1) }

    val todayFormatted = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
    val tomorrowFormatted = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
    }

    var date by remember { mutableStateOf(todayFormatted) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val displayDateLabel = remember(date) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsed = sdf.parse(date)
            if (parsed != null) {
                when (date) {
                    todayFormatted -> "Today, " + SimpleDateFormat("d MMM", Locale.getDefault()).format(parsed)
                    tomorrowFormatted -> "Tomorrow, " + SimpleDateFormat("d MMM", Locale.getDefault()).format(parsed)
                    else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(parsed)
                }
            } else {
                date
            }
        } catch (e: Exception) {
            date
        }
    }

    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    var isAM by remember { mutableStateOf(true) }

    val formattedTime = remember(selectedHour, selectedMinute, isAM) {
        val hourDisplay = if (selectedHour == 0) 12 else selectedHour
        String.format("%02d:%02d %s", hourDisplay, selectedMinute, if (isAM) "AM" else "PM")
    }

    var seats by remember { mutableIntStateOf(2) }
    var amount by remember { mutableStateOf("150") }
    var phoneNumber by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val pickupAddress = locationViewModel.pickupAddress
    val dropAddress = locationViewModel.dropAddress

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                    } else {
                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                    }
                },
                label = "offerRideStep"
            ) { step ->
                if (step == 1) {
                    // ==========================================
                    // STEP 1: Route & Schedule
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        OrichuHeader(
                            title = "Offer a Ride",
                            subtitle = "Host a shared journey",
                            onBackClick = { navController.popBackStack() }
                        )

                        // Map Preview Card (.map)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            // SVG Styled Map Canvas
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Road lines
                                val roadColor = Color.White.copy(alpha = 0.7f)
                                drawLine(roadColor, Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 18f)
                                drawLine(roadColor, Offset(0f, h * 0.72f), Offset(w, h * 0.72f), strokeWidth = 18f)
                                drawLine(roadColor, Offset(w * 0.27f, 0f), Offset(w * 0.27f, h), strokeWidth = 18f)
                                drawLine(roadColor, Offset(w * 0.63f, 0f), Offset(w * 0.63f, h), strokeWidth = 18f)
                                drawLine(roadColor, Offset(w * 0.87f, 0f), Offset(w * 0.87f, h), strokeWidth = 18f)

                                // Route curve
                                val routePath = Path().apply {
                                    moveTo(w * 0.15f, h * 0.24f)
                                    cubicTo(
                                        w * 0.37f, h * 0.24f,
                                        w * 0.30f, h * 0.67f,
                                        w * 0.50f, h * 0.55f
                                    )
                                    cubicTo(
                                        w * 0.75f, h * 0.75f,
                                        w * 0.75f, h * 0.75f,
                                        w * 0.85f, h * 0.75f
                                    )
                                }
                                drawPath(routePath, color = Color(0xFF7C3AED), style = Stroke(width = 8f, cap = StrokeCap.Round))

                                // Origin green pin
                                drawCircle(color = OrichuOk, radius = 16f, center = Offset(w * 0.15f, h * 0.24f))
                                drawCircle(color = Color.White, radius = 8f, center = Offset(w * 0.15f, h * 0.24f))

                                // Destination red pin
                                drawCircle(color = OrichuBad, radius = 16f, center = Offset(w * 0.85f, h * 0.75f))
                                drawCircle(color = Color.White, radius = 8f, center = Offset(w * 0.85f, h * 0.75f))

                                // Center car badge
                                drawCircle(color = OrichuPrimary, radius = 26f, center = Offset(w * 0.50f, h * 0.55f))
                                drawCircle(color = Color.White, radius = 22f, center = Offset(w * 0.50f, h * 0.55f), style = Stroke(width = 4f))
                            }

                            // Show address badges if selected
                            if (pickupAddress != null && dropAddress != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${pickupAddress.take(20)}... → ${dropAddress.take(20)}...",
                                        color = Color.White,
                                        style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }

                        // Select Route on Map Button (.btn.o)
                        OrichuButton(
                            text = if (pickupAddress == null) "Select Route on Map" else "Route Selected: Change Route",
                            onClick = { navController.navigate(Routes.OSMMap.route) },
                            style = OrichuButtonStyle.OUTLINE,
                            leadingIcon = Icons.Default.Map,
                            height = 50.dp
                        )

                        // Section Header: Departure Schedule (.sec)
                        Text(
                            text = "Departure Schedule",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrichuPrimary
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Departure Schedule Card (.card with 2 rows)
                        OrichuCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                        ) {
                            // Trip Date Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = OrichuPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Trip Date",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = displayDateLabel,
                                        style = TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Date",
                                    tint = OrichuPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            // Departure Time Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = OrichuPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Departure Time",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = formattedTime,
                                        style = TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Time",
                                    tint = OrichuPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Next Button: Next: Set Details ->
                        OrichuButton(
                            text = "Next: Set Details",
                            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                            onClick = {
                                if (pickupAddress.isNullOrBlank() || dropAddress.isNullOrBlank()) {
                                    Toast.makeText(context, "Please tap 'Select Route on Map' to choose origin and destination.", Toast.LENGTH_LONG).show()
                                } else {
                                    currentStep = 2
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                } else {
                    // ==========================================
                    // STEP 2: Capacity, Pricing, Contact & Publish
                    // ==========================================
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        OrichuHeader(
                            title = "Offer a Ride",
                            subtitle = "Set capacity, price and publish",
                            onBackClick = { currentStep = 1 }
                        )

                        // Section: Capacity & Pricing (.sec)
                        Text(
                            text = "Capacity & Pricing",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrichuPrimary
                            )
                        )

                        // Available Seats Card
                        OrichuCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Available Seats",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                OrichuStepper(
                                    value = seats,
                                    onValueChange = { seats = it }
                                )
                            }
                        }

                        // Price per Seat (₹) Label
                        Text(
                            text = "Price per Seat (₹)",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Price Input Field
                        OrichuTextField(
                            value = amount,
                            onValueChange = {
                                amount = it.filter { char -> char.isDigit() }
                                errorMessage = null
                            },
                            placeholder = "Price per seat",
                            leadingIcon = Icons.Default.CurrencyRupee,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        // Preset Chips: ₹50, ₹100, ₹150, ₹200 (.chips #pc)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("50", "100", "150", "200").forEach { preset ->
                                OrichuChip(
                                    text = "₹$preset",
                                    selected = amount == preset,
                                    onClick = { amount = preset }
                                )
                            }
                        }

                        // Section: Contact & Notes
                        Text(
                            text = "Contact & Notes",
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrichuPrimary
                            ),
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        // Phone Number
                        OrichuTextField(
                            value = phoneNumber,
                            onValueChange = {
                                phoneNumber = it
                                errorMessage = null
                            },
                            placeholder = "Contact Phone Number",
                            leadingIcon = Icons.Default.Phone,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        // Notes textarea
                        OrichuTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = "Trip Notes / Luggage / Rules (Optional)",
                            singleLine = false,
                            minLines = 3,
                            maxLines = 4
                        )

                        // Error text placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    style = TextStyle(fontSize = 12.sp, color = OrichuBad)
                                )
                            }
                        }

                        // Publish Ride Offer Button
                        OrichuButton(
                            text = "Publish Ride Offer",
                            leadingIcon = Icons.Default.Send,
                            loading = isSubmitting,
                            onClick = {
                                val priceInt = amount.toIntOrNull() ?: 0
                                if (priceInt <= 0) {
                                    errorMessage = "Enter a valid price per seat."
                                    return@OrichuButton
                                }
                                if (phoneNumber.isBlank()) {
                                    errorMessage = "Enter your contact phone number."
                                    return@OrichuButton
                                }
                                if (pickupAddress.isNullOrBlank() || dropAddress.isNullOrBlank()) {
                                    errorMessage = "Please select route on map."
                                    return@OrichuButton
                                }

                                errorMessage = null
                                isSubmitting = true

                                rideViewModel.uploadRide(
                                    from = pickupAddress,
                                    to = dropAddress,
                                    date = date,
                                    time = formattedTime,
                                    seatsAvailable = seats,
                                    notes = note,
                                    phoneNumber = phoneNumber,
                                    amount = amount,
                                    onSuccess = {
                                        isSubmitting = false
                                        Toast.makeText(context, "Ride offer published!", Toast.LENGTH_SHORT).show()
                                        locationViewModel.pickupAddress = null
                                        locationViewModel.dropAddress = null
                                        navController.navigate(Routes.WaitingScreen.route)
                                    },
                                    onFailure = { err ->
                                        isSubmitting = false
                                        errorMessage = err
                                        Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = OrichuPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = OrichuPrimary,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = OrichuPrimary
                )
            )
        }
    }

    // Departure Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = "Select Departure Time",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Hour selector chips
                    Column {
                        Text("Hour", style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items((1..12).toList()) { hour ->
                                OrichuChip(
                                    text = hour.toString(),
                                    selected = selectedHour == hour,
                                    onClick = { selectedHour = hour }
                                )
                            }
                        }
                    }

                    // Minute selector chips
                    Column {
                        Text("Minute", style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0, 15, 30, 45).forEach { min ->
                                OrichuChip(
                                    text = String.format("%02d", min),
                                    selected = selectedMinute == min,
                                    onClick = { selectedMinute = min }
                                )
                            }
                        }
                    }

                    // AM / PM Selector
                    Column {
                        Text("Period", style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OrichuChip(
                                text = "AM",
                                selected = isAM,
                                onClick = { isAM = true }
                            )
                            OrichuChip(
                                text = "PM",
                                selected = !isAM,
                                onClick = { isAM = false }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Done", color = OrichuPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
