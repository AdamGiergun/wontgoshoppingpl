package eu.adamgiergun.wontgoshoppingpl.main.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.*
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity
import eu.adamgiergun.wontgoshoppingpl.main.objects.MainActivityActions
import eu.adamgiergun.wontgoshoppingpl.settings.NotificationPreferenceFragment
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity
import kotlinx.coroutines.launch

internal class MainViewModel(application: Application) : AppViewModel(application) {

    private val _isInitialLookChosen = MutableLiveData<Boolean>()
    val isInitialLookChosen : LiveData<Boolean>
        get() = _isInitialLookChosen

    private val _isWidgetInfoButtonClicked = MutableLiveData<Boolean>()
    val isWidgetInfoButtonClicked : LiveData<Boolean>
        get() = _isWidgetInfoButtonClicked

    fun onWidgetInfoButtonClick(@Suppress("UNUSED_PARAMETER") view: View) {
        _isWidgetInfoButtonClicked.value = true
    }

    private val isFirstRun: Boolean
        get() = AppPreferences.isFirstRun(appContext)

    init {
        if (isFirstRun)
            viewModelScope.launch { AppDatabase.getInstance(getApplication()).eventDao().initialize() }
    }

    val nextAction: LiveData<MainActivity.Actions>
        get() = MainActivityActions.nextAction
    fun setNextAction(action: MainActivity.Actions) {
        MainActivityActions.setNextAction(action)
    }

    val isCalendarEnabledAndChosen: Boolean
        get() = AppPreferences.isUseGoogleCalendarSet(appContext) && AppPreferences.isCalendarSet(appContext)

    val mainNavigationId: Int
        get() = when {
            isFirstRun -> R.navigation.main_navigation_first_run_widget_info
            !AppPreferences.wasAppWidgetInfoDisplayed(appContext) -> R.navigation.main_navigation_widget_info
            else -> R.navigation.main_navigation
        }

    val wasWidgetInfoDisplayed
        get() = AppPreferences.wasAppWidgetInfoDisplayed(appContext)

    fun getAdRequest(context: Context): AdRequest {
        return AdsContainer.getAdRequest(context)
    }

    fun initializeInterstitialAd(applicationContext: Context) {
        AdsContainer.initializeInterstitialAd(applicationContext)
    }

    fun getSettingsActivityIntentAtNotifications(): Intent {
        return Intent(appContext, SettingsActivity::class.java).apply {
            putExtra(START_FRAGMENT_ID_TAG, NotificationPreferenceFragment::class.java.name)
        }
    }

    fun onInitialLookChoice(lookButtonId: Int) {
        isLookRich = (lookButtonId == R.id.imageButtonRich)
        _isInitialLookChosen.value = true
        setNotFirstRun()
        _isInitialLookChosen.value = false
    }

    fun setAppWidgetInfoDisplayed() {
        AppPreferences.setAppWidgetInfoDisplayed(appContext)
    }

    private fun setNotFirstRun() {
        AppPreferences.setFirstRun(appContext, false)
    }
}