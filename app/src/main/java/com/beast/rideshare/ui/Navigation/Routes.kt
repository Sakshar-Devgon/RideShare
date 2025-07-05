package com.beast.rideshare.ui.Navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object SignIn : Routes("SignIn")
    object SignUp : Routes("SignUp")
    object Home : Routes("home")
}