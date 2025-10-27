package com.jetpackComposeTest1.ui.screens.dashboard

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackComposeTest1.ui.screens.dashboard.viewmodel.ProfileViewModel
import com.jetpackComposeTest1.services.MyNotificationListener
import com.jetpackComposeTest1.services.NotificationForegroundService
import com.jetpackComposeTest1.ui.utils.NotificationUtils
import com.jetpackComposeTest1.ui.utils.toImageBitmap
import kotlinx.coroutines.flow.collectLatest


@Composable
fun ProfileScreenView(vm: ProfileViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val apps by vm.apps.collectAsState()

    var notifications by remember { mutableStateOf(listOf<String>()) }

    // Check permission once
    LaunchedEffect(Unit) {
        val serviceIntent = Intent(context, NotificationForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

        if (!isNotificationServiceEnabled(context)) {
            openNotificationAccessSettings(context)
        } else {
            vm.getAllApps(context)
            // restart service to make sure it is active
            NotificationUtils.restartNotificationListenerService(context)
        }
    }

    // Collect notificationsFlow
    LaunchedEffect(Unit) {
        MyNotificationListener.notificationsFlow.collectLatest { msg ->
            notifications = notifications + msg
        }
    }



    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {


        if (apps.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No apps found")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { openNotificationAccessSettings(context) }) {
                    Text("Grant Notification Access")
                }
            }
        } else {
            Column {
                Button(onClick = {
                    NotificationUtils.showNotification(context, "Hello!", "This is from Jetpack Compose 🚀")
                }) {
                    Text("Show Notification")
                }
                Button(onClick = {
                    vm.clearAllNotification()
                }) {
                    Text("Delete Database")
                }
                LazyColumn {

                    items(apps.size) { index ->
                        Row(
                            Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = apps[index].icon.toImageBitmap(),
                                contentDescription = apps[index].name,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(text = apps[index].name)
                        }
                    }
                }
            }

        }
    }
}

fun isNotificationServiceEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    if (!TextUtils.isEmpty(flat)) {
        val names = flat.split(":").toTypedArray()
        for (name in names) {
            val cn = android.content.ComponentName.unflattenFromString(name)
            if (cn != null) {
                if (TextUtils.equals(pkgName, cn.packageName)) {
                    return true
                }
            }
        }
    }
    return false
}

fun openNotificationAccessSettings(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    ContextCompat.startActivity(context, intent, null)
}

