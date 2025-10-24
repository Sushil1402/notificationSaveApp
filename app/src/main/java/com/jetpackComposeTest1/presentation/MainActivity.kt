package com.jetpackComposeTest1.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import com.jetpackComposeTest1.DashboardScreenRoute
import com.jetpackComposeTest1.DashboardScreenView
import com.jetpackComposeTest1.LoginScreenRoute
import com.jetpackComposeTest1.LoginScreenView
import com.jetpackComposeTest1.ui.theme.JetpackComposeTest1Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                        startDestination = DashboardScreenRoute,
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

