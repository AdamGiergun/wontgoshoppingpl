package eu.adamgiergun.wontgoshoppingpl.settings

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithThemeAndPermissions

internal class SettingsActivity :
        ActivityWithThemeAndPermissions(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val getLayout: Int
        get() = if (mSettingsViewModel.isScreenWideEnough) R.layout.settings_activity_large else R.layout.settings_activity

    private var startFragmentId: String? = null

    private val mSettingsViewModel: SettingsViewModel by lazy {
        mViewModel as SettingsViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, false)
        setContentView(getLayout)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        if (savedInstanceState == null) {
            startFragmentId = intent.getStringExtra(SettingsViewModel.START_FRAGMENT_ID_TAG)
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                onScreenWideEnoughChanged()
            }
        }
        title = mSettingsViewModel.geTitle()
        mSettingsViewModel.askingForPermissionsNeeded.observe(this, {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestCalendarPermissions()
            }
        })
    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        onScreenWideEnoughChanged()
//    }

    private fun onScreenWideEnoughChanged() {
        if (mSettingsViewModel.hasScreenWideEnoughChanged()) {
            setContentView(getLayout)
            //title = mSettingsViewModel.geTitle()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat?, pref: Preference?): Boolean {
        pref?.let { preference ->
            val navController = caller?.findNavController()
            if (preference.fragment == LookPreferenceFragment::class.java.name) {
                if (mSettingsViewModel.isScreenWideEnough) {
                    navController?.navigate(NotificationPreferenceFragmentDirections.actionNotificationPreferenceFragmentToLookPreferenceFragment())
                } else {
                    navController?.navigate(HeadersPreferenceFragmentDirections.actionHeadersPreferenceFragmentToLookPreferenceFragment())
                }
            } else {
                if (mSettingsViewModel.isScreenWideEnough) {
                    navController?.navigate(LookPreferenceFragmentDirections.actionLookPreferenceFragmentToNotificationPreferenceFragment())
                } else {
                    navController?.navigate(HeadersPreferenceFragmentDirections.actionHeadersPreferenceFragmentToNotificationPreferenceFragment())
                }
            }
        }
        return true
    }

//    override fun onSupportNavigateUp(): Boolean {
//        if (supportFragmentManager.popBackStackImmediate()) {
//            mSettingsViewModel.setLastFragmentIdHeaders()
//            title = mSettingsViewModel.geTitle()
//            return true
//        }
//        return super.onSupportNavigateUp()
//    }
}