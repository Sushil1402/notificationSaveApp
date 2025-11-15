package com.notistorex.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Data class to represent a menu option in the MoreOptionsMenu
 */
data class MenuOption(
    val title: String,
    val icon: ImageVector,
    val iconTint: Color = Color.Unspecified,
    val onClick: () -> Unit
)

/**
 * Reusable MoreOptionsMenu component that displays a dropdown menu
 * with customizable options when the MoreVert icon is clicked.
 *
 * @param options List of menu options to display
 * @param iconTint Color for the MoreVert icon (default is White)
 * @param modifier Modifier for the IconButton
 */
@Composable
fun MoreOptionsMenu(
    options: List<MenuOption>,
    iconTint: Color = Color.White,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { showMenu = true },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.title) },
                    onClick = {
                        option.onClick()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (option.iconTint != Color.Unspecified) {
                                option.iconTint
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}

