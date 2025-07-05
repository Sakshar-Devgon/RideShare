package com.beast.rideshare.ui.Screens.MapScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
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

@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSMMapScreen(
    navController: NavController,
    rideLocationViewModel: RideLocationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val config = Configuration.getInstance()
        config.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        config.userAgentValue = context.packageName
        config.osmdroidBasePath = context.filesDir
        config.osmdroidTileCache = File(context.cacheDir, "osmdroid")
    }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var dropGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var dropAddress by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location ?: Location("").apply {
                    latitude = 28.6139  // Delhi fallback
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

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            !hasLocationPermission -> Text("Location permission required to show map.")
            currentLocation == null -> CircularProgressIndicator()
            else -> {
                val userGeoPoint = GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude)
                rideLocationViewModel.pickupLocation = userGeoPoint
                rideLocationViewModel.pickupAddress = "Your Location"

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Drop Location") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val results = geocoder.getFromLocationName(searchQuery, 1)
                            val result = results?.firstOrNull()
                            if (result != null) {
                                val searchedPoint = GeoPoint(result.latitude, result.longitude)
                                dropGeoPoint = searchedPoint
                                dropAddress = result.getAddressLine(0)

                                rideLocationViewModel.dropLocation = searchedPoint
                                rideLocationViewModel.dropAddress = dropAddress

                                mapView?.controller?.setCenter(searchedPoint)

                                val marker = Marker(mapView).apply {
                                    position = searchedPoint
                                    icon = ContextCompat.getDrawable(context, R.drawable.red_marker)
                                    title = "Drop Location"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                mapView?.overlays?.removeAll { it is Marker && it.title == "Drop Location" }
                                mapView?.overlays?.add(marker)
                                mapView?.invalidate()
                            } else {
                                Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Search")
                        }
                    }

                    AndroidView(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        factory = { ctx ->
                            val map = MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(15.0)
                                controller.setCenter(userGeoPoint)
                                onResume()
                            }

                            mapView = map

                            val startMarker = Marker(map).apply {
                                position = userGeoPoint
                                icon = ContextCompat.getDrawable(ctx, R.drawable.green_marker)
                                title = "You are here"
                            }
                            map.overlays.add(startMarker)

                            map.setOnTouchListener { _, event ->
                                if (event.action == MotionEvent.ACTION_UP) {
                                    val proj = map.projection
                                    val geoPoint = proj.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                                    dropGeoPoint = geoPoint

                                    val endMarker = Marker(map).apply {
                                        icon = ContextCompat.getDrawable(ctx, R.drawable.red_marker)
                                        title = "Drop Location"
                                        position = geoPoint
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    }

                                    map.overlays.removeAll { it is Marker && it.title == "Drop Location" }
                                    map.overlays.add(endMarker)

                                    val geocoder = Geocoder(ctx, Locale.getDefault())
                                    try {
                                        val addressList = geocoder.getFromLocation(
                                            geoPoint.latitude,
                                            geoPoint.longitude,
                                            1
                                        )
                                        dropAddress = addressList?.getOrNull(0)?.getAddressLine(0) ?: "Address not found"
                                        rideLocationViewModel.dropLocation = geoPoint
                                        rideLocationViewModel.dropAddress = dropAddress
                                    } catch (e: Exception) {
                                        dropAddress = "Unable to fetch address"
                                    }

                                    val client = OkHttpClient()
                                    val apiKey = "5b3ce3597851110001cf62488c7eb6e4851342d2a5fadb85566ac738"
                                    val url =
                                        "https://api.openrouteservice.org/v2/directions/driving-car?api_key=$apiKey&start=${userGeoPoint.longitude},${userGeoPoint.latitude}&end=${geoPoint.longitude},${geoPoint.latitude}"

                                    val request = Request.Builder().url(url).build()

                                    Thread {
                                        try {
                                            val response = client.newCall(request).execute()
                                            val body = response.body?.string()

                                            if (response.isSuccessful && body != null) {
                                                val json = JSONObject(body)
                                                val coordinates = json
                                                    .getJSONArray("features")
                                                    .getJSONObject(0)
                                                    .getJSONObject("geometry")
                                                    .getJSONArray("coordinates")

                                                val routePoints = mutableListOf<GeoPoint>()
                                                for (i in 0 until coordinates.length()) {
                                                    val coord = coordinates.getJSONArray(i)
                                                    val lon = coord.getDouble(0)
                                                    val lat = coord.getDouble(1)
                                                    routePoints.add(GeoPoint(lat, lon))
                                                }

                                                map.post {
                                                    map.overlays.removeAll { it is Polyline }
                                                    val routeLine = Polyline().apply {
                                                        setPoints(routePoints)
                                                        color = android.graphics.Color.BLUE
                                                        width = 6f
                                                    }
                                                    map.overlays.add(routeLine)
                                                    val boundingBox = BoundingBox.fromGeoPoints(listOf(userGeoPoint, geoPoint))
                                                    map.zoomToBoundingBox(boundingBox, true)
                                                    map.invalidate()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("ROUTE_ERROR", "Exception: ${e.localizedMessage}")
                                        }
                                    }.start()
                                }
                                false
                            }
                            map
                        }
                    )

                    dropAddress?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Drop Location",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8C52FF)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF222222)
                                )
                            }
                        }
                    }

                    if (dropGeoPoint != null) {
                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Drop location confirmed:\n$dropAddress",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigate(Routes.OfferRide.route)
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C52FF))
                        ) {
                            Text("Confirm Drop Location")
                        }
                    }
                }
            }
        }
    }
}
