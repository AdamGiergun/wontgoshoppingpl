package eu.adamgiergun.wontgoshoppingpl.main

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleDate
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleReminder
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.*
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.settings.NotificationPreferenceFragment
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity
import kotlinx.coroutines.launch
import java.util.*

internal class MainViewModel(application: Application) : AppViewModel(application) {

    //region LiveData
    private val _days = MutableLiveData<List<Day>>()
    val days: LiveData<List<Day>>
        get() = _days

    private val _isDbChanged = MutableLiveData<Boolean>()
    val isDbChanged: LiveData<Boolean>
        get() = _isDbChanged

    private val _reminderDaysBefore = MutableLiveData<Int>()
    val reminderDaysBefore: LiveData<Int>
        get() = _reminderDaysBefore

    private val _reminderTime = MutableLiveData<SimpleTime>()
    val reminderTime: LiveData<SimpleTime>
        get() = _reminderTime

    private val _tillDate = MutableLiveData<SimpleDate>()
    val tillDate: LiveData<SimpleDate>
        get() = _tillDate
    //endregion

    //region MainActivity Actions
    private var _actions = MutableLiveData<MainActivity.Actions>()
    val actions: LiveData<MainActivity.Actions>
        get() = _actions
    fun setActions(actions: MainActivity.Actions) { _actions.value = actions }
    fun setActionsNone() { _actions.value = MainActivity.Actions.NONE }
    var lastActionIsAddList = false
        private set
    //endregion

    private lateinit var lastTouchedDay: Day
    var lastTouchedPosition = -1
        private set
    fun setLastTouchedItem(day: Day?, position: Int, isDbChanged: Boolean) {
        day?.let {
            lastTouchedDay = it
            _actions.value = MainActivity.Actions.CHECK_PERMISSIONS_DAY
        }
        lastTouchedPosition = position
        if (isDbChanged) _isDbChanged.value = true
    }

    var density: Float = 0F
    var lastDateInMillis = 0L
        private set

    private lateinit var dayTypesToBeDisplayed: Day.Type.Set
    private lateinit var dayTypesToBeAddedToCalendar: Day.Type.Set

    val dayDescriptionForDialog: String
        get() = lastTouchedDay.descriptionForDialog

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

    private val reminder: SimpleReminder
        get() {
            val daysBefore = reminderDaysBefore.value
                    ?: AppPreferences.getReminderDaysBefore(appContext)
            val time = reminderTime.value ?: SimpleTime(AppPreferences.getReminderTime(appContext))
            return SimpleReminder(SimpleReminder.METHOD_ALERT, time, daysBefore, timeZoneId)
        }

    private val timeZoneId: String
        get() = if (AppPreferences.getUsePolishTimeZone(appContext)) "Europe/Warsaw" else TimeZone.getDefault().id

    init {
        resetReminderValues()
        val todayCal = Calendar.getInstance()
        _tillDate.value = SimpleDate.getLastDayOfMonth(todayCal[Calendar.YEAR], todayCal[Calendar.MONTH] + 2)
        viewModelScope.launch {
            resetDayTypes()
        }
    }

    fun getDayTypesToBeAddedToCalendarAsArray(): BooleanArray {
        return booleanArrayOf(
                dayTypesToBeAddedToCalendar.contains(Day.Type.NON_COMMERCE_DAY),
                dayTypesToBeAddedToCalendar.contains(Day.Type.HOLIDAY),
                dayTypesToBeAddedToCalendar.contains(Day.Type.COMMERCE_SUNDAY)
        )
    }

    fun resetDayTypes() {
        dayTypesToBeDisplayed = AppPreferences.newDayTypeSet(appContext)
        dayTypesToBeAddedToCalendar = dayTypesToBeDisplayed
        setDaysAndLastDate()
    }

    fun updateDayTypesToBeAddedToCalendar(int: Int, value: Boolean) {
        Day.Type.getType(int)?.let {
            if (value)
                dayTypesToBeAddedToCalendar.add(it)
            else
                dayTypesToBeAddedToCalendar.remove(it)
        }
    }

    fun addToCalendar(isActionAddList: Boolean) {
        lastActionIsAddList = isActionAddList
        //viewModelScope.launch { AdsContainer.getInterstitialAd(appContext)?.show() }
        viewModelScope.launch {
            val tillDateValue = tillDate.value
            when {
                isActionAddList && tillDateValue == null ->
                    false
                isActionAddList && tillDateValue != null ->
                    Day.List.addToCalendar(
                            appContext,
                            dayTypesToBeAddedToCalendar,
                            reminder,
                            timeZoneId,
                            tillDateValue)
                else -> lastTouchedDay.run {
                    setReminder(reminder)
                    addToGoogleCalendar(appContext, timeZoneId)
                }
            }.let { result ->
                logCalendarEvent(result)
                showInfo(result)
                if (result) _isDbChanged.value = true
            }
        }
    }

    fun askForReminderSettingsAndDateTill() {
        _actions.value = MainActivity.Actions.ASK_FOR_REMINDER_SETTINGS_AND_DATE_TILL
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

    fun resetReminderValues() {
        _reminderDaysBefore.value = AppPreferences.getReminderDaysBefore(appContext)
        _reminderTime.value = SimpleTime(AppPreferences.getReminderTime(appContext))
    }

    fun setReminderDaysBefore(daysBefore: Int) {
        _reminderDaysBefore.value = daysBefore
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _reminderTime.value = SimpleTime(hour, minute)
    }

    fun setReminderDefaults() {
        AppPreferences.setReminderDaysBefore(reminderDaysBefore.value, appContext)
        AppPreferences.setReminderTime(reminderTime.value.toString(), appContext)
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
                    lastDateInMillis = lastDay.dateInMillis
                }
            }
        }

    }

    private fun setNotFirstRun() {
        AppPreferences.setFirstRun(appContext, false)
    }

    fun setTillDate(year: Int, month: Int, day: Int) {
        _tillDate.value = SimpleDate(year, month, day)
    }

    private fun showInfo(resultIsPositive: Boolean) {
        val info = when {
            resultIsPositive -> R.string.info_added_to_calendar
            lastActionIsAddList -> R.string.error_sth_went_wrong
            else -> R.string.error_adding_to_calendar_failed
        }
        Toast.makeText(appContext, info, Toast.LENGTH_SHORT).show()
    }

    //region Firebase log
    fun logLook() {
        FirebaseEvent.logAppTheme(appContext, isLookRich)
    }

    private fun logCalendarEvent(result: Boolean) {
        FirebaseEvent.logCalendarEvent(appContext, lastActionIsAddList, result)
    }
    //endregion
}