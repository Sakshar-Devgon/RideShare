package com.beast.rideshare.Navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.beast.rideconnect.AuthViewModel
import com.beast.rideconnect.ui.screens.auth.SignInScreen
import com.beast.rideconnect.ui.screens.auth.SignUpScreen
import com.beast.rideshare.RideDetailScreen
import com.beast.rideshare.RideHistoryScreen
import com.beast.rideshare.WaitingScreen
import com.beast.rideshare.ui.Screens.MapScreen.OSMMapScreen
import com.beast.rideshare.ui.Screens.RideScreens.FindRide.FindRideScreen
import com.beast.rideshare.ui.Screens.RideScreens.FindRide.FindRideViewModel
import com.beast.rideshare.ui.Screens.RideScreens.OfferRide.OfferRideScreen
import com.beast.rideshare.ui.Screens.SplashScreen
import com.beast.rideshare.ui.screens.home.HomeScreen
import com.beast.rideshare.ui.Screens.RideScreens.OfferRides.RideViewModel
import com.beast.rideshare.ui.Screens.RideScreens.OfferRide.RideLocationViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    // ✅ Proper ViewModel setup using lifecycle-aware viewModel()
    val rideViewModel: RideViewModel = viewModel()
    val locationViewModel: RideLocationViewModel = viewModel()
    val findRideViewModel: FindRideViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
        modifier = modifier
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(navController = navController , authViewModel)
        }

        composable(Routes.SignIn.route) {
            SignInScreen(navController, authViewModel)
        }

        composable(Routes.SignUp.route) {
            SignUpScreen(navController, authViewModel)
        }

        composable(Routes.Home.route) {
            HomeScreen(navController, authViewModel)
        }

        composable(Routes.OfferRide.route) {
            OfferRideScreen(navController, rideViewModel, locationViewModel)
        }

        composable(Routes.FindRide.route) {
            FindRideScreen(
                navController, findRideViewModel = findRideViewModel
            )
        }

        composable(Routes.OSMMap.route) {
            OSMMapScreen(
                navController, locationViewModel,
                modifier = modifier.fillMaxSize()
            )
        }

        composable(Routes.WaitingScreen.route) {
            WaitingScreen(navController = navController, rideViewModel = rideViewModel)
        }

        // ✅ Corrected here (was Composable with capital C)
        composable(Routes.RideHistory.route) {
            RideHistoryScreen()
        }
        composable(
            route = Routes.RideDetail.route,
            arguments = listOf(navArgument("rideId") { type = NavType.StringType })
        ) { backStackEntry ->
            val rideId = backStackEntry.arguments?.getString("rideId")
            if (rideId != null) {
                RideDetailScreen(rideId = rideId)
            } else {
                Text("Error loading ride details")
            }
        }


    }
}
