package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import eu.adamgiergun.wontgoshoppingpl.R

internal object AdsContainer {
    private var initialized = false
    private var interstitialAd: InterstitialAd? = null

    fun initializeInterstitialAd(applicationContext: Context) {
        if (interstitialAd == null)
            interstitialAd = InterstitialAd(applicationContext).apply {
                adUnitId = applicationContext.getString(R.string.interstitial_ad_id)
                loadAd(AdRequest.Builder().build())
            }
    }

    fun getInterstitialAd(applicationContext: Context): InterstitialAd? {
        return interstitialAd.run {
            when {
                this == null -> {
                    initializeInterstitialAd(applicationContext)
                    null
                }
                isLoaded ->
                    this
                !isLoading -> {
                    loadAd(AdRequest.Builder().build())
                    null
                }
                else ->
                    null
            }
        }
    }

    fun getBannerAd(context: Context): AdRequest {
        if (!initialized) {
            MobileAds.initialize(context)
            initialized = true
        }
        return AdRequest.Builder().build()
    }
}