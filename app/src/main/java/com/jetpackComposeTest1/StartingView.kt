package com.jetpackComposeTest1

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController

@Composable
fun StartingView(navController: NavController) {
    val text = remember { mutableStateOf("") }
    Column {

        Text(text="Stating View")

        OutlinedTextField(
            value = text.value,
            onValueChange = {text.value = it}
        )

        Button(onClick = {
            navController.navigate(SecondViewRoute(text.value))
        }) {
            Text(text= "Navigation top Second View")
        }


    }
}