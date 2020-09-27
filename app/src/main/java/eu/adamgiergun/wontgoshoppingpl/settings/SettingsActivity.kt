package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.mylibrary.NoticeDialogFragment.NoticeDialogListener
import eu.adamgiergun.mylibrary.NoticeDialogFragment.NoticeDialogViewListener
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithTheme

internal class SettingsActivity : ActivityWithTheme(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, NoticeDialogListener, NoticeDialogViewListener {

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    private val mSettingsViewModel: SettingsViewModel by lazy {
        ViewModelProvider(this).get(SettingsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, false)
        setContentView(R.layout.settings_activity)
        setupHomeInBar()
        if (savedInstanceState == null) {
            val startFragmentId = intent.getStringExtra(SettingsViewModel.START_FRAGMENT_ID_TAG)
            addFragments(mSettingsViewModel.getInitialFragments(startFragmentId))
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                onScreenWideEnoughChanged()
            }
        }
        title = mSettingsViewModel.geTitle()
    }

    private fun addFragments(fragments: SettingsFragment.List) {
        for (fragment in fragments) {
            fragment?.let {
                val fm = supportFragmentManager
                        .beginTransaction()
                it.fragment?.let { fg -> fm.replace(it.containerId, fg) }
                if (it.addToBackStack) fm.addToBackStack(null)
                fm.commit()
            }

        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onScreenWideEnoughChanged()
    }

    private fun onScreenWideEnoughChanged() {
        if (mSettingsViewModel.hasScreenWideEnoughChanged()) {
            popBackStackCompletely()
            setContentView(R.layout.settings_activity)
            addFragments(mSettingsViewModel.rotatedFragments)
            title = mSettingsViewModel.geTitle()
        }
    }

    private fun popBackStackCompletely() {
        val fragmentManager = supportFragmentManager
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat?, pref: Preference?): Boolean {
        pref?.let { preference ->
            caller?.let { caller ->
                mSettingsViewModel.startedPreference = StartedPreference(caller, preference) } }
        if (mSettingsViewModel.isStartedPreferenceNotNotifications || checkCalendarPermissions()) {
            addStartedFragment()
        }
        return true
    }

    private fun addStartedFragment() {
        mSettingsViewModel.startedPreference?.let {
            mSettingsViewModel.setLastFragmentId(it.preference.fragment)
            // Instantiate the new Fragment
            val args = it.preference.extras
            val fragmentManager = supportFragmentManager
            val fragment = fragmentManager.fragmentFactory.instantiate(
                    classLoader,
                    it.preference.fragment)
            fragment.arguments = args
            fragment.setTargetFragment(it.caller, 0)
            // Replace the existing Fragment with the new Fragment
            val fragmentTransaction = fragmentManager.beginTransaction()
                    .replace(R.id.settings, fragment)
            if (!mSettingsViewModel.isScreenWideEnough) fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
            title = it.preference.title
            mSettingsViewModel.startedPreference = null
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun checkCalendarPermissions(): Boolean {
        return if (mSettingsViewModel.areCalendarPermissionsGranted()) {
            true
        } else {
            requestCalendarPermissions()
            false
        }
    }

    override fun onCalendarPermissionsGranted() {
        super.onCalendarPermissionsGranted()
        addStartedFragment()
    }

    override fun onCalendarPermissionNotGranted() {
        super.onCalendarPermissionNotGranted()
        addEmptyNotificationsFragment()
    }

    private fun addEmptyNotificationsFragment() {
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.fragmentFactory.instantiate(
                classLoader,
                EmptyPreferenceFragment::class.java.name)
        fragment.setTargetFragment(mSettingsViewModel.startedPreference?.caller, 0)
        val fragmentTransaction = fragmentManager.beginTransaction()
                .replace(R.id.settings, fragment)
        if (!mSettingsViewModel.isScreenWideEnough) fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        setTitle(R.string.pref_header_notifications)
        mSettingsViewModel.startedPreference = null
    }

    override fun onDialogPositiveClick(noticeDialogFragment: NoticeDialogFragment) {}
    override fun onDialogNegativeClick(noticeDialogFragment: NoticeDialogFragment) {
        Toast.makeText(applicationContext, R.string.info_cancelled, Toast.LENGTH_SHORT)
                .show()
    }

    override fun onViewClick(stub: NoticeDialogFragment) {
        //stub
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            mSettingsViewModel.setLastFragmentIdHeaders()
            title = mSettingsViewModel.geTitle()
            return true
        }
        return super.onSupportNavigateUp()
    }
}