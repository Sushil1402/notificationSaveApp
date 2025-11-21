package com.notistorex.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.notistorex.app.data.local.preferences.AppPreferences
import com.notistorex.app.utils.BillingConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) : PurchasesUpdatedListener, BillingClientStateListener {

    private var billingClient: BillingClient? = null

    private val _billingConnectionState = MutableStateFlow<BillingConnectionState>(BillingConnectionState.DISCONNECTED)
    val billingConnectionState: StateFlow<BillingConnectionState> = _billingConnectionState.asStateFlow()

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails: StateFlow<List<ProductDetails>> = _productDetails.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _subscriptionPlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val subscriptionPlans: StateFlow<List<SubscriptionPlan>> = _subscriptionPlans.asStateFlow()

    private var purchaseCallback: ((PurchaseState) -> Unit)? = null

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        startConnection()
    }

    private fun startConnection() {
        _billingConnectionState.value = BillingConnectionState.CONNECTING
        billingClient?.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _billingConnectionState.value = BillingConnectionState.CONNECTED
            queryProductDetails()
            queryPurchases()
        } else {
            _billingConnectionState.value = BillingConnectionState.ERROR
        }
    }

    override fun onBillingServiceDisconnected() {
        _billingConnectionState.value = BillingConnectionState.DISCONNECTED
        Log.d(TAG, "Billing service disconnected")
        // Try to reconnect
        startConnection()
    }

    private fun queryProductDetails() {
        val productList = BillingConstants.SUBSCRIPTION_PRODUCT_IDS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, detailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetails.value = detailsList

                val plans = detailsList.flatMap { productDetails ->
                    convertToPlans(productDetails)
                }
                _subscriptionPlans.value = plans


            } else {
            }
        }

    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchases.value = purchases
                processPurchases(purchases)
            } else {
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let {
                    _purchases.value = it
                    processPurchases(it)
                    _purchaseState.value = PurchaseState.Success
                    purchaseCallback?.invoke(PurchaseState.Success)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
                purchaseCallback?.invoke(PurchaseState.Cancelled)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseState.value = PurchaseState.AlreadyOwned
                purchaseCallback?.invoke(PurchaseState.AlreadyOwned)
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
                purchaseCallback?.invoke(PurchaseState.Error(billingResult.debugMessage))
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        var hasActiveSubscription = false

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }

                // Check if subscription is active
                if (purchase.products.firstOrNull()
                        ?.let { BillingConstants.SUBSCRIPTION_PRODUCT_IDS.contains(it) } == true
                ) {
                    hasActiveSubscription = true
                }
            }
        }

        // Update premium status based on active subscription
        appPreferences.setPremiumUnlocked(hasActiveSubscription)
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged: ${purchase.products}")
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
            }
        }
    }

    fun launchPurchaseFlow(
        activity: Activity,
        productId: String,
        offerToken: String,
        callback: ((PurchaseState) -> Unit)? = null
    ) {
        purchaseCallback = callback

        val productDetails = _productDetails.value.find {
            it.productId == productId
        }

        if (productDetails == null) {
            _purchaseState.value = PurchaseState.Error("Product not found")
            callback?.invoke(PurchaseState.Error("Product not found"))
            return
        }

        // Ensure requested offer belongs to this product
        val tokenToUse = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.offerToken == offerToken }
            ?.offerToken
            ?: offerToken

        if (tokenToUse.isBlank()) {
            _purchaseState.value = PurchaseState.Error("No offer token found")
            callback?.invoke(PurchaseState.Error("No offer token found"))
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(tokenToUse)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _purchaseState.value = PurchaseState.Processing
        val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)

        if (billingResult?.responseCode != BillingClient.BillingResponseCode.OK) {
            _purchaseState.value = PurchaseState.Error(billingResult?.debugMessage ?: "Unknown error")
            callback?.invoke(PurchaseState.Error(billingResult?.debugMessage ?: "Unknown error"))
        }
    }

    fun checkSubscriptionStatus(): Boolean {
        return _purchases.value.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    purchase.isAcknowledged &&
                    BillingConstants.SUBSCRIPTION_PRODUCT_IDS.contains(purchase.products.firstOrNull())
        }
    }

    fun getProductDetails(productId: String): ProductDetails? {
        return _productDetails.value.firstOrNull { it.productId == productId }
    }

    fun refreshPurchases() {
        queryPurchases()
    }

    companion object {
        private const val TAG = "find_error"
    }

    private fun convertToPlans(productDetails: ProductDetails): List<SubscriptionPlan> {
        val offers = productDetails.subscriptionOfferDetails ?: return emptyList()

        return offers.map { offer ->
            val phase = offer.pricingPhases.pricingPhaseList.first()

            SubscriptionPlan(
                productId = productDetails.productId,
                basePlanId = offer.basePlanId,
                price = phase.formattedPrice,
                priceMicros = phase.priceAmountMicros,
                currency = phase.priceCurrencyCode,
                billingPeriod = phase.billingPeriod,
                offerToken = offer.offerToken
            )
        }
    }
}

data class SubscriptionPlan(
    val productId: String,
    val basePlanId: String,
    val price: String,
    val priceMicros: Long,
    val currency: String,
    val billingPeriod: String,
    val offerToken: String
)

sealed class BillingConnectionState {
    object DISCONNECTED : BillingConnectionState()
    object CONNECTING : BillingConnectionState()
    object CONNECTED : BillingConnectionState()
    object ERROR : BillingConnectionState()
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Processing : PurchaseState()
    object Success : PurchaseState()
    object Cancelled : PurchaseState()
    object AlreadyOwned : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

