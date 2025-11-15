package com.notistorex.app.ui.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.hilt.navigation.compose.hiltViewModel
import com.notistorex.app.R
import com.notistorex.app.data.local.preferences.AppPreferences
import com.notistorex.app.ui.screens.dashboard.viewmodel.PasscodeViewModel
import com.notistorex.app.ui.theme.JetpackComposeTest1Theme
import com.notistorex.app.ui.theme.main_appColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasscodeScreenView(
    onNavigateBack: (() -> Unit)? = null,
    onPasscodeVerified: () -> Unit,
    viewModel: PasscodeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences(context) }
    
    // Initialize ViewModel based on whether passcode exists and intent
    LaunchedEffect(Unit) {
        val hasPasscode = appPreferences.getPasscode() != null
        val shouldDisable = appPreferences.getPasscodeDisableIntent()
        
        if (hasPasscode && shouldDisable) {
            // User wants to disable passcode - verify first
            viewModel.initForDisable()
        } else if (hasPasscode) {
            // Passcode exists - verify it
            viewModel.initForVerification()
        } else {
            // No passcode - setup new one
            viewModel.initForSetup()
        }
    }
    
    val passcodeState by viewModel.passcodeState.collectAsState()
    val isSettingUp = passcodeState.isSettingUp
    val isDisabling = passcodeState.isDisabling
    
    // Handle successful verification - disable if intent was to disable
    LaunchedEffect(passcodeState.isSuccess) {
        if (passcodeState.isSuccess) {
            val shouldDisable = appPreferences.getPasscodeDisableIntent()
            if (shouldDisable) {
                // Clear the disable intent flag
                appPreferences.setPasscodeDisableIntent(false)
            }
            // Delay before navigating
            kotlinx.coroutines.delay(300)
            onPasscodeVerified()
        }
    }

    Scaffold(
        topBar = {
            if (onNavigateBack != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = when {
                                isDisabling -> context.getString(R.string.disable_passcode)
                                isSettingUp -> context.getString(R.string.set_passcode)
                                else -> context.getString(R.string.enter_passcode)
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = main_appColor
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // Title
                Text(
                    text = when {
                        isDisabling -> context.getString(R.string.enter_passcode)
                        isSettingUp -> {
                            if (passcodeState.isConfirming) context.getString(R.string.confirm_passcode) else context.getString(R.string.create_passcode)
                        }
                        else -> context.getString(R.string.enter_passcode)
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp
                )
                
                // Subtitle (only show in verification mode)
                if (!isSettingUp && !isDisabling) {
                    Text(
                        text = context.getString(R.string.please_enter_your_passcode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }

                // PIN dots row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val passcode = passcodeState.currentPasscode
                        val isFilled = index < passcode.length
                        val currentLength = passcode.length
                        val isCurrent = index == currentLength - 1 && currentLength in 1..3

                        // Animate the active dot size smoothly
                        val dotSize by animateDpAsState(
                            targetValue = if (isCurrent) 22.dp else 16.dp,
                            label = "dotSizeAnim"
                        )

                        // Animate color transition (optional but looks great)
                        val dotColor by animateColorAsState(
                            targetValue = if (isFilled) main_appColor else Color.Transparent,
                            label = "dotColorAnim"
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .size(dotSize)
                                .background(
                                    color = dotColor,
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) main_appColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }


                // Error message
                if (passcodeState.errorMessage != null) {
                    Text(
                        text = passcodeState.errorMessage!!,
                        color = Color(0xFFDC2626),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }


                // Number pad
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: 1, 2, 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NumberButton("1") { viewModel.addDigit('1') }
                        NumberButton("2") { viewModel.addDigit('2') }
                        NumberButton("3") { viewModel.addDigit('3') }
                    }
                    
                    // Row 2: 4, 5, 6
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NumberButton("4") { viewModel.addDigit('4') }
                        NumberButton("5") { viewModel.addDigit('5') }
                        NumberButton("6") { viewModel.addDigit('6') }
                    }
                    
                    // Row 3: 7, 8, 9
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NumberButton("7") { viewModel.addDigit('7') }
                        NumberButton("8") { viewModel.addDigit('8') }
                        NumberButton("9") { viewModel.addDigit('9') }
                    }
                    
                    // Row 4: Empty, 0, Backspace
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Spacer(modifier = Modifier.size(80.dp))
                        NumberButton("0") { viewModel.addDigit('0') }
                        BackspaceButton { viewModel.removeDigit() }
                    }
                }

                // Success message
                if (passcodeState.isSuccess) {
                    LaunchedEffect(passcodeState.isSuccess) {
                        kotlinx.coroutines.delay(500)
                        onPasscodeVerified()
                    }
                }
            }
        }
    }
}


@Composable
private fun NumberButton(
    number: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .size(70.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (isPressed)
                    main_appColor.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isPressed) 2.dp else 4.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


@Composable
private fun BackspaceButton(
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .size(70.dp)
                .clickable(
                    interactionSource = interactionSource,
                    onClick = onClick
                ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (isPressed) main_appColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 2.dp else 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun PasscodeScreenPreview() {
    JetpackComposeTest1Theme(darkTheme = false) {
        // PasscodeScreenView(onNavigateBack = {}, onPasscodeVerified = {})
    }
}


