package com.jetpackComposeTest1

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SecondView(text: String?) {

    Column {
        Text(text = "Second View")
        Text(text = "Text from Starting View: $text")
    }
}