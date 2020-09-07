package eu.adamgiergun.wontgoshoppingpl.settings

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class StartedPreference private constructor(private val callerFragment: PreferenceFragmentCompat, private val preference: Preference) {
    val pref: Preference
        get() = preference
    val caller
        get() = callerFragment

    companion object {
        @JvmStatic
        fun newInstance(caller: PreferenceFragmentCompat, preference: Preference): StartedPreference {
            return StartedPreference(caller, preference)
        }
    }
}