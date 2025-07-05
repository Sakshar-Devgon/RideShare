package com.beast.rideshare.ui.Screens.RideScreens.OfferRide
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint

class RideLocationViewModel : ViewModel() {

    var pickupLocation: GeoPoint? = null
    var pickupAddress: String? = null

    var dropLocation: GeoPoint? = null
    var dropAddress: String? = null

    // "pickup" or "drop" — helps track selection state if needed
    var selectionType: String = "pickup"
}
