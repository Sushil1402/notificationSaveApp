package com.jetpackComposeTest1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jetpackComposeTest1.dashboard.CartScreenView
import com.jetpackComposeTest1.dashboard.HomeScreenView
import com.jetpackComposeTest1.dashboard.ProfileScreenView
import com.jetpackComposeTest1.dashboard.SettingScreenView
import com.jetpackComposeTest1.ui.theme.JetpackComposeTest1Theme
import com.jetpackComposeTest1.ui.theme.main_appColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            JetpackComposeTest1Theme {
                Column(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = LoginScreenRoute,
                    ) {
                        composable<LoginScreenRoute> {
                            LoginScreenView(navController)
                        }

                        composable<DashboardScreenRoute> {
                            DashboardScreenView()
                        }

                    }
                }
            }
        }
    }
}

