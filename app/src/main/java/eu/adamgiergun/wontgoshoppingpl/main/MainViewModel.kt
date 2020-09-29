package eu.adamgiergun.wontgoshoppingpl.main

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleDate
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleReminder
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.*
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsFragment
import kotlinx.coroutines.launch
import java.util.*

internal class MainViewModel(application: Application) : AppViewModel(application) {

    private var _days = MutableLiveData<List<Day>>()
    val days: LiveData<List<Day>>
        get() = _days

    private var _isActionAddList = false
    val isActionAddList
        get() = _isActionAddList

    fun setActionAddList() {
        _isActionAddList = true
    }

    private val _tillDate = MutableLiveData<SimpleDate>()
    val tillDate: LiveData<SimpleDate>
        get() = _tillDate

    fun setTillDate(year: Int, month: Int, day: Int) {
        _tillDate.value = SimpleDate(year, month, day)
    }

    private var _reminder = MutableLiveData<SimpleReminder>()
    val reminder: LiveData<SimpleReminder>
        get() = _reminder

    private lateinit var lastTouchedDay: Day

    private val dayTypesToBeDisplayed = AppPreferences.newDayTypeSet(appContext)
    private var dayTypesToBeAddedToCalendar: Day.Type.Set

    fun updateDayTypesToBeAddedToCalendar(int: Int, value: Boolean) {
        Day.Type.getType(int)?.let {
            if (value)
                dayTypesToBeAddedToCalendar.add(it)
            else
                dayTypesToBeAddedToCalendar.remove(it)
        }
    }

    fun getDayTypesToBeAddedToCalendarAsArray(): BooleanArray {
        return booleanArrayOf(
                dayTypesToBeAddedToCalendar.contains(Day.Type.NON_COMMERCE_DAY),
                dayTypesToBeAddedToCalendar.contains(Day.Type.HOLIDAY),
                dayTypesToBeAddedToCalendar.contains(Day.Type.COMMERCE_SUNDAY)
        )
    }

    var lastDateInMillis = 0L
        private set

    private var _askForReminderSettings = MutableLiveData<Boolean>()
    val askForReminderSettings: LiveData<Boolean>
        get() = _askForReminderSettings

    fun askForReminderSettings() {
        _askForReminderSettings.value = true
    }

    init {
        dayTypesToBeAddedToCalendar = dayTypesToBeDisplayed
        setReminderToDefaultValueIfNull()
        val todayCal = Calendar.getInstance()
        _tillDate.value = SimpleDate.getLastDayOfMonth(todayCal[Calendar.YEAR], todayCal[Calendar.MONTH] + 2)

        viewModelScope.launch {
            setDaysAndLastDate(application)
        }
    }

//    private var _eventsChanged = MutableLiveData<Boolean>()
//    val eventsChanged: LiveData<Boolean>
//        get() = _eventsChanged

    private suspend fun setDaysAndLastDate(application: Application) {
        val dayTypes = dayTypesToBeDisplayed.map {
            it.code.toString()
        }.toList()
        val eventDao = AppDatabase.getInstance(application).eventDao()
        if (isFirstRun) eventDao.initialize()
        _days.value = eventDao.getAllEventsOfTypeNotBefore(dayTypes, today)?.map {
            Day.newInstance(it)
        }
//        eventDao.getAllEventsOfTypeNotBefore(dayTypes, today).observe(this.getApplication(), {
//            _eventsChanged.value = true
//        })
        eventDao.getLastEvent()?.let {
            val lastDay = Day.newInstance(it)
            lastDateInMillis = lastDay.dateInMillis
        }
    }

    fun setReminderToDefaultValueIfNull() {
        if (_reminder.value == null)
            _reminder.value = reminderPreference
    }

    fun setReminderDaysBefore(daysBefore: Int) {
        _reminder.value?.let {
            it.daysBefore = daysBefore
            _reminder.value = it
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _reminder.value?.let {
            it.time = SimpleTime(hour, minute)
            _reminder.value = it
        }
    }

    val dayDescriptionForDialog: String
        get() = lastTouchedDay.descriptionForDialog

    fun setAppWidgetInfoDisplayed() {
        AppPreferences.setAppWidgetInfoDisplayed(appContext)
    }

    fun setLastTouchedDay(day: Day) {
        lastTouchedDay = day
        _isActionAddList = false
    }

    suspend fun addToCalendar(): Boolean {
        InterstitialAdContainer.getAd(appContext)?.show()
        val result: Boolean = if (_isActionAddList) {
            val tillDateValue = tillDate.value
            if (tillDateValue == null) {
                false
            } else
                Day.List.addToCalendar(
                        appContext,
                        dayTypesToBeAddedToCalendar,
                        _reminder.value,
                        timeZoneId,
                        tillDateValue)
        } else {
            lastTouchedDay.setReminder(_reminder.value)
            lastTouchedDay.addToGoogleCalendar(appContext, timeZoneId)
        }
        logCalendarEvent(result)
        showInfo(result)
        return result
    }

    private fun showInfo(resultIsPositive: Boolean) {
        val info = if (resultIsPositive) R.string.info_added_to_calendar else error
        Toast.makeText(appContext, info, Toast.LENGTH_SHORT).show()
    }

    private val timeZoneId: String
        get() = if (AppPreferences.getUsePolishTimeZone(appContext)) "Europe/Warsaw" else TimeZone.getDefault().id

    fun onInitialLookChoice(newIsLookRich: Boolean) {
        isLookRich = newIsLookRich
        setNotFirstRun()
    }

    //region AppPreferences proxy region
    private val reminderPreference: SimpleReminder
        get() = AppPreferences.getReminder(appContext, SimpleReminder.METHOD_ALERT, timeZoneId)

    val isFirstRun: Boolean
        get() = AppPreferences.isFirstRun(appContext)
    val mainNavigationId: Int
        get() = when {
            isFirstRun -> R.navigation.main_navigation_first_run_widget_info
            !AppPreferences.wasAppWidgetInfoDisplayed(appContext) -> R.navigation.main_navigation_widget_info
            else -> R.navigation.main_navigation
        }

    val isCalendarEnabledAndChosen: Boolean
        get() = AppPreferences.isUseGoogleCalendarSet(appContext) && AppPreferences.isCalendarSet(appContext)

    private fun setNotFirstRun() {
        AppPreferences.setFirstRun(appContext, false)
    }

    //endregion
    fun logLook() {
        FirebaseEvent.logAppTheme(appContext, isLookRich)
    }

    private fun logCalendarEvent(result: Boolean) {
        FirebaseEvent.logCalendarEvent(appContext, _isActionAddList, result)
    }

    private val error: Int
        get() = if (_isActionAddList) R.string.error_sth_went_wrong else R.string.error_adding_to_calendar_failed

    companion object {
        const val START_FRAGMENT_ID_TAG = AppViewModel.START_FRAGMENT_ID_TAG

        fun getStartFragmentId(startFromNotifications: Boolean): String {
            return SettingsFragment.getStartFragmentId(startFromNotifications)
        }
    }

    val askForLookText
        get() = LocalizedConst.askForLook
}