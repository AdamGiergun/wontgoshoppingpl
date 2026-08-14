package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.widget.Widget
import kotlinx.coroutines.launch

internal class LookPreferenceFragment : SettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_look, rootKey)
    }

    /**
     * Zmiana zestawu kolorystycznego wymaga odtworzenia aktywności, bo zestaw
     * jest motywem, a motyw rozstrzyga się przy tworzeniu okna — patrz
     * `ActivityWithThemeAndPermissions.onCreate`.
     *
     * Gęstość listy odtworzenia NIE wymaga: podchwytuje ją lista przy powrocie.
     *
     * Widget odświeżamy jawnie, bo sam z siebie odczekałby do sześciu godzin,
     * a broadcastu APPWIDGET_UPDATE nie da się wysłać — jest chroniony.
     *
     * Odtworzenie musi być W ŚRODKU korutyny, po odświeżeniu. Wersja wołająca
     * `recreate()` obok `launch` przegrywała wyścig: odtworzenie niszczy
     * fragment, a to anuluje `lifecycleScope`, więc odświeżenie widgetu ginęło
     * po drodze i widget zostawał w poprzednim zestawie aż do własnej
     * aktualizacji — czyli nawet kilka godzin.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != getString(R.string.colour_set_preference_key)) return
        val appContext = requireContext().applicationContext
        lifecycleScope.launch {
            Widget.refresh(appContext)
            requireActivity().recreate()
        }
    }
}
