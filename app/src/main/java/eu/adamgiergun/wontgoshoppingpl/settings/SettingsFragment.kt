package eu.adamgiergun.wontgoshoppingpl.settings

import androidx.preference.PreferenceFragmentCompat
import java.util.*

internal class SettingsFragment (fragmentId: String, val containerId: Int, val addToBackStack: Boolean) {

    val fragment: PreferenceFragmentCompat?

    init {
        fragment = when (fragmentId) {
            HEADERS -> HeadersPreferenceFragment()
            LOOK -> LookPreferenceFragment()
            NOTIFICATIONS -> NotificationPreferenceFragment()
            else -> null
        }
    }

    class List : ArrayList<SettingsFragment?>()

    companion object {
        val HEADERS: String = HeadersPreferenceFragment::class.java.name
        val LOOK: String = LookPreferenceFragment::class.java.name
        val NOTIFICATIONS: String = NotificationPreferenceFragment::class.java.name

        fun getStartFragmentId(startFromNotifications: Boolean): String {
            return if (startFromNotifications) NOTIFICATIONS else HEADERS
        }
    }
}