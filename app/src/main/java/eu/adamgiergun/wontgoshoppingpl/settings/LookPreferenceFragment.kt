package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.mykotlinlibrary.BasePreferenceFragmentCompat
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity

class LookPreferenceFragment : BasePreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_look, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == getString(R.string.app_look_preference_key)) {
            Intent(activity, MainActivity::class.java).run {
                settingsViewModel.setLastFragmentIdHeaders()
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        }
    }

    private val settingsViewModel: SettingsViewModel
        get() {
            return ViewModelProvider(requireActivity()).get(SettingsViewModel::class.java)
        }
}