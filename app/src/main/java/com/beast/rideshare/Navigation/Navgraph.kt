package com.beast.rideshare.Navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.beast.rideconnect.AuthViewModel
import com.beast.rideconnect.ui.screens.auth.SignInScreen
import com.beast.rideconnect.ui.screens.auth.SignUpScreen
import com.beast.rideshare.*
import com.beast.rideshare.ui.Screens.Home.HomeScreen
import com.beast.rideshare.ui.Screens.MapScreen.OSMMapScreen
import com.beast.rideshare.ui.Screens.RideScreens.FindRide.FindRideScreen
import com.beast.rideshare.ui.Screens.RideScreens.FindRide.FindRideViewModel
import com.beast.rideshare.ui.Screens.RideScreens.OfferRide.OfferRideScreen
import com.beast.rideshare.ui.Screens.RideScreens.OfferRide.RideLocationViewModel
import com.beast.rideshare.ui.Screens.RideScreens.OfferRides.RideViewModel
import com.beast.rideshare.ui.Screens.SplashScreen
import com.beast.rideshare.ui.Screens.Profile.ProfileScreen
import com.beast.rideshare.ui.Screens.Profile.ProfileViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    themeManager: com.beast.rideshare.ui.theme.ThemeManager,
    modifier: Modifier = Modifier
) {
    val rideViewModel: RideViewModel = viewModel()
    val locationViewModel: RideLocationViewModel = viewModel()
    val findRideViewModel: FindRideViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    val springSpec = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = springSpec
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = springSpec
            ) + fadeOut(animationSpec = tween(250))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = springSpec
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = springSpec
            ) + fadeOut(animationSpec = tween(250))
        }
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(navController = navController, authViewModel = authViewModel)
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
            FindRideScreen(navController, findRideViewModel)
        }
        composable(Routes.OSMMap.route) {
            OSMMapScreen(navController, locationViewModel, modifier = modifier.fillMaxSize())
        }
        composable(Routes.WaitingScreen.route) {
            WaitingScreen(rideViewModel = rideViewModel, navController = navController)
        }
        composable(Routes.RideHistory.route) {
            RideHistoryScreen()
        }
        composable(Routes.Profile.route) {
            ProfileScreen(navController, profileViewModel, themeManager)
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
