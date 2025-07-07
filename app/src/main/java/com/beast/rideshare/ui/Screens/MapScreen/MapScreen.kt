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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            CircularProgressIndicator()
        }
        return
    }

    val userGeoPoint = GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude)
    rideLocationViewModel.pickupLocation = userGeoPoint
    rideLocationViewModel.pickupAddress = getAddressFromLocation(context, currentLocation!!)

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

            // Search Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(8.dp)
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
                                val geo = GeoPoint(result.latitude, result.longitude)
                                handleDropTap(geo, context, mapView!!, userGeoPoint, rideLocationViewModel) {
                                    dropAddress = it
                                }
                            } ?: Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF))
                    ) {
                        Text("Search")
                    }
                }
            }

            // Drop Address & Confirm Button
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
            ?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
    } catch (e: Exception) {
        "Unable to fetch address"
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
                    color = android.graphics.Color.BLUE
                    width = 6f
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
            ?.firstOrNull()?.getAddressLine(0) ?: "Unknown Address"
    } catch (e: Exception) {
        "Unknown Address"
    }
}
