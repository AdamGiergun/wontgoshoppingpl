package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences
import android.os.Bundle
import eu.adamgiergun.mykotlinlibrary.BasePreferenceFragmentCompat
import eu.adamgiergun.wontgoshoppingpl.R

internal class LookPreferenceFragment : BasePreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_look, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.app_look_preference_key)) {
            requireActivity().recreate()
        }
    }
}