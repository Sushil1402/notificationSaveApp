package com.notistorex.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.notistorex.app.ui.navigation.DashboardScreenRoute

@Composable
fun LoginScreenView(navController: NavController) {
    val userName = remember {  mutableStateOf("testUser") }
    val password = remember {  mutableStateOf("Pass123") }
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        Text(text = "Login Screen", modifier = Modifier.align(alignment = Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(height = 20.dp))
        Text(text = "User Name")
        Spacer(modifier = Modifier.height(height = 10.dp))
        TextField(
            value = userName.value,
            onValueChange = {  userName.value = it },
            label = { Text("Enter User Name ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().width(50.dp)
        )
        Spacer(modifier = Modifier.height(height = 20.dp))
        Text(text = "Password")
        Spacer(modifier = Modifier.height(height = 10.dp))
        TextField(
            value = password.value,
            onValueChange = {  password.value = it },
            label = { Text("Enter Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().width(50.dp)
        )

        Spacer(modifier = Modifier.height(height = 20.dp))
         Button(modifier = Modifier.align(alignment = Alignment.CenterHorizontally), onClick = {
             val userNameValue = userName.value
             val passwordValue = password.value
             if(userNameValue=="testUser" && passwordValue =="Pass123"){
                 navController.navigate(DashboardScreenRoute)
             }else{
                 Toast.makeText(context, "Wrong User", Toast.LENGTH_SHORT).show()
             }

         }) {
             Text("Login")
         }

    }
}