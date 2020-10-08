package eu.adamgiergun.wontgoshoppingpl.settings

internal abstract class SettingsFragment {

    companion object {
        val LOOK: String = LookPreferenceFragment::class.java.name
        val NOTIFICATIONS: String = NotificationPreferenceFragment::class.java.name
    }
}