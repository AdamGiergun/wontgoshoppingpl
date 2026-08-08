package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import com.google.android.material.color.MaterialColors
import eu.adamgiergun.wontgoshoppingpl.R

internal class HeadersPreferenceFragment : SettingsFragment() {

    private val settingsViewModel
        get() = ViewModelProvider(requireActivity())[SettingsViewModel::class.java]

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_headers)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        settingsViewModel.setSelectedHeader(preference.order)
        podswietlWybranyNaglowek()
        return super.onPreferenceTreeClick(preference)
    }

    override fun onResume() {
        super.onResume()
        // Podświetlenie ginęło przy odtworzeniu fragmentu — po obrocie ekranu
        // prawy panel pokazywał wybrany dział, a lewy nie zaznaczał żadnego.
        podswietlWybranyNaglowek()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    /**
     * Zaznaczenie działu w panelu lewym; widoczne tylko w układzie dwupanelowym,
     * bo w jednopanelowym kliknięcie od razu zabiera z tego ekranu.
     *
     * Kolor idzie z roli motywu zamiast dawnego [Color.LTGRAY], który w trybie
     * ciemnym był jaśniejszy od tekstu.
     *
     * Nadal chodzimy po dzieciach listy, a nie po adapterze: lista ma dwie
     * pozycje i nigdy się nie przewija, więc indeks dziecka to zarazem pozycja.
     * Przy dłuższej liście trzeba by to przepisać na własny układ preferencji
     * ze stanem `activated`.
     */
    private fun podswietlWybranyNaglowek() {
        // W układzie jednopanelowym lista nagłówków jest nawigacją, a nie wyborem —
        // zaznaczenie sugerowałoby stan, którego nie ma.
        if (!settingsViewModel.isScreenWideEnough) return
        val wybrany = settingsViewModel.selectedHeader
        val kolor = MaterialColors.getColor(
            requireView(),
            com.google.android.material.R.attr.colorSecondaryContainer
        )
        listView.post {
            for (i in 0 until listView.childCount) {
                listView.getChildAt(i)
                    .setBackgroundColor(if (i == wybrany) kolor else Color.TRANSPARENT)
            }
        }
    }
}
