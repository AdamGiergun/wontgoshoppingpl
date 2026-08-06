package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import eu.adamgiergun.wontgoshoppingpl.BuildConfig
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.db.DaysPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Pobiera daty dni handlowych i niehandlowych z Firebase Remote Config.
 *
 * Dane wbudowane w aplikację (`res/raw/days.json`, wstawiane przy tworzeniu bazy)
 * są tylko punktem startowym. Dzięki temu kolejny rok albo zmiana przepisów nie
 * wymaga wydania nowej wersji w sklepie — wystarczy podmienić parametr
 * `days_data` w konsoli Firebase.
 *
 * Remote Config sam trzyma pobrane wartości na dysku, więc brak sieci przy
 * starcie nie jest problemem: użyte zostaną ostatnie znane dane albo, na czystej
 * instalacji, te z zasobów.
 */
internal object DaysRepository {

    private const val TAG = "WGS_Days"

    // Dane zmieniają się raz na rok, więc w wydaniu nie ma po co odpytywać
    // częściej niż raz na dobę. W debugu bez throttlingu, żeby dało się testować.
    private const val FETCH_INTERVAL_SECONDS = 24L * 60 * 60

    suspend fun refresh(appContext: Context) {
        val json = withContext(Dispatchers.IO) { fetch() } ?: return
        val events = DaysPayload.parse(json)
        if (events.isEmpty()) {
            // Payload nie do sparsowania albo pusty — zostawiamy to, co już jest w bazie.
            return
        }
        val changed = AppDatabase.getInstance(appContext).eventDao().upsertAll(events)
        if (changed > 0) Log.d(TAG, "Zaktualizowano $changed wpisów z Remote Config")
    }

    private suspend fun fetch(): String? = suspendCancellableCoroutine { continuation ->
        val config = FirebaseRemoteConfig.getInstance()
        config.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(
                    if (BuildConfig.DEBUG) 0 else FETCH_INTERVAL_SECONDS
                )
                .build()
        )
        config.fetchAndActivate().addOnCompleteListener { task ->
            if (!continuation.isActive) return@addOnCompleteListener
            if (task.isSuccessful) {
                continuation.resume(
                    config.getString(DaysPayload.REMOTE_CONFIG_KEY).takeIf { it.isNotBlank() }
                )
            } else {
                Log.w(TAG, "Remote Config niedostępny: ${task.exception?.message}")
                continuation.resume(null)
            }
        }
    }
}
