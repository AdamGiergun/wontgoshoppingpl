package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import eu.adamgiergun.wontgoshoppingpl.settings.IntegerDialogPreference
import eu.adamgiergun.wontgoshoppingpl.settings.IntegerPreferenceDialogFragmentCompat
import eu.adamgiergun.wontgoshoppingpl.settings.TimePreference
import eu.adamgiergun.wontgoshoppingpl.R

internal class NotificationPreferenceFragment : SettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        ViewModelProvider(requireActivity())[SettingsViewModel::class.java].run {
            // Ekran trzeba ustawić od razu — bez niego PreferenceFragmentCompat
            // nie ma czego pokazać, a obserwator nie wystartuje, dopóki
            // allPermissionsGranted nie ma wartości.
            var poprzednieUprawnienia = allPermissionsGranted.value == true
            ustawEkran(poprzednieUprawnienia, rootKey)
            if (!poprzednieUprawnienia) askingForPermissionsNeeded(true)

            allPermissionsGranted.observe(this@NotificationPreferenceFragment) { uprawnienia ->
                // Obserwator emituje bieżącą wartość natychmiast po podpięciu,
                // więc bez tego warunku ekran ładował się dwa razy z rzędu.
                if (uprawnienia == poprzednieUprawnienia) return@observe
                poprzednieUprawnienia = uprawnienia
                ustawEkran(uprawnienia, rootKey)
                if (!uprawnienia) askingForPermissionsNeeded(true)
            }
        }
    }

    private fun ustawEkran(uprawnieniaPrzyznane: Boolean, rootKey: String?) {
        setPreferencesFromResource(
            if (uprawnieniaPrzyznane) R.xml.pref_notification else R.xml.pref_notification_disabled,
            rootKey
        )
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(eu.adamgiergun.wontgoshoppingpl.R.string.use_google_calendar_preference_key)) {
            requireActivity().recreate()
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        preference.let {
            // TimePreference pokazuje MaterialTimePicker, a nie własny
            // PreferenceDialogFragmentCompat: platformowy TimePickerDialog brał
            // wygląd z motywu systemu i po przejściu na Material 3 odstawał od
            // reszty aplikacji tak samo jak selektory na ekranie głównym.
            if (preference is TimePreference) {
                pokazSelektorGodziny(preference)
                return
            }
            val dialogFragment = when (preference) {
                is IntegerDialogPreference -> IntegerPreferenceDialogFragmentCompat()
                else -> null
            }
            dialogFragment?.let {
                it.arguments = Bundle(1).apply { putString("key", preference.key) }
                // setTargetFragment jest przestarzałe, ale androidx.preference 1.2.1
                // wciąż go wymaga: PreferenceDialogFragmentCompat.onCreate woła
                // getTargetFragment() i rzuca wyjątkiem, jeśli nie jest ustawiony.
                @Suppress("DEPRECATION")
                it.setTargetFragment(this, 0)
                it.show(parentFragmentManager, preference.javaClass.name)
                return
            }
        }
        super.onDisplayPreferenceDialog(preference)
    }

    private fun pokazSelektorGodziny(preference: TimePreference) {
        val czas = preference.time
        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            // Tarcza, nie klawiatura: MaterialTimePicker sam przechodzi na wpisywanie
            // z klawiatury, gdy uzna, że jest za mało miejsca.
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(czas.hour)
            .setMinute(czas.minute)
            .setTitleText(preference.dialogTitle ?: preference.title)
            .build()
            .also { selektor ->
                polacz(selektor, preference)
                selektor.show(parentFragmentManager, TAG_SELEKTORA_GODZINY)
            }
    }

    /**
     * Po obrocie ekranu menedżer fragmentów odtwarza selektor bez nasłuchów —
     * tak samo jak na ekranie głównym, patrz ReminderTimeDialog.
     */
    private fun podepnijSelektorGodzinyPonownie() {
        val selektor = parentFragmentManager
            .findFragmentByTag(TAG_SELEKTORA_GODZINY) as? MaterialTimePicker ?: return
        (findPreference<TimePreference>(getString(R.string.default_reminder_time_preference_key)))
            ?.let { polacz(selektor, it) }
    }

    private fun polacz(selektor: MaterialTimePicker, preference: TimePreference) {
        selektor.addOnPositiveButtonClickListener {
            preference.setTime(selektor.hour, selektor.minute)
        }
    }

    override fun onResume() {
        super.onResume()
        podepnijSelektorGodzinyPonownie()
    }

    private companion object {
        const val TAG_SELEKTORA_GODZINY = "default_reminder_time_picker"
    }

// TODO ChooseCalendarsForCleaning
//        @Override
//        public boolean onPreferenceTreeClick(Preference preference) {
//            if (preference.getKey().equals(getString(R.string.key_delete_events))) {
//                DialogFragment dialogFragment = new ChooseCalendarsForCleaning();
//                if (this.getFragmentManager() != null) {
//                    dialogFragment.show(this.getFragmentManager(), "test");
//                }
//                return true;
//            } else {
//                return super.onPreferenceTreeClick(preference);
//            }
//        }
}