package com.notistorex.app.ads

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Rewarded Ad Manager to handle loading and showing rewarded ads
 */

class RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    companion object {
        private const val TAG = "RewardedAdManager"
        // Test ad unit ID - Replace with your actual ad unit ID in production
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    /**
     * Load a rewarded ad
     */
    fun loadRewardedAd(activity: Activity, onAdLoaded: (() -> Unit)? = null, onAdFailed: ((String) -> Unit)? = null) {
        Log.d(TAG, "loadRewardedAd called - isAdLoading: $isAdLoading, hasAd: ${rewardedAd != null}")
        
        if (isAdLoading) {
            Log.d(TAG, "Ad is already loading, ignoring duplicate request")
            return
        }

        if (rewardedAd != null) {
            Log.d(TAG, "Ad is already loaded, invoking onAdLoaded callback")
            onAdLoaded?.invoke()
            return
        }

        Log.d(TAG, "Starting to load rewarded ad with unit ID: $REWARDED_AD_UNIT_ID")
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        try {
            RewardedAd.load(
                activity,
                REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "✗ Rewarded ad failed to load - Code: ${loadAdError.code}, Message: ${loadAdError.message}, Domain: ${loadAdError.domain}, ResponseInfo: ${loadAdError.responseInfo}")
                        rewardedAd = null
                        isAdLoading = false
                        // Always invoke callback on main thread
                        activity.runOnUiThread {
                            onAdFailed?.invoke("Failed to load ad: ${loadAdError.message} (Code: ${loadAdError.code})")
                        }
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "✓ Rewarded ad loaded successfully!")
                        rewardedAd = ad
                        isAdLoading = false
                        // Always invoke callback on main thread
                        activity.runOnUiThread {
                            onAdLoaded?.invoke()
                        }
                    }
                }
            )
            Log.d(TAG, "RewardedAd.load() call completed, waiting for callbacks...")
        } catch (e: Exception) {
            Log.e(TAG, "Exception in RewardedAd.load(): ${e.message}", e)
            isAdLoading = false
            rewardedAd = null
            activity.runOnUiThread {
                onAdFailed?.invoke("Exception loading ad: ${e.message}")
            }
        }
    }

    /**
     * Show the rewarded ad
     * @param activity The activity to show the ad in
     * @param onUserEarnedReward Callback when user earns reward (ad watched completely)
     * @param onAdFailedToShow Callback when ad fails to show
     * @param onAdDismissed Callback when ad is dismissed
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (() -> Unit)? = null,
        onAdFailedToShow: ((String) -> Unit)? = null,
        onAdDismissed: (() -> Unit)? = null
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.e(TAG, "Rewarded ad is not loaded")
            onAdFailedToShow?.invoke("Ad not loaded")
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed full screen content")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show full screen content: ${adError.message}")
                rewardedAd = null
                onAdFailedToShow?.invoke(adError.message)
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed full screen content")
                rewardedAd = null
                onAdDismissed?.invoke()
                // Preload next ad
                loadRewardedAd(activity)
            }
        }

        ad.show(activity) { rewardItem ->
            val rewardAmount = rewardItem.amount
            val rewardType = rewardItem.type
            Log.d(TAG, "User earned reward: $rewardAmount $rewardType")
            onUserEarnedReward?.invoke()
        }
    }

    /**
     * Check if ad is loaded
     */
    fun isAdLoaded(): Boolean {
        return rewardedAd != null
    }

    /**
     * Preload ad for future use
     */
    fun preloadAd(activity: Activity) {
        if (rewardedAd == null && !isAdLoading) {
            loadRewardedAd(activity)
        }
    }
}