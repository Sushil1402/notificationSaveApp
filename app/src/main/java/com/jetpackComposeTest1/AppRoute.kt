package com.jetpackComposeTest1

import kotlinx.serialization.Serializable

sealed class MainNavScreen(val route: String) {
    object Home: MainNavScreen("home_screen")
    object Profile: MainNavScreen("profile_screen")
    object Cart: MainNavScreen("cart_screen")
    object Setting: MainNavScreen("setting_screen")
}

@Serializable
object LoginScreenRoute

@Serializable
object DashboardScreenRoute

@Serializable
object  StatingViewRoute

@Serializable
data class SecondViewRoute(val text:String?)

@Serializable
object HomeScreenRoute

@Serializable
object ProfileScreenRoute

@Serializable
object CartScreenRoute

@Serializable
object SettingScreenRoute
