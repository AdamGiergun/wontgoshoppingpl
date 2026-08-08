package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat

/**
 * Wspólna obsługa nasłuchu zmian preferencji.
 *
 * Klasa nadpisywała wcześniej `onCreateAdapter`, żeby własnym potomkiem
 * `PreferenceGroupAdapter` (klasy oznaczonej @RestrictedApi) ukrywać ramkę ikony
 * przy preferencjach bez ikony i zerować wcięcie nagłówków kategorii. Obie
 * rzeczy da się dziś powiedzieć w XML-u: pierwszą przez
 * `app:iconSpaceReserved="false"`, drugą załatwia sam motyw Material 3.
 *
 * Przy okazji zniknęło rekurencyjne przechodzenie po drzewie widoków przy każdym
 * wiązaniu wiersza i martwe sprawdzenie `Build.VERSION_CODES.JELLY_BEAN_MR1`,
 * które przy minSdk 24 zawsze było prawdziwe.
 */
abstract class BasePreferenceFragmentCompat : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onPause() {
        super.onPause()
        preferenceScreen
            .sharedPreferences
            ?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen
            .sharedPreferences
            ?.registerOnSharedPreferenceChangeListener(this)
    }
}
