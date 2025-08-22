package com.beast.rideshare.Navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object SignIn : Routes("SignIn")
    object SignUp : Routes("SignUp")
    object Home : Routes("home")
    object OfferRide: Routes("offerRide")
    object FindRide: Routes("findRide")
    object OSMMap : Routes("osm_map") // ✅ map screen route
    object WaitingScreen : Routes("waitingScreen") // New route for waiting screen
    object RideHistory : Routes("rideHistory") // New route for ride history
    object Profile : Routes("profile") // New route for profile/settings screen
    object RideDetail : Routes("rideDetail/{rideId}") {
        fun createRoute(rideId: String) = "rideDetail/$rideId"
    }

}