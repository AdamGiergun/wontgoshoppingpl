package eu.adamgiergun.wontgoshoppingpl.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsFragment
import java.util.*

internal class MainViewModel(application: Application) : AppViewModel(application) {

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
        _tillDate.value = SimpleDate.newInstance(year, month + 1, day)
    }

    private var _reminder = MutableLiveData<SimpleReminder>()
    val reminder: LiveData<SimpleReminder>
        get() = _reminder

    private var lastTouchedDay: Day? = null

    var dayTypesToBeAddedToCalendar: Day.Type.Set? = null
        get() = if (field == null) {
            AppPreferences.newDayTypeSet(appContext)
        } else {
            field!!
        }

    private var dayTypesToBeDisplayed: Day.Type.Set? = null
    private var recreatedOnDemand = false

    init {
        _isActionAddList = false
        recreatedOnDemand = false
        dayTypesToBeDisplayed = AppPreferences.newDayTypeSet(appContext)
        dayTypesToBeAddedToCalendar = dayTypesToBeDisplayed
        resetReminderToDefaultValue()
        val today = Calendar.getInstance()
        _tillDate.value = SimpleDate.getLastDayOfMonth(today[Calendar.YEAR], today[Calendar.MONTH] + 2)
    }

    fun resetReminderToDefaultValue() {
        _reminder.value = reminderPreference
    }

    fun setReminderDaysBefore(daysBefore: Int) {
        val simpleReminder = _reminder.value
        simpleReminder!!.daysBefore = daysBefore
        _reminder.value = simpleReminder
    }

    fun setReminderTime(hour: Int, minute: Int) {
        val time = SimpleTime(hour, minute)
        val simpleReminder = _reminder.value
        simpleReminder!!.time = time
        _reminder.value = simpleReminder
    }

    val dayDescriptionForDialog: String
        get() = lastTouchedDay!!.descriptionForDialog!!

    fun setAppWidgetInfoDisplayed() {
        AppPreferences.setAppWidgetInfoDisplayed(appContext)
    }

    fun setLastTouchedDay(day: Day?) {
        lastTouchedDay = day
        _isActionAddList = false
    }

    fun addToCalendar(): Boolean {
        val result: Boolean = if (_isActionAddList) {
            Day.List.addToCalendar(
                    appContext,
                    dayTypesToBeAddedToCalendar!!,
                    _reminder.value,
                    timeZoneId,
                    tillDate.value!!)
        } else {
            lastTouchedDay!!.setReminder(_reminder.value)
            lastTouchedDay!!.addToGoogleCalendar(appContext, timeZoneId)
        }
        logCalendarEvent(result)
        return result
    }

    private val timeZoneId: String
        get() = if (AppPreferences.getUsePolishTimeZone(appContext)) "Europe/Warsaw" else TimeZone.getDefault().id

//    fun getDayTypesToBeAddedToCalendar(): Day.Type.Set {
//        return if (dayTypesToBeAddedToCalendar == null) AppPreferences.newDayTypeSet(appContext) else dayTypesToBeAddedToCalendar!!
//    }

    fun newDayTypesSet(addNonCommerceDays: Boolean, addHolidays: Boolean, addCommerceSundays: Boolean): Day.Type.Set {
        return Day.Type.newSet(addNonCommerceDays, addHolidays, addCommerceSundays)
    }

    val lastDateInMillis: Long
        get() = Day.List.getLastDateInMillis(appContext)

//    fun setDayTypesToBeAddedToCalendar(dayTypes: Day.Type.Set?) {
//        dayTypesToBeAddedToCalendar = dayTypes
//    }

    fun isRecreatedOnDemand(): Boolean {
        return if (recreatedOnDemand) {
            recreatedOnDemand = false
            true
        } else {
            false
        }
    }

    fun onInitialLookChoice(newIsLookRich: Boolean) {
        isLookRich = newIsLookRich
        setNotFirstRun()
        recreatedOnDemand = true
    }

    //region AppPreferences proxy region
    private val reminderPreference: SimpleReminder
        get() = AppPreferences.getReminder(appContext, SimpleReminder.METHOD_ALERT, timeZoneId)
    val isFirstRun: Boolean
        get() = AppPreferences.isFirstRun(appContext)

    fun wasAppWidgetInfoDisplayed(): Boolean {
        return AppPreferences.wasAppWidgetInfoDisplayed(appContext)
    }

    val isCalendarEnabledAndChosen: Boolean
        get() = AppPreferences.isUseGoogleCalendarSet(appContext) && AppPreferences.isCalendarSet(appContext)

    fun checkIfDayTypesChanged(): Boolean {
        val oldDayTypesToBeDisplayed = dayTypesToBeDisplayed
        dayTypesToBeDisplayed = AppPreferences.newDayTypeSet(appContext)
        val typesChanged = dayTypesToBeDisplayed != oldDayTypesToBeDisplayed
        if (typesChanged) dayTypesToBeAddedToCalendar = dayTypesToBeDisplayed
        return !isFirstRun && typesChanged
    }

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

    val error: Int
        get() = if (_isActionAddList) R.string.error_sth_went_wrong else R.string.error_adding_to_calendar_failed

    companion object {
        const val START_FRAGMENT_ID_TAG = AppViewModel.START_FRAGMENT_ID_TAG

        fun getStartFragmentId(startFromNotifications: Boolean): String {
            return SettingsFragment.getStartFragmentId(startFromNotifications)
        }
    }
}