package com.jetpackComposeTest1.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.jetpackComposeTest1.ui.navigation.AppNavigationRoute
import com.jetpackComposeTest1.ui.navigation.MainNavScreen
import com.jetpackComposeTest1.ui.screens.dashboard.AnalyticsScreenView
import com.jetpackComposeTest1.ui.screens.dashboard.HomeScreenView
import com.jetpackComposeTest1.ui.screens.dashboard.GroupsScreenView
import com.jetpackComposeTest1.ui.screens.dashboard.SettingsScreenView
import com.jetpackComposeTest1.ui.theme.main_appColor

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DashboardScreenView(navToScreen:(AppNavigationRoute)->Unit) {
    val navController = rememberNavController()
    val navigationItems = listOf(
        com.jetpackComposeTest1.ui.navigation.NavigationItem(
            "Home",
            Icons.Default.Home,
            MainNavScreen.Home.route
        ),
        com.jetpackComposeTest1.ui.navigation.NavigationItem(
            "Groups",
            Icons.Default.Info,
            MainNavScreen.Groups.route
        ),
        com.jetpackComposeTest1.ui.navigation.NavigationItem(
            "Analytics",
            Icons.Default.Info,
            MainNavScreen.Analytics.route
        ),
        com.jetpackComposeTest1.ui.navigation.NavigationItem(
            "Settings",
            Icons.Default.Settings,
            MainNavScreen.Settings.route
        ),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            Card(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    navigationItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = main_appColor,
                                unselectedIconColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // ⚡️ Trick: Don't apply bottom padding, only top/inset padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(main_appColor)
                .padding(
                    start = innerPadding.calculateStartPadding(layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr),
                    top = innerPadding.calculateTopPadding()

                )
        ) {
            NavHost(
                navController = navController,
                startDestination = MainNavScreen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(MainNavScreen.Home.route) { HomeScreenView(navToScreen) }
                composable(MainNavScreen.Groups.route) { GroupsScreenView(navToScreen) }
                composable(MainNavScreen.Analytics.route) { AnalyticsScreenView() }
                composable(MainNavScreen.Settings.route) { SettingsScreenView() }
            }
        }
    }
}
