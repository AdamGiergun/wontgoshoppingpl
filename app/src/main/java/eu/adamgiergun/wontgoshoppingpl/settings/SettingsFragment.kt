package eu.adamgiergun.wontgoshoppingpl.settings

import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.mykotlinlibrary.BasePreferenceFragmentCompat
import eu.adamgiergun.wontgoshoppingpl.R

internal abstract class SettingsFragment : BasePreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        mSettingsViewModel.apply {
            when (this@SettingsFragment) {
                is HeadersPreferenceFragment ->
                    if (!isScreenWideEnough) setActivityBarTitle(settingsTitle)
                is LookPreferenceFragment ->
                    setActivityBarTitle("$settingsTitle: $lookTitle")
                is NotificationPreferenceFragment ->
                    setActivityBarTitle("$settingsTitle: $notificationsTitle")
            }
        }
    }

    private val mSettingsViewModel
        get() = ViewModelProvider(requireActivity())
                .get(SettingsViewModel::class.java)

    private val settingsTitle
        get() = getString(R.string.title_activity_settings)
    private val lookTitle
        get() = getString(R.string.pref_header_look)
    private val notificationsTitle
        get() = getString(R.string.pref_header_notifications)
}