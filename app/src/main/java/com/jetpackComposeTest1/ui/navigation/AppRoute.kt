package com.jetpackComposeTest1.ui.navigation

import kotlinx.serialization.Serializable

sealed class MainNavScreen(val route: String) {
    object Home: MainNavScreen("home_screen")
    object Groups: MainNavScreen("groups_screen")
    object Analytics: MainNavScreen("analytics_screen")
    object Settings: MainNavScreen("settings_screen")
}

interface AppNavigationRoute

@Serializable
object LoginScreenRoute:AppNavigationRoute

@Serializable
object DashboardScreenRoute:AppNavigationRoute

@Serializable
object  StatingViewRoute:AppNavigationRoute

@Serializable
data class SecondViewRoute(val text:String?):AppNavigationRoute

@Serializable
object HomeScreenRoute:AppNavigationRoute

@Serializable
object ProfileScreenRoute:AppNavigationRoute

@Serializable
object CartScreenRoute:AppNavigationRoute

@Serializable
object SettingScreenRoute:AppNavigationRoute

@Serializable
object AppSelectionScreenRoute:AppNavigationRoute

@Serializable
data class GroupAppSelectionRoute(
    val groupName: String,
    val groupId: String? = null
): AppNavigationRoute



@Serializable
data class NotificationDetailRoute(
    val packageName: String,
    val appName: String,
    val isFromNotification:Boolean = false,
    val selectedDate: Long? = null
): AppNavigationRoute

@Serializable
object AllUnreadNotificationsRoute: AppNavigationRoute

@Serializable
data class GroupAppsRoute(
    val groupId: String,
    val groupName: String
): AppNavigationRoute