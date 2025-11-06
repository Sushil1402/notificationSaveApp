package com.jetpackComposeTest1.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jetpackComposeTest1.data.local.preferences.AppPreferences
import com.jetpackComposeTest1.ui.navigation.AllUnreadNotificationsRoute
import com.jetpackComposeTest1.ui.navigation.AppSelectionScreenRoute
import com.jetpackComposeTest1.ui.navigation.DashboardScreenRoute
import com.jetpackComposeTest1.ui.navigation.NotificationDetailRoute
import com.jetpackComposeTest1.ui.navigation.NotificationDetailViewRoute
import com.jetpackComposeTest1.ui.navigation.GroupAppSelectionRoute
import com.jetpackComposeTest1.ui.navigation.GroupAppsRoute
import com.jetpackComposeTest1.ui.navigation.SettingScreenRoute
import com.jetpackComposeTest1.ui.navigation.PasscodeScreenRoute
import com.jetpackComposeTest1.ui.screens.DashboardScreenView
import com.jetpackComposeTest1.ui.screens.dashboard.GroupAppsScreenView
import com.jetpackComposeTest1.ui.navigation.LoginScreenRoute
import com.jetpackComposeTest1.ui.screens.LoginScreenView
import com.jetpackComposeTest1.ui.screens.appselection.AppSelectionScreen
import com.jetpackComposeTest1.ui.screens.appselection.DatabaseAppSelectionScreen
import com.jetpackComposeTest1.ui.screens.dashboard.AllUnreadNotificationsScreen
import com.jetpackComposeTest1.ui.screens.dashboard.NotificationDetailScreen
import com.jetpackComposeTest1.ui.screens.dashboard.NotificationDetailViewScreen
import com.jetpackComposeTest1.ui.screens.dashboard.SettingsScreenView
import com.jetpackComposeTest1.ui.screens.dashboard.PasscodeScreenView
import com.jetpackComposeTest1.ui.theme.JetpackComposeTest1Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val context = LocalContext.current
            val appPreferences = remember { AppPreferences(context) }
            
            // Check if passcode is enabled to determine start destination
            val passcodeEnabled = remember { appPreferences.isPasscodeEnabled() }
            val hasPasscode = remember { appPreferences.getPasscode() != null }
            val startDestination = if (passcodeEnabled && hasPasscode) {
                PasscodeScreenRoute
            } else {
                DashboardScreenRoute
            }
            
            JetpackComposeTest1Theme {
                Column(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                    ) {
                        composable<AppSelectionScreenRoute> {
                            AppSelectionScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToDashboard = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable<LoginScreenRoute> {
                            LoginScreenView(navController)
                        }

                        composable<DashboardScreenRoute> {
                            DashboardScreenView(){navToScreen->
                                navController.navigate(navToScreen)
                            }
                        }

                        composable<NotificationDetailRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<NotificationDetailRoute>()
                            NotificationDetailScreen(
                                packageName = args.packageName,
                                appName = args.appName,
                                isFromNotification = args.isFromNotification,
                                selectedDate= args.selectedDate,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToDetail = { notificationId ->
                                    navController.navigate(NotificationDetailViewRoute(notificationId))
                                }
                            )
                        }

                        composable<NotificationDetailViewRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<NotificationDetailViewRoute>()
                            NotificationDetailViewScreen(
                                notificationId = args.notificationId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNotificationDeleted = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable<GroupAppSelectionRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<GroupAppSelectionRoute>()
                            DatabaseAppSelectionScreen(
                                groupName=args.groupName,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                title = "Add Apps to ${args.groupName}",
                                description = "Select apps to include in your '${args.groupName}' group",
                                confirmButtonText = "Add to Group"
                            )
                        }

                        composable<GroupAppsRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<GroupAppsRoute>()
                            GroupAppsScreenView(
                                groupId = args.groupId,
                                groupName = args.groupName,
                                navToScreen = { navToScreen ->
                                    navController.navigate(navToScreen)
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable<AllUnreadNotificationsRoute> {
                            AllUnreadNotificationsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToDetail = { notificationId ->
                                    navController.navigate(NotificationDetailViewRoute(notificationId))
                                }
                            )
                        }

                        composable<SettingScreenRoute> {
                            SettingsScreenView(
                                navToScreen = { route ->
                                    navController.navigate(route)
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable<PasscodeScreenRoute> {
                            PasscodeScreenView(
                                onNavigateBack = if (navController.previousBackStackEntry != null) {
                                    { navController.popBackStack() }
                                } else {
                                    null // No back button on initial launch
                                },
                                onPasscodeVerified = {
                                    // Navigate to dashboard after successful verification
                                    navController.navigate(DashboardScreenRoute) {
                                        // Clear back stack so user can't go back to passcode screen
                                        popUpTo(PasscodeScreenRoute) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }

                        }
                    }
                }
            }
        }
    }


