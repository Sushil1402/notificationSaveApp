package com.notistorex.app.ads

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.nativead.AdChoicesView
import com.notistorex.app.R

@Composable
fun HomeScreenNotificationGroupCardAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd: NativeAd? = null

    DisposableEffect(Unit) {
        onDispose { nativeAd?.destroy() }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 🔗 Place Native AdView here
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    val adView = LayoutInflater.from(ctx)
                        .inflate(R.layout.native_ads_notification_group_card, null) as NativeAdView

                    val headline = adView.findViewById<TextView>(R.id.ad_headline)
                    val body = adView.findViewById<TextView>(R.id.ad_body)
                    val cta = adView.findViewById<Button>(R.id.ad_call_to_action)
                    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
                    val attribution = adView.findViewById<TextView>(R.id.ad_attribution)
                    val adChoices = adView.findViewById<AdChoicesView>(R.id.ad_choices_view)

                    adView.mediaView = mediaView
                    adView.headlineView = headline
                    adView.bodyView = body
                    adView.callToActionView = cta
                    adView.advertiserView = attribution
                    adView.adChoicesView = adChoices

                    val adLoader = AdLoader.Builder(
                        ctx,
                        "ca-app-pub-3940256099942544/2247696110"
                    ).forNativeAd { ad ->
                        nativeAd = ad

                        (adView.headlineView as TextView).text = ad.headline
                        (adView.bodyView as TextView).text = ad.body
                        (adView.callToActionView as Button).apply {
                            text = ad.callToAction
                            visibility = if (ad.callToAction != null)
                                View.VISIBLE else View.INVISIBLE
                        }

                        mediaView.mediaContent = ad.mediaContent
                        adView.setNativeAd(ad)
                    }.build()

                    adLoader.loadAd(AdRequest.Builder().build())

                    adView
                }
            )
        }
    }
}

@Composable
fun BannerAD(modifier: Modifier = Modifier){

    Box(modifier = modifier.fillMaxWidth()){
        AndroidView( modifier =Modifier,factory = {context->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }){}
    }
}



/**
 * Composable helper to remember RewardedAdManager instance
 */
@Composable
fun rememberRewardedAdManager(): RewardedAdManager {
    return remember { RewardedAdManager() }
}

@Preview
@Composable
private fun AdsComponentsView() {
    HomeScreenNotificationGroupCardAd()
}
