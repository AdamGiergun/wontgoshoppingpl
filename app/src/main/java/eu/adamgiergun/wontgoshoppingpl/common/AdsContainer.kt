package eu.adamgiergun.wontgoshoppingpl.common

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import eu.adamgiergun.wontgoshoppingpl.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal object AdsContainer {
    private const val TAG = "WGS_Ads"

    @Volatile
    private var initialized = false
    private var interstitialAd: InterstitialAd? = null

    /**
     * Zgoda UMP musi zostać zebrana zanim wystartuje MobileAds. Formularz pokazuje
     * się tylko tam, gdzie wymaga tego prawo (EOG/UK) i tylko dopóki użytkownik nie
     * podejmie decyzji. Błędy nie blokują aplikacji — reklamy po prostu się nie
     * pojawią, dopóki `canRequestAds()` nie zwróci true.
     */
    private suspend fun requestConsentIfNeeded(activity: Activity): Boolean =
        suspendCancellableCoroutine { continuation ->
            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            consentInformation.requestConsentInfoUpdate(
                activity,
                ConsentRequestParameters.Builder().build(),
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        formError?.let { Log.w(TAG, "UMP form: ${it.message}") }
                        if (continuation.isActive) continuation.resume(consentInformation.canRequestAds())
                    }
                },
                { requestError ->
                    Log.w(TAG, "UMP update: ${requestError.message}")
                    if (continuation.isActive) continuation.resume(consentInformation.canRequestAds())
                }
            )
        }

    private suspend fun initializeMobileAds(applicationContext: Context) {
        if (initialized) return
        initialized = true
        // MobileAds.initialize wykonuje I/O dyskowe i sieciowe — nie na głównym wątku.
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                MobileAds.initialize(applicationContext) {
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        }
    }

    /**
     * Pełna ścieżka startowa reklam: zgoda -> inicjalizacja SDK -> baner -> interstitial.
     * Zwraca utworzony baner (do zwolnienia w onDestroy) albo null, gdy użytkownik
     * nie wyraził zgody i reklam nie wolno pobierać.
     */
    suspend fun start(activity: Activity, adViewContainer: ViewGroup): AdView? {
        if (!requestConsentIfNeeded(activity)) return null
        initializeMobileAds(activity.applicationContext)
        return createBannerAdView(activity).also {
            adViewContainer.addView(it)
            initializeInterstitialAd(activity.applicationContext)
        }
    }

    /**
     * Baner adaptacyjny zamiast wycofanego SMART_BANNER — wysokość dobierana do
     * szerokości okna, więc działa też po obrocie i na dużych ekranach.
     *
     * Wariant "large" jest następcą zwykłego banera zakotwiczonego, który
     * (razem z odmianami portrait/landscape) jest w SDK 25 przestarzały.
     *
     * Slot ma rozmiar, o który poprosiliśmy; kreację dobiera serwer i bywa
     * mniejsza, a różnicę SDK zamalowuje. Zmiana wariantu tego nie usuwa —
     * sprawdzone: przy zwykłym czerń przenosi się z góry i dołu na boki, bo
     * kreacje testowe mają sztywne rozmiary IAB (320x50, 468x60, 728x90)
     * i nie skalują się do slotu. Reklamy produkcyjne slot wypełniają.
     */
    private fun createBannerAdView(activity: Activity): AdView {
        val displayMetrics = activity.resources.displayMetrics
        // Ramka banera jest w layoucie ograniczona osobnym limitem, żeby nie
        // ciągnęła się przez całą szerokość ekranu w poziomie. Rozmiar trzeba
        // liczyć z tej samej wartości — inaczej prosilibyśmy o baner szerszy niż
        // miejsce, w które trafi, i SDK dobrałby wysokość do szerokości,
        // której nie ma.
        val maxWidthPx = activity.resources.getDimensionPixelSize(R.dimen.ad_max_width)
        val adWidthDp = (minOf(displayMetrics.widthPixels, maxWidthPx) / displayMetrics.density).toInt()
        return AdView(activity).apply {
            adUnitId = activity.getString(R.string.main_banner_ad_id)
            setAdSize(AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, adWidthDp))
            // Slot ma rozmiar, o który poprosiliśmy, a kreacja bywa mniejsza —
            // reszta slotu to tło AdView, domyślnie czarne. Przezroczyste pokazuje
            // w tym miejscu tło aplikacji zamiast czarnej ramki wokół reklamy.
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            loadAd(AdRequest.Builder().build())
        }
    }

    fun initializeInterstitialAd(applicationContext: Context) {
        if (!initialized || interstitialAd != null) return
        InterstitialAd.load(
            applicationContext,
            applicationContext.getString(R.string.interstitial_ad_id),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "InterstitialAd was loaded")
                    interstitialAd = ad.apply { fullScreenContentCallback = contentCallback }
                }
            }
        )
    }

    fun getInterstitialAd(applicationContext: Context): InterstitialAd? {
        if (interstitialAd == null) initializeInterstitialAd(applicationContext)
        return interstitialAd
    }

    private val contentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Log.d(TAG, "InterstitialAd was dismissed.")
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            Log.d(TAG, "InterstitialAd failed to show.")
        }

        override fun onAdShowedFullScreenContent() {
            Log.d(TAG, "InterstitialAd showed fullscreen content.")
            interstitialAd = null
        }
    }
}
