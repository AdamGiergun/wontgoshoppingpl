package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import eu.adamgiergun.wontgoshoppingpl.R

internal object AdsContainer {
    private const val TAG = "WGS_InterstitialAd"
    private var initialized = false
    private var interstitialAd: InterstitialAd? = null

    fun initializeInterstitialAd(applicationContext: Context) {
        if (interstitialAd == null) {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                applicationContext,
                applicationContext.getString(R.string.interstitial_ad_id),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(TAG, adError.message)
                        interstitialAd = null
                    }

                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "InterstitialAd was loaded")
                        interstitialAd = ad
                    }
                }
            )
        }
    }

    fun getInterstitialAd(applicationContext: Context): InterstitialAd? {
        if (interstitialAd == null) {
            initializeInterstitialAd(applicationContext)
        }
        interstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "InterstitialAd was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.d(TAG, "InterstitialAd failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "InterstitialAd showed fullscreen content.")
                interstitialAd = null
            }
        }
        return interstitialAd
    }

    fun getAdRequest(context: Context): AdRequest {
        if (!initialized) {
            MobileAds.initialize(context)
            initialized = true
        }
        return AdRequest.Builder().build()
    }
}