package eu.adamgiergun.wontgoshoppingpl.main.viewModel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleDate
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleReminder
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AdsContainer
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.common.today
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.db.Event
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity
import eu.adamgiergun.wontgoshoppingpl.main.objects.MainActivityActions
import kotlinx.coroutines.launch
import java.util.*

internal class DaysFragmentViewModel(application: Application) : AppViewModel(application) {

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>>
        get() = _events

    private val _idOfDialogToShow = MutableLiveData<Int>()
    val idOfDialogToShow: LiveData<Int>
        get() = _idOfDialogToShow

    fun setIdOfDialogToShow(id: Int) {
        _idOfDialogToShow.value = id
    }

    private val _isDbChanged = MutableLiveData<Boolean>()
    val isDbChanged: LiveData<Boolean>
        get() = _isDbChanged

    val lastActionIsAddList
        get() = MainActivityActions.lastActionIsAddList

    val lastTouchedPosition
        get() = LastTouchedDay.position

    var density: Float = 0F

    val tillDate: LiveData<SimpleDate>
        get() = DialogParameters.tillDate

    val lastDateInMillis
        get() = DialogParameters.lastDateInMillis

    private var dayTypesToBeDisplayed = Day.Type.Set()
    private lateinit var dayTypesToBeAddedToCalendar: Day.Type.Set

    init {
        initializeReminder()
        val todayCal = Calendar.getInstance()
        DialogParameters.tillDate.value = SimpleDate.getLastDayOfMonth(todayCal[Calendar.YEAR], todayCal[Calendar.MONTH] + 2)
        viewModelScope.launch { updateDisplayedDataSetIfNeeded() }
    }

    fun getDayTypesToBeAddedToCalendarAsArray(): BooleanArray {
        return booleanArrayOf(
                dayTypesToBeAddedToCalendar.contains(Day.Type.NON_COMMERCE_DAY),
                dayTypesToBeAddedToCalendar.contains(Day.Type.HOLIDAY),
                dayTypesToBeAddedToCalendar.contains(Day.Type.COMMERCE_SUNDAY)
        )
    }

    fun updateDayTypesToBeAddedToCalendar(int: Int, value: Boolean) {
        Day.Type.getType(int)?.let {
            if (value)
                dayTypesToBeAddedToCalendar.add(it)
            else
                dayTypesToBeAddedToCalendar.remove(it)
        }
    }

    val reminderDaysBefore: LiveData<Int>
        get() = DialogParameters.Reminder.daysBefore
    val reminderTime: LiveData<SimpleTime>
        get() = DialogParameters.Reminder.time

    fun initializeReminder() {
        DialogParameters.Reminder.daysBefore.value = AppPreferences.getReminderDaysBefore(appContext)
        DialogParameters.Reminder.time.value = SimpleTime(AppPreferences.getReminderTime(appContext))
    }

    fun setReminderDaysBefore(daysBefore: Int) {
        DialogParameters.Reminder.daysBefore.value = daysBefore
    }

    fun setReminderTime(hour: Int, minute: Int) {
        DialogParameters.Reminder.time.value = SimpleTime(hour, minute)
    }

    fun saveCurrentReminderAsDefault() {
        AppPreferences.setReminderDaysBefore(DialogParameters.Reminder.daysBefore.value, appContext)
        AppPreferences.setReminderTime(DialogParameters.Reminder.time.value.toString(), appContext)
    }

    fun setTillDate(year: Int, month: Int, day: Int) {
        DialogParameters.tillDate.value = SimpleDate(year, month, day)
    }

    fun askForReminderSettingsAndDateTill() {
        MainActivityActions.setNextAction(MainActivity.Actions.ASK_FOR_REMINDER_SETTINGS_AND_DATE_TILL)
    }

    val dayDescriptionForDialog: String
        get() = LastTouchedDay.value.descriptionForDialog

    fun addToCalendar(isActionAddList: Boolean, activity: Activity) {
        MainActivityActions.lastActionIsAddList = isActionAddList
        viewModelScope.launch {
            AdsContainer.getInterstitialAd(appContext)?.show(activity)
        }
        viewModelScope.launch {
            val tillDateValue = DialogParameters.tillDate.value
            when {
                isActionAddList && tillDateValue == null ->
                    false
                isActionAddList && tillDateValue != null ->
                    Day.List.addToCalendar(
                            appContext,
                            dayTypesToBeAddedToCalendar,
                            DialogParameters.Reminder.get(appContext, timeZoneId),
                            timeZoneId,
                            tillDateValue)
                else -> LastTouchedDay.value.run {
                    setReminder(DialogParameters.Reminder.get(appContext, timeZoneId))
                    addToGoogleCalendar(appContext, timeZoneId)
                }
            }.let { result ->
                logCalendarEvent(result)
                showInfo(result)
                if (result) _isDbChanged.value = true
            }
        }
    }

    private val timeZoneId: String
        get() = if (AppPreferences.getUsePolishTimeZone(appContext)) "Europe/Warsaw" else TimeZone.getDefault().id

    private fun showInfo(resultIsPositive: Boolean) {
        val info = when {
            resultIsPositive -> R.string.info_added_to_calendar
            MainActivityActions.lastActionIsAddList -> R.string.error_sth_went_wrong
            else -> R.string.error_adding_to_calendar_failed
        }
        Toast.makeText(appContext, info, Toast.LENGTH_SHORT).show()
    }

    //region Firebase log
    private fun logCalendarEvent(result: Boolean) {
        FirebaseEvent.logCalendarEvent(appContext, MainActivityActions.lastActionIsAddList, result)
    }

    fun logLook() {
        FirebaseEvent.logAppTheme(appContext, isLookRich)
    }
    //endregion

    fun setActionNone() {
        MainActivityActions.setNextAction(MainActivity.Actions.NONE)
    }

    fun updateDisplayedDataSetIfNeeded() {
        AppPreferences.storedDayTypeSet(appContext).let { storedDayTypeSet ->
            if (dayTypesToBeDisplayed != storedDayTypeSet) {
                dayTypesToBeDisplayed = storedDayTypeSet
                dayTypesToBeAddedToCalendar = storedDayTypeSet
                setDaysAndLastDate()
            } else if (isDbChanged.value == true) {
                setDaysAndLastDate()
                _isDbChanged.value = false
            }
        }
    }

    private fun setDaysAndLastDate() {
        val dayTypes = dayTypesToBeDisplayed.map { it.code.toString() }.toList()
        AppDatabase.getInstance(getApplication()).eventDao().run {
            viewModelScope.launch {
                getAllEventsOfTypeNotBefore(dayTypes, today)?.let { list ->
                    _events.value = list
                }

                getLastEvent()?.let {
                    val lastDay = Day(it)
                    DialogParameters.lastDateInMillis = lastDay.dateInMillis
                }
            }
        }
    }

    fun setLastTouchedItem(day: Day?, position: Int, isDbChanged: Boolean) {
        day?.let {
            LastTouchedDay.value = it
            MainActivityActions.setNextAction(MainActivity.Actions.CHECK_PERMISSIONS_DAY)
        }
        LastTouchedDay.position = position
        if (isDbChanged) _isDbChanged.value = true
    }

    private object DialogParameters {
        object Reminder {
            val daysBefore = MutableLiveData<Int>()
            val time = MutableLiveData<SimpleTime>()

            fun get(appContext: Context, timeZoneId: String): SimpleReminder {
                val daysBefore = daysBefore.value
                        ?: AppPreferences.getReminderDaysBefore(appContext)
                val time = time.value ?: SimpleTime(AppPreferences.getReminderTime(appContext))
                return SimpleReminder(SimpleReminder.METHOD_ALERT, time, daysBefore, timeZoneId)
            }
        }

        val tillDate = MutableLiveData<SimpleDate>()

        var lastDateInMillis = 0L
    }

    private object LastTouchedDay {
        lateinit var value: Day
        var position = -1
    }
}