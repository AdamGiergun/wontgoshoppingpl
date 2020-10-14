package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import eu.adamgiergun.wontgoshoppingpl.R

internal object InterstitialAdContainer {
    private var interstitialAd: InterstitialAd? = null

    fun initializeAd(applicationContext: Context) {
        if (interstitialAd == null)
            interstitialAd = InterstitialAd(applicationContext).apply {
                adUnitId = applicationContext.getString(R.string.interstitial_ad_id)
                loadAd(AdRequest.Builder().build())
            }
    }

    fun getAd(applicationContext: Context): InterstitialAd? {
        return interstitialAd.run {
            when {
                this == null -> {
                    initializeAd(applicationContext)
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
}