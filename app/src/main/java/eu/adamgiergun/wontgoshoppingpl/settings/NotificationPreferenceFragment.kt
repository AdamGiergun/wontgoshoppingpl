package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import eu.adamgiergun.wontgoshoppingpl.settings.IntegerDialogPreference
import eu.adamgiergun.wontgoshoppingpl.settings.IntegerPreferenceDialogFragmentCompat
import eu.adamgiergun.wontgoshoppingpl.settings.TimePreference
import eu.adamgiergun.wontgoshoppingpl.settings.TimePreferenceDialogFragmentCompat
import eu.adamgiergun.wontgoshoppingpl.R

internal class NotificationPreferenceFragment : SettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        ViewModelProvider(requireActivity())[SettingsViewModel::class.java].run {
            if (allPermissionsGranted.value == true) {
                setPreferencesFromResource(R.xml.pref_notification, rootKey)
            } else {
                setPreferencesFromResource(R.xml.pref_notification_disabled, rootKey)
                askingForPermissionsNeeded(true)
            }

            allPermissionsGranted.observe(this@NotificationPreferenceFragment) { allPermissionsGranted ->
                setPreferencesFromResource(
                    if (allPermissionsGranted) R.xml.pref_notification else R.xml.pref_notification_disabled,
                    rootKey
                )
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(eu.adamgiergun.wontgoshoppingpl.R.string.use_google_calendar_preference_key)) {
            requireActivity().recreate()
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        preference.let {
            val dialogFragment = when (preference) {
                is IntegerDialogPreference -> IntegerPreferenceDialogFragmentCompat()
                is TimePreference -> TimePreferenceDialogFragmentCompat()
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