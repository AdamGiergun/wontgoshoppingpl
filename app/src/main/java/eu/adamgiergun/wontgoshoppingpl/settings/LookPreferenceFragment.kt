package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences
import android.os.Bundle
import eu.adamgiergun.wontgoshoppingpl.R

internal class LookPreferenceFragment : SettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_look, rootKey)
    }

    /**
     * Gęstość listy nie wymaga już przeładowania ekranu — dawny wybór „rich"
     * podmieniał motyw aktywności, więc trzeba było ją odtwarzać. Teraz zmianę
     * podchwytuje lista przy powrocie do niej.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) = Unit
}