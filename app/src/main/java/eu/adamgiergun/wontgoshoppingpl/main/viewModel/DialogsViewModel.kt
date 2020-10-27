package eu.adamgiergun.wontgoshoppingpl.main.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleDate
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AdsContainer
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.common.AppViewModel
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity
import eu.adamgiergun.wontgoshoppingpl.main.objects.DialogParameters
import eu.adamgiergun.wontgoshoppingpl.main.objects.IsDbChanged
import eu.adamgiergun.wontgoshoppingpl.main.objects.LastTouchedDay
import eu.adamgiergun.wontgoshoppingpl.main.objects.MainActivityActions
import kotlinx.coroutines.launch
import java.util.*

internal class DialogsViewModel(application: Application) : AppViewModel(application) {

    val tillDate
        get() = DialogParameters.tillDate
    val lastDateInMillis
        get() = DialogParameters.lastDateInMillis
    private var dayTypesToBeAddedToCalendar: Day.Type.Set

    init {
        initializeReminder()
        val todayCal = Calendar.getInstance()
        DialogParameters.setTillDate(SimpleDate.getLastDayOfMonth(todayCal[Calendar.YEAR], todayCal[Calendar.MONTH] + 2))
        dayTypesToBeAddedToCalendar = AppPreferences.newDayTypeSet(appContext)
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
        DialogParameters.setTillDate(SimpleDate(year, month, day))
    }

    fun askForReminderSettingsAndDateTill() {
        MainActivityActions.setNextAction(MainActivity.Actions.ASK_FOR_REMINDER_SETTINGS_AND_DATE_TILL)
    }

    val dayDescriptionForDialog: String
        get() = LastTouchedDay.value.descriptionForDialog

    fun addToCalendar(isActionAddList: Boolean) {
        MainActivityActions.lastActionIsAddList = isActionAddList
        viewModelScope.launch { AdsContainer.getInterstitialAd(appContext)?.show() }
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
                if (result) IsDbChanged.set(true)
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

    private fun logCalendarEvent(result: Boolean) {
        FirebaseEvent.logCalendarEvent(appContext, MainActivityActions.lastActionIsAddList, result)
    }

    fun setActionNone() {
        MainActivityActions.setNextAction(MainActivity.Actions.NONE)
    }
}