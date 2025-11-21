package com.notistorex.app.utils

/**
 * Central place for Google Play Billing product identifiers that are used across the
 * app (ads removal, premium unlock, etc). Replace the placeholder product IDs with
 * the actual IDs configured in the Play Console.
 */
object BillingConstants {

    /**
     * Google Play Billing now uses a single product with multiple base plans.
     * Replace this ID with the product configured in Play Console.
     */
    const val PREMIUM_PRODUCT_ID = "premium_id"

    val SUBSCRIPTION_PRODUCT_IDS = listOf(PREMIUM_PRODUCT_ID)

    /**
     * We no longer rely on plan titles to decide the product id – the UI always maps to the
     * single premium product.
     */
    fun getProductIdForPlan(@Suppress("UNUSED_PARAMETER") planTitle: String?): String = PREMIUM_PRODUCT_ID
}