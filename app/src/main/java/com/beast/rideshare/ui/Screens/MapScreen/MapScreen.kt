package com.beast.rideshare.ui.Screens.MapScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.beast.rideshare.R
import com.beast.rideshare.ui.Screens.RideScreens.OfferRide.RideLocationViewModel
import com.beast.rideshare.ui.components.GlassCard
import com.beast.rideshare.ui.components.ShowcaseButton
import com.beast.rideshare.ui.components.StatusBadge
import com.beast.rideshare.ui.theme.*
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSMMapScreen(
    navController: NavController,
    rideLocationViewModel: RideLocationViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
            osmdroidBasePath = context.filesDir
            osmdroidTileCache = File(context.cacheDir, "osmdroid")
        }
    }

    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var dropAddress by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                currentLocation = it ?: Location("").apply {
                    latitude = 28.6139
                    longitude = 77.2090
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (currentLocation == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val userGeoPoint = GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude)
    rideLocationViewModel.pickupLocation = userGeoPoint
    rideLocationViewModel.pickupAddress = getAddressFromLocation(context, currentLocation!!)

    fun triggerSearch() {
        keyboardController?.hide()
        if (searchQuery.isNotBlank() && mapView != null) {
            try {
                Geocoder(context).getFromLocationName(searchQuery, 1)?.firstOrNull()?.let { result ->
                    val geo = GeoPoint(result.latitude, result.longitude)
                    handleDropTap(geo, context, mapView!!, userGeoPoint, rideLocationViewModel) {
                        dropAddress = it
                    }
                } ?: Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to search location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Full-screen edge-to-edge Map canvas
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Map Canvas View
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(userGeoPoint)
                    onResume()
                }.also { map ->
                    mapView = map

                    map.overlays.add(Marker(map).apply {
                        position = userGeoPoint
                        icon = ContextCompat.getDrawable(ctx, R.drawable.green_marker)
                        title = "You are here"
                    })

                    map.overlays.add(object : MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            handleDropTap(p, ctx, map, userGeoPoint, rideLocationViewModel) { address ->
                                dropAddress = address
                            }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    }) {})
                }
            }
        )

        // 2. Floating Unified Search Pill (Google Maps / Uber style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(14.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.25f))
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        RoundedCornerShape(28.dp)
                    ),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Clean borderless search input field
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search drop destination...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { triggerSearch() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Trailing Action: Clear or Search
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { triggerSearch() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BrandPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Subtle Hint Badge
            StatusBadge(
                text = "Tap anywhere on map to drop pin",
                showBeacon = true,
                beaconColor = BrandPurple,
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                textColor = MaterialTheme.colorScheme.onSurface,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }

        // 3. Re-center FAB
        FloatingActionButton(
            onClick = {
                mapView?.controller?.animateTo(userGeoPoint)
                mapView?.controller?.setZoom(16.0)
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = CircleShape
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location")
        }

        // 4. Drop Location Confirmation Card
        dropAddress?.let { confirmedAddress ->
            GlassCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                elevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(
                        text = "DROP POINT SET",
                        showBeacon = true,
                        beaconColor = EmeraldNeon,
                        backgroundColor = EmeraldGlow,
                        textColor = EmeraldNeon
                    )

                    Text(
                        text = "Tap map to reposition",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = confirmedAddress,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                ShowcaseButton(
                    text = "Confirm Drop Location",
                    icon = Icons.Default.Check,
                    onClick = {
                        Toast.makeText(context, "Drop location confirmed!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

private fun handleDropTap(
    geoPoint: GeoPoint,
    context: Context,
    map: MapView,
    start: GeoPoint,
    rideLocationViewModel: RideLocationViewModel,
    onAddressResolved: (String) -> Unit
) {
    map.overlays.removeAll { it is Marker && it.title == "Drop Location" || it is Polyline }

    val dropMarker = Marker(map).apply {
        position = geoPoint
        title = "Drop Location"
        icon = ContextCompat.getDrawable(context, R.drawable.red_marker)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }
    map.overlays.add(dropMarker)

    val address = try {
        Geocoder(context).getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
            ?.firstOrNull()?.getAddressLine(0) ?: "Address: ${geoPoint.latitude}, ${geoPoint.longitude}"
    } catch (e: Exception) {
        "Address: ${geoPoint.latitude}, ${geoPoint.longitude}"
    }

    rideLocationViewModel.dropLocation = geoPoint
    rideLocationViewModel.dropAddress = address
    onAddressResolved(address)

    drawRoute(map, start, geoPoint)
}

private fun drawRoute(map: MapView, start: GeoPoint, end: GeoPoint) {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val url =
        "https://api.openrouteservice.org/v2/directions/driving-car" +
                "?api_key=5b3ce3597851110001cf62488c7eb6e4851342d2a5fadb85566ac738" +
                "&start=${start.longitude},${start.latitude}" +
                "&end=${end.longitude},${end.latitude}"

    Thread {
        try {
            val response = client.newCall(Request.Builder().url(url).build()).execute()
            val body = response.body?.string()
            val coordinates = JSONObject(body)
                .getJSONArray("features")
                .getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val routePoints = (0 until coordinates.length()).map {
                val coord = coordinates.getJSONArray(it)
                GeoPoint(coord.getDouble(1), coord.getDouble(0))
            }

            Handler(Looper.getMainLooper()).post {
                val polyline = Polyline().apply {
                    setPoints(routePoints)
                    color = android.graphics.Color.parseColor("#7C3AED") // Brand Purple
                    width = 10f
                }
                map.overlays.add(polyline)
                map.zoomToBoundingBox(BoundingBox.fromGeoPoints(listOf(start, end)), true)
                map.invalidate()
            }
        } catch (e: Exception) {
            Log.e("ROUTE_ERROR", "Failed to load route: ${e.localizedMessage}")
        }
    }.start()
}

fun getAddressFromLocation(context: Context, location: Location): String {
    return try {
        Geocoder(context, Locale.getDefault())
            .getFromLocation(location.latitude, location.longitude, 1)
            ?.firstOrNull()?.getAddressLine(0) ?: "Current GPS Location"
    } catch (e: Exception) {
        "Current GPS Location"
    }
}
