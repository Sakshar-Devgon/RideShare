package com.beast.rideshare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beast.rideshare.Model.Ride
import com.beast.rideshare.ui.components.*
import com.beast.rideshare.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailScreen(rideId: String) {
    val context = LocalContext.current
    var ride by remember { mutableStateOf<Ride?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(rideId) {
        FirebaseFirestore.getInstance().collection("rides")
            .document(rideId)
            .get()
            .addOnSuccessListener { doc ->
                val fetchedRide = doc.toObject(Ride::class.java)
                if (fetchedRide != null) {
                    ride = fetchedRide.copy(rideId = doc.id)
                } else {
                    error = "Ride not found or completed."
                }
                loading = false
            }
            .addOnFailureListener {
                error = it.message ?: "Error loading ride"
                loading = false
            }
    }

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

            when {
                loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrichuPrimary)
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = OrichuBad,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = error ?: "Unknown error",
                                color = OrichuBad,
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                ride != null -> {
                    val currentRide = ride!!

                    // .h Header: Title "Ride Details", status pill "Active" or "Confirmed"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Ride Details",
                                style = TextStyle(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        OrichuPill(
                            text = if (currentRide.accepted) "Confirmed" else "Active",
                            isGreen = true
                        )
                    }

                    // Map Visual Card (.map data-map)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val roadColor = Color.White.copy(alpha = 0.7f)
                            drawLine(roadColor, Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 18f)
                            drawLine(roadColor, Offset(0f, h * 0.72f), Offset(w, h * 0.72f), strokeWidth = 18f)
                            drawLine(roadColor, Offset(w * 0.27f, 0f), Offset(w * 0.27f, h), strokeWidth = 18f)
                            drawLine(roadColor, Offset(w * 0.63f, 0f), Offset(w * 0.63f, h), strokeWidth = 18f)
                            drawLine(roadColor, Offset(w * 0.87f, 0f), Offset(w * 0.87f, h), strokeWidth = 18f)

                            val routePath = Path().apply {
                                moveTo(w * 0.15f, h * 0.24f)
                                cubicTo(w * 0.37f, h * 0.24f, w * 0.30f, h * 0.67f, w * 0.50f, h * 0.55f)
                                cubicTo(w * 0.75f, h * 0.75f, w * 0.75f, h * 0.75f, w * 0.85f, h * 0.75f)
                            }
                            drawPath(routePath, color = Color(0xFF7C3AED), style = Stroke(width = 8f, cap = StrokeCap.Round))

                            drawCircle(color = OrichuOk, radius = 16f, center = Offset(w * 0.15f, h * 0.24f))
                            drawCircle(color = Color.White, radius = 8f, center = Offset(w * 0.15f, h * 0.24f))

                            drawCircle(color = OrichuBad, radius = 16f, center = Offset(w * 0.85f, h * 0.75f))
                            drawCircle(color = Color.White, radius = 8f, center = Offset(w * 0.85f, h * 0.75f))

                            drawCircle(color = OrichuPrimary, radius = 26f, center = Offset(w * 0.50f, h * 0.55f))
                            drawCircle(color = Color.White, radius = 22f, center = Offset(w * 0.50f, h * 0.55f), style = Stroke(width = 4f))
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Your Offered Ride",
                        style = TextStyle(
                            fontFamily = PoppinsFontFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Route Timeline (.rt)
                    OrichuCard(modifier = Modifier.fillMaxWidth()) {
                        OrichuRouteTimeline(
                            origin = currentRide.from,
                            destination = currentRide.to
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date & Time Card (.card with 2 spans)
                    OrichuCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = OrichuPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = currentRide.date,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = OrichuPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = currentRide.time,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key-Value Grid (.kv with 2 cards)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Seats
                        OrichuCard(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = OrichuPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Seats",
                                        style = TextStyle(
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = currentRide.seatsAvailable.toString(),
                                        style = TextStyle(
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }

                        // Card 2: Price per Seat
                        OrichuCard(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = OrichuPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Price per Seat",
                                        style = TextStyle(
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "₹${currentRide.amount.ifBlank { "150" }}",
                                        style = TextStyle(
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Contact Section if matched
                    if (currentRide.accepted && !currentRide.acceptedByPhone.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OrichuCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Matched Passenger: ${currentRide.acceptedByName}",
                                        style = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = currentRide.acceptedByPhone,
                                        style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                IconButton(onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${currentRide.acceptedByPhone}")
                                    }
                                    context.startActivity(intent)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Call",
                                        tint = OrichuOk
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Share Ride Link Button (.btn.l)
                    OrichuButton(
                        text = "Share Ride Link",
                        leadingIcon = Icons.Default.Share,
                        style = OrichuButtonStyle.SOFT,
                        onClick = {
                            val shareLink = "https://rideshare.app/ride/${currentRide.rideId}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Ride Link", shareLink))
                            Toast.makeText(context, "Ride link copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
