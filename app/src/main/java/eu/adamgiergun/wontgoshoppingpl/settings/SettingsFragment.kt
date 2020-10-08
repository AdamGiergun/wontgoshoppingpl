package eu.adamgiergun.wontgoshoppingpl.settings

import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.mykotlinlibrary.BasePreferenceFragmentCompat

internal abstract class SettingsFragment: BasePreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        ViewModelProvider(requireActivity())
                .get(SettingsViewModel::class.java)
                .setTitle(this::class.java.name)
    }
}