package com.beast.rideshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.beast.rideconnect.AuthViewModel
import com.beast.rideshare.Navigation.AppNavHost
import com.beast.rideshare.ui.Screens.RideScreens.OfferRide.RideLocationViewModel
import com.beast.rideshare.ui.theme.RideShareTheme
import com.beast.rideshare.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val rideLocationViewModel: RideLocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = remember { ThemeManager(this) }
            RideShareTheme(darkTheme = themeManager.isDarkMode) {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    authViewModel = authViewModel,
                    themeManager = themeManager
                )
            }
        }
    }
}
