package eu.adamgiergun.wontgoshoppingpl.settings

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel

internal class SettingsViewModel(application: Application) : AppViewModel(application) {

    var intentAlreadyUsed = false

    private var _settingsActivityBarTitle = MutableLiveData<String>()
    val settingsActivityBarTitle: LiveData<String>
        get() = _settingsActivityBarTitle

    private val settingsTitle = appContext.getString(R.string.title_activity_settings)
    private val lookTitle = appContext.getString(R.string.pref_header_look)
    private val notificationsTitle = appContext.getString(R.string.pref_header_notifications)

    private var lastIsScreenWideEnough: Boolean

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

    fun setTitle(fragmentId: String) {
        when (fragmentId) {
            HeadersPreferenceFragment::class.java.name ->
                if (!isScreenWideEnough) _settingsActivityBarTitle.value = settingsTitle
            LookPreferenceFragment::class.java.name ->
                _settingsActivityBarTitle.value = "$settingsTitle: $lookTitle"
            NotificationPreferenceFragment::class.java.name ->
                _settingsActivityBarTitle.value = "$settingsTitle: $notificationsTitle"
        }
    }

    init {
        lastIsScreenWideEnough = isScreenWideEnough
        _settingsActivityBarTitle.value = settingsTitle
    }

    companion object {
        const val START_FRAGMENT_ID_TAG = AppViewModel.START_FRAGMENT_ID_TAG
    }
}
