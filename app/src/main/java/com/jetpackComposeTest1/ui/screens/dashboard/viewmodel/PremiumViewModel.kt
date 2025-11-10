package com.jetpackComposeTest1.ui.screens.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackComposeTest1.data.local.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = appPreferences
        .premiumStatusFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = appPreferences.isPremiumUnlocked()
        )

    fun setPremium(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setPremiumUnlocked(enabled)
        }
    }
}

