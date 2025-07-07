package com.beast.rideshare.ui.Screens.MapScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.beast.rideshare.Navigation.Routes
import com.beast.rideshare.R
import com.beast.rideshare.ui.Screens.RideScreens.OfferRide.RideLocationViewModel
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.util.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSMMapScreen(
    navController: NavController,
    rideLocationViewModel: RideLocationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName
            osmdroidBasePath = context.filesDir
            osmdroidTileCache = File(context.cacheDir, "osmdroid")
        }
    }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var dropGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var dropAddress by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location ?: Location("").apply {
                    latitude = 28.6139
                    longitude = 77.2090
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onPause()
            mapView?.onDetach()
        }
    }

    if (!hasLocationPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Location permission required to show map.")
        }
        return
    }

    currentLocation?.let { location ->
        val userGeoPoint = GeoPoint(location.latitude, location.longitude)

        rideLocationViewModel.pickupLocation = userGeoPoint
        rideLocationViewModel.pickupAddress = getAddressFromLocation(context, location)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Drop Location", fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setBuiltInZoomControls(true)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(userGeoPoint)
                            onResume()
                        }.also { map ->
                            mapView = map

                            val startMarker = Marker(map).apply {
                                position = userGeoPoint
                                icon = ContextCompat.getDrawable(ctx, R.drawable.green_marker)
                                title = "You are here"
                            }
                            map.overlays.add(startMarker)

                            var tapConfirmed = false
                            map.setOnTouchListener { _: View, event: MotionEvent ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN -> tapConfirmed = true
                                    MotionEvent.ACTION_MOVE -> tapConfirmed = false
                                    MotionEvent.ACTION_UP -> {
                                        if (tapConfirmed) {
                                            val geoPoint = map.projection.fromPixels(
                                                event.x.toInt(), event.y.toInt()
                                            ) as GeoPoint
                                            dropGeoPoint = geoPoint

                                            val endMarker = Marker(map).apply {
                                                position = geoPoint
                                                icon = ContextCompat.getDrawable(ctx, R.drawable.red_marker)
                                                title = "Drop Location"
                                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            }

                                            map.overlays.removeAll { it is Marker && it.title == "Drop Location" }
                                            map.overlays.add(endMarker)

                                            try {
                                                dropAddress = Geocoder(ctx).getFromLocation(
                                                    geoPoint.latitude, geoPoint.longitude, 1
                                                )?.firstOrNull()?.getAddressLine(0)
                                                    ?: "Address not found"

                                                rideLocationViewModel.dropLocation = geoPoint
                                                rideLocationViewModel.dropAddress = dropAddress
                                            } catch (e: Exception) {
                                                dropAddress = "Unable to fetch address"
                                            }

                                            val client = OkHttpClient()
                                            val url = "https://api.openrouteservice.org/v2/directions/driving-car" +
                                                    "?api_key=5b3ce3597851110001cf62488c7eb6e4851342d2a5fadb85566ac738" +
                                                    "&start=${userGeoPoint.longitude},${userGeoPoint.latitude}" +
                                                    "&end=${geoPoint.longitude},${geoPoint.latitude}"

                                            Thread {
                                                try {
                                                    val response = client.newCall(Request.Builder().url(url).build()).execute()
                                                    response.body?.string()?.let { jsonBody ->
                                                        val coordinates = JSONObject(jsonBody)
                                                            .getJSONArray("features")
                                                            .getJSONObject(0)
                                                            .getJSONObject("geometry")
                                                            .getJSONArray("coordinates")

                                                        val routePoints = (0 until coordinates.length()).map {
                                                            val coord = coordinates.getJSONArray(it)
                                                            GeoPoint(coord.getDouble(1), coord.getDouble(0))
                                                        }

                                                        map.post {
                                                            map.overlays.removeAll { it is Polyline }
                                                            Polyline().apply {
                                                                setPoints(routePoints)
                                                                color = android.graphics.Color.BLUE
                                                                width = 6f
                                                            }.also { map.overlays.add(it) }

                                                            val box = BoundingBox.fromGeoPoints(listOf(userGeoPoint, geoPoint))
                                                            map.zoomToBoundingBox(box, true)
                                                            map.controller.animateTo(geoPoint)
                                                            map.invalidate()
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("ROUTE_ERROR", "Exception: ${e.localizedMessage}")
                                                }
                                            }.start()
                                        }
                                    }
                                }
                                false
                            }
                        }
                    }
                )

                // Search bar (transparent background)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search Drop Location") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF8C52FF),
                                unfocusedBorderColor = Color.LightGray,
                                cursorColor = Color(0xFF8C52FF)
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                Geocoder(context).getFromLocationName(searchQuery, 1)?.firstOrNull()?.let { result ->
                                    dropGeoPoint = GeoPoint(result.latitude, result.longitude)
                                    dropAddress = result.getAddressLine(0)

                                    rideLocationViewModel.dropLocation = dropGeoPoint
                                    rideLocationViewModel.dropAddress = dropAddress

                                    mapView?.controller?.animateTo(dropGeoPoint)

                                    val marker = Marker(mapView).apply {
                                        position = dropGeoPoint
                                        icon = ContextCompat.getDrawable(context, R.drawable.red_marker)
                                        title = "Drop Location"
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    }

                                    mapView?.overlays?.removeAll { it is Marker && it.title == "Drop Location" }
                                    mapView?.overlays?.add(marker)

                                    dropGeoPoint?.let { end ->
                                        val box = BoundingBox.fromGeoPoints(listOf(userGeoPoint, end))
                                        mapView?.zoomToBoundingBox(box, true)
                                    }

                                    mapView?.invalidate()
                                } ?: Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF))
                        ) {
                            Text("Search")
                        }
                    }
                }

                // Drop location info and confirm button
                dropAddress?.let {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Drop Location", color = Color(0xFF8C52FF), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(it, color = Color.DarkGray)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Drop confirmed: $it", Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.OfferRide.route)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF))
                        ) {
                            Text("Confirm Drop Location")
                        }
                    }
                }
            }
        }
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

fun getAddressFromLocation(context: Context, location: Location): String {
    return try {
        Geocoder(context, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
            ?.firstOrNull()?.getAddressLine(0) ?: "Unknown Address"
    } catch (e: Exception) {
        e.printStackTrace()
        "Unknown Address"
    }
}