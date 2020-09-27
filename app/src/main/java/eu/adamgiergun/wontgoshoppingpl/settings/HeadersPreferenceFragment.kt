package eu.adamgiergun.wontgoshoppingpl.settings

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import eu.adamgiergun.wontgoshoppingpl.R

internal class HeadersPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_headers)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        preference?.order?.let { highlightLeftTreePreference(it) }
        return super.onPreferenceTreeClick(preference)
    }

    private fun highlightLeftTreePreference(position: Int) {
        for (i in 0 until listView.childCount) {
            listView.getChildAt(i).setBackgroundColor(
                    if (i == position) Color.LTGRAY else Color.TRANSPARENT)
        }
    }
}