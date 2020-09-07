package eu.adamgiergun.wontgoshoppingpl.settings

import android.app.Application
import android.content.res.Configuration
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel

internal class SettingsViewModel(application: Application) : AppViewModel(application) {
    private val SETTINGS_TITLE: String
    private val LOOK_TITLE: String
    private val NOTIFICATIONS_TITLE: String
    private var lastIsScreenWideEnough: Boolean
    private var title: CharSequence? = null
    private var lastFragmentId: String? = null
    var startedPreference: StartedPreference? = null
    val isScreenWideEnough: Boolean
        get() = isScreenSizeLarge || isOrientationLandscape
    private val isScreenSizeLarge: Boolean
        get() {
            val screenSize = appContext.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
            return screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }
    private val isOrientationLandscape: Boolean
        get() {
            val orientation = appContext.resources.configuration.orientation
            return orientation == Configuration.ORIENTATION_LANDSCAPE
        }

    fun hasScreenWideEnoughChanged(): Boolean {
        val result = lastIsScreenWideEnough != isScreenWideEnough
        lastIsScreenWideEnough = isScreenWideEnough
        return result
    }

    fun getInitialFragments(newStartFragmentId: String?): SettingsFragment.List {
        var startFragmentId = newStartFragmentId
        if (startFragmentId == null) {
            startFragmentId = if (isScreenWideEnough) SettingsFragment.LOOK else SettingsFragment.HEADERS
        }
        val fragments = SettingsFragment.List()
        fragments.add(SettingsFragment.newInstance(
                SettingsFragment.HEADERS,
                if (isScreenWideEnough) R.id.headers else R.id.settings,
                false))
        if (isScreenWideEnough || startFragmentId != SettingsFragment.HEADERS) {
            lastFragmentId = if (startFragmentId == SettingsFragment.LOOK || startFragmentId == SettingsFragment.HEADERS) SettingsFragment.LOOK else startFragmentId
            fragments.add(SettingsFragment.newInstance(
                    lastFragmentId!!,
                    R.id.settings,
                    !isScreenWideEnough))
        } else {
            lastFragmentId = startFragmentId
        }
        setTitle()
        return fragments
    }

    private fun setTitle() {
        title = if (lastFragmentId == SettingsFragment.LOOK) LOOK_TITLE else if (lastFragmentId == SettingsFragment.NOTIFICATIONS) NOTIFICATIONS_TITLE else SETTINGS_TITLE
    }

    val rotatedFragments: SettingsFragment.List
        get() = getInitialFragments(lastFragmentId)

    fun setLastFragmentId(fragmentId: String?) {
        lastFragmentId = fragmentId
        setTitle()
    }

    fun setLastFragmentIdHeaders() {
        setLastFragmentId(SettingsFragment.HEADERS)
    }

    val isStartedPreferenceNotNotifications: Boolean
        get() = startedPreference!!.pref.fragment != SettingsFragment.NOTIFICATIONS

    fun geTitle(): CharSequence? {
        return title
    }

    init {
        lastIsScreenWideEnough = isScreenWideEnough
        SETTINGS_TITLE = appContext.getString(R.string.title_activity_settings)
        LOOK_TITLE = appContext.getString(R.string.pref_header_look)
        NOTIFICATIONS_TITLE = appContext.getString(R.string.pref_header_notifications)
    }

    companion object {
        const val START_FRAGMENT_ID_TAG = AppViewModel.START_FRAGMENT_ID_TAG
    }
}