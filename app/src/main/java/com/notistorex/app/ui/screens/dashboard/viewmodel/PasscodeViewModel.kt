package com.notistorex.app.ui.screens.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notistorex.app.data.local.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasscodeViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    data class PasscodeState(
        val currentPasscode: String = "",
        val firstPasscode: String = "",
        val isConfirming: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false,
        val isSettingUp: Boolean = false,
        val isDisabling: Boolean = false
    )

    private val _passcodeState = MutableStateFlow(PasscodeState())
    val passcodeState: StateFlow<PasscodeState> = _passcodeState.asStateFlow()

    fun initForSetup() {
        _passcodeState.value = PasscodeState(
            isSettingUp = true,
            isDisabling = false
        )
    }

    fun initForVerification() {
        _passcodeState.value = PasscodeState(
            isSettingUp = false,
            isDisabling = false
        )
    }
    
    fun initForDisable() {
        _passcodeState.value = PasscodeState(
            isSettingUp = false,
            isDisabling = true
        )
    }

    fun addDigit(digit: Char) {
        if (_passcodeState.value.currentPasscode.length >= 4) return

        val newPasscode = _passcodeState.value.currentPasscode + digit
        _passcodeState.value = _passcodeState.value.copy(
            currentPasscode = newPasscode,
            errorMessage = null
        )

        // Check if passcode is complete
        if (newPasscode.length == 4) {
            handleCompletePasscode(newPasscode)
        }
    }

    fun removeDigit() {
        if (_passcodeState.value.currentPasscode.isEmpty()) return

        val newPasscode = _passcodeState.value.currentPasscode.dropLast(1)
        _passcodeState.value = _passcodeState.value.copy(
            currentPasscode = newPasscode,
            errorMessage = null
        )
    }

    private fun handleCompletePasscode(passcode: String) {
        if (_passcodeState.value.isSettingUp) {
            handleSetupPasscode(passcode)
        } else {
            handleVerificationPasscode(passcode)
        }
    }

    private fun handleSetupPasscode(passcode: String) {
        viewModelScope.launch {
            if (!_passcodeState.value.isConfirming) {
                // First time entering passcode - save it and ask for confirmation
                _passcodeState.value = _passcodeState.value.copy(
                    firstPasscode = passcode,
                    currentPasscode = "",
                    isConfirming = true,
                    errorMessage = null
                )
            } else {
                // Confirming passcode - check if it matches
                if (passcode == _passcodeState.value.firstPasscode) {
                    // Passcodes match - save it
                    appPreferences.setPasscode(passcode)
                    appPreferences.setPasscodeEnabled(true)
                    _passcodeState.value = _passcodeState.value.copy(
                        isSuccess = true,
                        errorMessage = null,
                        isConfirming = false
                    )
                } else {
                    // Passcodes don't match - show error
                    _passcodeState.value = _passcodeState.value.copy(
                        currentPasscode = "",
                        firstPasscode = "",
                        isConfirming = false,
                        errorMessage = "Passcodes don't match. Please try again."
                    )
                }
            }
        }
    }

    private fun handleVerificationPasscode(passcode: String) {
        viewModelScope.launch {
            val savedPasscode = appPreferences.getPasscode()
            
            if (savedPasscode == passcode) {
                // Passcode matches
                if (_passcodeState.value.isDisabling) {
                    // Disable passcode
                    appPreferences.setPasscodeEnabled(false)
                    appPreferences.clearPasscode()
                }
                _passcodeState.value = _passcodeState.value.copy(
                    isSuccess = true,
                    errorMessage = null
                )
            } else {
                // Passcode doesn't match - show error and reset
                _passcodeState.value = _passcodeState.value.copy(
                    currentPasscode = "",
                    errorMessage = "Incorrect passcode. Please try again."
                )
            }
        }
    }

    fun disablePasscode() {
        viewModelScope.launch {
            appPreferences.setPasscodeEnabled(false)
            appPreferences.clearPasscode()
        }
    }
}

