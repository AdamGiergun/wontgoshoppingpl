package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences
import android.os.Bundle
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.areFiltersChanged
import eu.adamgiergun.wontgoshoppingpl.common.isLookChanged

internal class LookPreferenceFragment : SettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_look, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.app_look_preference_key) -> {
                isLookChanged = true
                requireActivity().recreate()
            }
            getString(R.string.key_app_widget_text_size) -> Unit
            else -> areFiltersChanged = true
        }
    }
}