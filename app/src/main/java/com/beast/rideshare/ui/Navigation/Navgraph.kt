package com.beast.rideshare.ui.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.beast.rideconnect.AuthViewModel
import com.beast.rideconnect.ui.screens.auth.SignInScreen
import com.beast.rideconnect.ui.screens.auth.SignUpScreen
import com.beast.rideshare.ui.Screens.SplashScreen
import com.beast.rideshare.ui.screens.home.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SignIn.route
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Routes.SignIn.route) {
            SignInScreen(navController, authViewModel)
        }

        composable(Routes.SignUp.route) {
            SignUpScreen(navController, authViewModel)
        }

        composable(Routes.Home.route) {
            HomeScreen(navController,authViewModel)
        }

    }
}

