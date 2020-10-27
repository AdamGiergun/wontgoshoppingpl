package eu.adamgiergun.wontgoshoppingpl.main.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.*
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity
import eu.adamgiergun.wontgoshoppingpl.main.objects.DialogParameters
import eu.adamgiergun.wontgoshoppingpl.main.objects.IsDbChanged
import eu.adamgiergun.wontgoshoppingpl.main.objects.LastTouchedDay
import eu.adamgiergun.wontgoshoppingpl.main.objects.MainActivityActions
import eu.adamgiergun.wontgoshoppingpl.settings.NotificationPreferenceFragment
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity
import kotlinx.coroutines.launch

internal class MainViewModel(application: Application) : AppViewModel(application) {

    private val _days = MutableLiveData<List<Day>>()
    val days: LiveData<List<Day>>
        get() = _days

    val nextAction: LiveData<MainActivity.Actions>
        get() = MainActivityActions.nextAction
    fun setNextAction(action: MainActivity.Actions) {
        MainActivityActions.setNextAction(action)
    }

    val isDbChanged
        get() = IsDbChanged.value
    val lastActionIsAddList
        get() = MainActivityActions.lastActionIsAddList
    val lastTouchedPosition
        get() = LastTouchedDay.position

    fun setLastTouchedItem(day: Day?, position: Int, isDbChanged: Boolean) {
        day?.let {
            LastTouchedDay.value = it
            MainActivityActions.setNextAction(MainActivity.Actions.CHECK_PERMISSIONS_DAY)
        }
        LastTouchedDay.position = position
        if (isDbChanged) IsDbChanged.set(true)
    }

    var density: Float = 0F

    private lateinit var dayTypesToBeDisplayed: Day.Type.Set

    val isCalendarEnabledAndChosen: Boolean
        get() = AppPreferences.isUseGoogleCalendarSet(appContext) && AppPreferences.isCalendarSet(appContext)

    val isLookChanged: Boolean
        get() {
            return eu.adamgiergun.wontgoshoppingpl.common.isLookChanged.run {
                if (this) {
                    eu.adamgiergun.wontgoshoppingpl.common.isLookChanged = false
                    resetDayTypes()
                }
                this
            }

        }

    val mainNavigationId: Int
        get() = when {
            isFirstRun -> R.navigation.main_navigation_first_run_widget_info
            !AppPreferences.wasAppWidgetInfoDisplayed(appContext) -> R.navigation.main_navigation_widget_info
            else -> R.navigation.main_navigation
        }

    val wasWidgetInfoDisplayed
        get() = AppPreferences.wasAppWidgetInfoDisplayed(appContext)

    private val isFirstRun: Boolean
        get() = AppPreferences.isFirstRun(appContext)

    init {
        viewModelScope.launch { resetDayTypes() }
    }

    fun resetDayTypes() {
        dayTypesToBeDisplayed = AppPreferences.newDayTypeSet(appContext)
        setDaysAndLastDate()
    }

    fun getBannerAd(context: Context): AdRequest {
        return AdsContainer.getBannerAd(context)
    }

    fun initializeInterstitialAd(applicationContext: Context) {
        AdsContainer.initializeInterstitialAd(applicationContext)
    }

    fun getSettingsActivityIntentAtNotifications(): Intent {
        return Intent(appContext, SettingsActivity::class.java).apply {
            putExtra(START_FRAGMENT_ID_TAG, NotificationPreferenceFragment::class.java.name)
        }
    }

    fun onInitialLookChoice(newIsLookRich: Boolean) {
        isLookRich = newIsLookRich
        setNotFirstRun()
    }

    fun setAppWidgetInfoDisplayed() {
        AppPreferences.setAppWidgetInfoDisplayed(appContext)
    }

    private fun setDaysAndLastDate() {
        val dayTypes = dayTypesToBeDisplayed.map { it.code.toString() }.toList()
        AppDatabase.getInstance(getApplication()).eventDao().run {
            viewModelScope.launch {
                if (isFirstRun) initialize()
                getAllEventsOfTypeNotBefore(dayTypes, today)?.let { list ->
                    _days.value = list.map { Day(it) }
                }

                getLastEvent()?.let {
                    val lastDay = Day(it)
                    DialogParameters.lastDateInMillis = lastDay.dateInMillis
                }
            }
        }

    }

    private fun setNotFirstRun() {
        AppPreferences.setFirstRun(appContext, false)
    }

    //region Firebase log
    fun logLook() {
        FirebaseEvent.logAppTheme(appContext, isLookRich)
    }
    //endregion
}