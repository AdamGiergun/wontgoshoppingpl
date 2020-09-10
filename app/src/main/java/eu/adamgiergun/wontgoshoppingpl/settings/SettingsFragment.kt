package eu.adamgiergun.wontgoshoppingpl.settings

import androidx.preference.PreferenceFragmentCompat
import java.util.*

class SettingsFragment private constructor(val fragment: PreferenceFragmentCompat?, val containerId: Int, val addToBackStack: Boolean) {
    internal class List : ArrayList<SettingsFragment?>()
    companion object {
        val HEADERS: String = HeadersPreferenceFragment::class.java.name
        val LOOK: String = LookPreferenceFragment::class.java.name
        val NOTIFICATIONS: String = NotificationPreferenceFragment::class.java.name

        fun newInstance(fragmentId: String, containerId: Int, addToBackStack: Boolean): SettingsFragment {
            return SettingsFragment(newFragment(fragmentId), containerId, addToBackStack)
        }

        private fun newFragment(fragmentId: String): PreferenceFragmentCompat? {
            return if (fragmentId == HEADERS) HeadersPreferenceFragment() else if (fragmentId == LOOK) LookPreferenceFragment() else if (fragmentId == NOTIFICATIONS) NotificationPreferenceFragment() else null
        }

        fun getStartFragmentId(startFromNotifications: Boolean): String {
            return if (startFromNotifications) NOTIFICATIONS else HEADERS
        }
    }
}