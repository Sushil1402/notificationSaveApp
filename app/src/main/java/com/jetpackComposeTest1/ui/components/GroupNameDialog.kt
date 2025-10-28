package com.jetpackComposeTest1.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jetpackComposeTest1.ui.theme.main_appColor

@Composable
fun GroupNameDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialName: String = "",
    title: String = "Create New Group",
    placeholder: String = "Enter group name",
    confirmButtonText: String = "Create",
    cancelButtonText: String = "Cancel"
) {
    // Text Field state
    var textFieldValue by remember(initialName) { 
        mutableStateOf(TextFieldValue(initialName)) 
    }
    
    if (isVisible) {
        Dialog(
            onDismissRequest = {
                textFieldValue = TextFieldValue("")
                onDismiss()
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Description
                    Text(
                        text = "Choose a name for your notification group",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        placeholder = { Text(placeholder) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = main_appColor,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = {
                                textFieldValue = TextFieldValue("")
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            ),
                        ) {
                            Text(
                                text = cancelButtonText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Confirm Button
                        Button(
                            onClick = {
                                val groupName = textFieldValue.text.trim()
                                if (groupName.isNotEmpty()) {
                                    onConfirm(groupName)
                                    textFieldValue = TextFieldValue("") // Reset text field
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = main_appColor
                            ),
                            enabled = textFieldValue.text.trim().isNotEmpty()
                        ) {
                            Text(
                                text = confirmButtonText,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
