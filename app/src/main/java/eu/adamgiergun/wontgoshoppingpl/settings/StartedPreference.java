package eu.adamgiergun.wontgoshoppingpl.settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

class StartedPreference {
    final PreferenceFragmentCompat caller;
    final Preference pref;

    private StartedPreference(PreferenceFragmentCompat caller, Preference pref) {
        this.caller = caller;
        this.pref = pref;
    }

    static StartedPreference newInstance(PreferenceFragmentCompat caller, Preference pref) {
        return new StartedPreference(caller, pref);
    }
}
