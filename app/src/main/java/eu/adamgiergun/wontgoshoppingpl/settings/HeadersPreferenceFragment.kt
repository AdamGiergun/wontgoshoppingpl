package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import eu.adamgiergun.wontgoshoppingpl.R

internal class HeadersPreferenceFragment : SettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_headers)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        preference?.order?.let { highlightLeftTreePreference(it) }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    private fun highlightLeftTreePreference(position: Int) {
        for (i in 0 until listView.childCount) {
            listView.getChildAt(i).setBackgroundColor(
                    if (i == position) Color.LTGRAY else Color.TRANSPARENT)
        }
    }
}