package eu.adamgiergun.wontgoshoppingpl.settings

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel

internal class SettingsViewModel(application: Application) : AppViewModel(application) {

    private var _settingsActivityBarTitle = MutableLiveData<String>()
    val settingsActivityBarTitle: LiveData<String>
        get() = _settingsActivityBarTitle

    fun setActivityBarTitle(title: String) {
        _settingsActivityBarTitle.value = title
    }

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

    init {
        lastIsScreenWideEnough = isScreenWideEnough
    }

    companion object {
        const val START_FRAGMENT_ID_TAG = AppViewModel.START_FRAGMENT_ID_TAG
    }
}
