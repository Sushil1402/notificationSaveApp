package com.jetpackComposeTest1.presentation

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jetpackComposeTest1.ui.navigation.AllUnreadNotificationsRoute
import com.jetpackComposeTest1.ui.navigation.AppSelectionScreenRoute
import com.jetpackComposeTest1.ui.navigation.AdFreeScreenRoute
import com.jetpackComposeTest1.ui.navigation.DashboardScreenRoute
import com.jetpackComposeTest1.ui.navigation.NotificationDetailRoute
import com.jetpackComposeTest1.ui.navigation.NotificationDetailViewRoute
import com.jetpackComposeTest1.data.local.preferences.AppPreferences
import com.jetpackComposeTest1.model.setting.ThemeMode
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
import com.jetpackComposeTest1.ui.screens.dashboard.AdFreeScreenView
import com.jetpackComposeTest1.ui.theme.JetpackComposeTest1Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val context = LocalContext.current
            val activity = remember(context) { context as? Activity }
            val appPreferences = remember { AppPreferences(context) }

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val handleBack: () -> Unit = {
                if (!navController.popBackStack()) {
                    activity?.finish()
                }
            }

            BackHandler(
                enabled = currentDestination?.route == DashboardScreenRoute::class.qualifiedName &&
                        navController.previousBackStackEntry == null
            ) {
                activity?.finish()
            }

            // Check if passcode is enabled to determine start destination
            val passcodeEnabled = remember { appPreferences.isPasscodeEnabled() }
            val hasPasscode = remember { appPreferences.getPasscode() != null }
            val startDestination = if (passcodeEnabled && hasPasscode) {
                PasscodeScreenRoute
            } else {
                DashboardScreenRoute
            }

            val themeModeFlow = remember { appPreferences.themeModeFlow() }
            val themeMode by themeModeFlow.collectAsState(initial = appPreferences.getThemeMode())
            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            JetpackComposeTest1Theme(darkTheme = useDarkTheme) {
                Column(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                    ) {
                        composable<AppSelectionScreenRoute> {
                            AppSelectionScreen(
                                onNavigateBack = {
                                    handleBack()
                                },
                                onNavigateToDashboard = {
                                    handleBack()
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
                                    handleBack()
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
                                    handleBack()
                                },
                                onNotificationDeleted = {
                                    handleBack()
                                }
                            )
                        }

                        composable<GroupAppSelectionRoute> { backStackEntry ->
                            val args = backStackEntry.toRoute<GroupAppSelectionRoute>()
                            DatabaseAppSelectionScreen(
                                groupName=args.groupName,
                                onNavigateBack = {
                                    handleBack()
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
                                    handleBack()
                                }
                            )
                        }
                        composable<AllUnreadNotificationsRoute> {
                            AllUnreadNotificationsScreen(
                                onNavigateBack = {
                                    handleBack()
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
                                    handleBack()
                                }
                            )
                        }

                        composable<AdFreeScreenRoute> {
                            AdFreeScreenView(
                                onNavigateBack = {
                                    handleBack()
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
                                    if (!navController.popBackStack()) {
                                        navController.navigate(DashboardScreenRoute) {
                                            popUpTo(PasscodeScreenRoute) {
                                                inclusive = true
                                            }
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


