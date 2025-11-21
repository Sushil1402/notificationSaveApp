package com.notistorex.app.ui.screens.dashboard.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notistorex.app.ads.BillingManager
import com.notistorex.app.ads.PurchaseState
import com.notistorex.app.data.local.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val billingManager: BillingManager
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = combine(
        appPreferences.premiumStatusFlow(),
        billingManager.purchases
    ) { isPremiumUnlocked, _ ->
        isPremiumUnlocked || billingManager.checkSubscriptionStatus()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = appPreferences.isPremiumUnlocked() || billingManager.checkSubscriptionStatus()
    )

    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState
    val billingConnectionState = billingManager.billingConnectionState
    val productDetails = billingManager.productDetails
    val subscriptionPlans = billingManager.subscriptionPlans

    fun setPremium(enabled: Boolean) {
        viewModelScope.launch {
            appPreferences.setPremiumUnlocked(enabled)
        }
    }

    fun refreshPurchases() {
        billingManager.refreshPurchases()
    }

    fun purchaseSubscription(activity: Activity, productId: String, offerToken: String) {
        billingManager.launchPurchaseFlow(activity, productId, offerToken)
    }
}

