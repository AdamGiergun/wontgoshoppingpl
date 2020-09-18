package eu.adamgiergun.wontgoshoppingpl.day

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleCalendarEvent
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst.since
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst.wholeDay
import eu.adamgiergun.wontgoshoppingpl.db.DatabaseHandler
import eu.adamgiergun.wontgoshoppingpl.db.Event
import eu.adamgiergun.wontgoshoppingpl.db.EventsTableRow
import java.util.*

class Day private constructor(private val dbId: Int, item: String, private val type: Type?) : Comparable<Day> {
    val name: String
    private var date: SimpleDate? = null

    val dateInMillis: Long
        get() = date!!.timeInMillis

    private val time: String
    val timeDescription: String?
    private val allDay: Boolean
    val backgroundColorResource: Int
    val backgroundResource: Int
    private var noOfConsecutiveDays = 0
    private var reminder: SimpleReminder? = null
    private var description: String? = null
    private var isReminderNeeded = false
    private var calendarId: String? = null
    private var calendarEventId: Long = 0
    private fun newBackgroundResource(): Int {
        return when (type) {
            Type.HOLIDAY -> R.drawable.card_bg_red
            Type.NON_COMMERCE_DAY -> R.drawable.card_bg_orange
            Type.COMMERCE_SUNDAY -> R.drawable.card_bg_green
            else -> 0
        }
    }

    private fun newBackgroundColorResource(): Int {
        return when (type) {
            Type.HOLIDAY -> R.color.colorHoliday
            Type.NON_COMMERCE_DAY -> R.color.colorNonCommerceDay
            Type.COMMERCE_SUNDAY -> R.color.colorCommerceSunday
            else -> 0
        }
    }

    override fun compareTo(other: Day): Int {
        return date!!.compareTo(other.date!!)
    }

    fun addToGoogleCalendar(appContext: Context, timeZoneId: String): Boolean {
        calendarId = AppPreferences.getDefaultCalendarId(appContext)
        val calendarEvent = newCalendarEventInstance(appContext, timeZoneId)
        if (calendarEvent.exists(appContext, calendarId)) {
            calendarEvent.writeReminders(appContext)
        } else {
            if (calendarEvent.exists(appContext)) {
                deleteFromGoogleCalendar(calendarEvent, appContext)
            }
            calendarEventId = calendarEvent.write(appContext)
            updateEventInDb(appContext)
        }
        return calendarEventId > SimpleCalendarEvent.NON_EXISTING
    }

    private fun newCalendarEventInstance(appContext: Context, timeZoneId: String): SimpleCalendarEvent {
        return SimpleCalendarEvent.Builder(calendarId, date, SimpleTime(time), timeZoneId, allDay)
                .setCalendarEventId(calendarEventId)
                .setEndCalendar(endDate, SimpleTime("23:59"))
                .setReminders(reminders)
                .setTitle(if (type == Type.COMMERCE_SUNDAY) description + " - " + appContext.getString(R.string.go_shopping) else description + " - " + appContext.getString(R.string.go_shopping_earlier))
                .build()
    }

    private val endDate: SimpleDate
        get() = date!!.addDays(noOfConsecutiveDays)
    private val reminders: SimpleReminder.List
        get() {
            val reminders = SimpleReminder.List()
            reminders.add(reminder)
            return reminders
        }

    private fun deleteFromGoogleCalendar(calendarEvent: SimpleCalendarEvent, appContext: Context) {
        if (calendarEventId != SimpleCalendarEvent.NON_EXISTING.toLong() && calendarEvent.delete(appContext)) {
            finishDeleting(appContext)
        }
    }

    private fun finishDeleting(appContext: Context) {
        Toast.makeText(appContext, appContext.getString(R.string.info_deleted), Toast.LENGTH_SHORT).show()
        calendarEventId = SimpleCalendarEvent.NON_EXISTING.toLong()
        calendarId = null
        updateEventInDb(appContext)
    }

    private fun updateEventInDb(appContext: Context) {
        DatabaseHandler.updateEvent(dbId, calendarId, calendarEventId, appContext)
    }

    fun deleteFromGoogleCalendar(appContext: Context) {
        if (calendarEventId != SimpleCalendarEvent.NON_EXISTING.toLong() && SimpleCalendarEvent.delete(calendarEventId, appContext, calendarId)) {
            finishDeleting(appContext)
        }
    }

    val dateAndWeekdayString: String
        get() = """
               ${date.toString()}
               ${date!!.weekdayName}
               """.trimIndent()

    val descriptionForDialog: String?
        get() = if (type == Type.HOLIDAY) description else dateAndWeekdayString

    val widgetText: CharSequence
        get() = """
               ${date.toString()}
               PL: $description
               """.trimIndent()

    val isCommerceSunday: Boolean
        get() = type == Type.COMMERCE_SUNDAY

    fun setReminder(reminder: SimpleReminder?) {
        this.reminder = reminder
    }

    private fun appendToDescription(string: String?) {
        description += ", $string"
    }

    private fun compareDateTo(comparedDate: SimpleDate): Int {
        return date!!.compareTo(comparedDate)
    }

    private fun incrementNoOfConsecutiveDays() {
        noOfConsecutiveDays++
    }

    private fun isTomorrowOf(otherDay: Day?): Boolean {
        return date == otherDay!!.date!!.addDays(1)
    }

    private fun setReminderNotNeeded() {
        isReminderNeeded = false
    }

    enum class Type(val code: Int) {
        NON_COMMERCE_DAY(0), HOLIDAY(1), COMMERCE_SUNDAY(2);

        class Set : TreeSet<Type?>()
        companion object {
            fun getType(code: Int): Type? {
                for (type in values()) {
                    if (type.code == code) return type
                }
                return null
            }

            @JvmStatic
            fun newSet(addNonCommerceDays: Boolean, addHolidays: Boolean, addCommerceSundays: Boolean): Set {
                val types = Set()
                if (addNonCommerceDays) types.add(NON_COMMERCE_DAY)
                if (addHolidays) types.add(HOLIDAY)
                if (addCommerceSundays) types.add(COMMERCE_SUNDAY)
                return types
            }
        }
    }

    class List : ArrayList<Day?>() {
        private fun skipReminderForConsecutiveDays() {
            val iterator: ListIterator<Day?> = listIterator()
            if (iterator.hasNext()) {
                var nextDay = iterator.next()
                while (iterator.hasNext()) {
                    val firstDay = nextDay
                    var previousDay = firstDay
                    nextDay = iterator.next()
                    while (!firstDay!!.isCommerceSunday && !nextDay!!.isCommerceSunday && nextDay.isTomorrowOf(previousDay)) {
                        nextDay.setReminderNotNeeded()
                        firstDay.incrementNoOfConsecutiveDays()
                        firstDay.appendToDescription(nextDay.description)
                        if (iterator.hasNext()) {
                            previousDay = nextDay
                            nextDay = iterator.next()
                        } else {
                            break
                        }
                    }
                }
            }
        }

        companion object {
            fun newInstance(appContext: Context): LiveData<List> {
                val dayTypes = AppPreferences.newDayTypeSet(appContext)
                return newInstance(appContext, dayTypes)
            }

            private fun newInstance(appContext: Context, includedDaysTypes: Type.Set): LiveData<List> {
                val list = MutableLiveData<List>()
                list.value = List()
                val eventsIterator = DatabaseHandler.newEventsIterator(appContext, includedDaysTypes)
                val yesterday = SimpleDate.getYesterday()
                while (eventsIterator.hasNext()) {
                    val day = newInstance(eventsIterator.next())
                    if (day.compareDateTo(yesterday) > 0) {
                        list.value!!.add(day)
                    }
                }

                return list
            }

            fun addToCalendar(
                    appContext: Context,
                    dayTypes: Type.Set,
                    reminder: SimpleReminder?,
                    timeZoneId: String,
                    till: SimpleDate): Boolean {
                val list = newInstance(appContext, dayTypes)
                list.value!!.skipReminderForConsecutiveDays()
                var result = true
                for (day in list.value!!) {
                    if (day!!.compareDateTo(till) <= 0 && day.isReminderNeeded) {
                        day.setReminder(reminder)
                        result = result && day.addToGoogleCalendar(appContext, timeZoneId)
                    }
                }
                return result
            }
        }
    }

    //        private void removePastDays() {

    //            Iterator<Day> iterator = this.iterator();
    //            Calendar yesterday = getYesterday();
    //            Day day;
    //            while (iterator.hasNext()) {
    //                day = iterator.next();
    //                if (yesterday.compareTo(day.getCalendar()) >= 0) {
    //                    this.remove(day);
    //                } else {
    //                    break;
    //                }
    //            }
    //        }

    companion object {
        private fun newInstance(row: EventsTableRow): Day {
            val day = Day(row.dbId, row.item, Type.getType(row.typeId))
            day.calendarId = row.calendarId
            day.calendarEventId = row.calendarEventId
            day.description = day.name
            day.isReminderNeeded = true
            day.noOfConsecutiveDays = 0
            return day
        }

        fun newInstance(event: Event): Day {
            val day = Day(event.id!!, event.item, Type.getType(event.typeId))
            day.calendarId = event.calendarId
            day.calendarEventId = event.calendarEventId ?: 0
            day.description = day.name
            day.isReminderNeeded = true
            day.noOfConsecutiveDays = 0
            return day
        }

        fun getDayForWidget(event: Event): Day {
            return Day(event.id!!, event.item, Type.getType(event.typeId)).apply {
                description = name
            }
        }
    }

    init {
        backgroundResource = newBackgroundResource()
        backgroundColorResource = newBackgroundColorResource()
        val pieces = item.split(",".toRegex()).toTypedArray()
        date = if (pieces[0].startsWith("2019")) {
            SimpleDate.newInstance(pieces[0], SimpleDate.ORDER_YDM.toInt())
        } else {
            SimpleDate.newInstance(pieces[0], SimpleDate.ORDER_YMD.toInt())
        }
        time = pieces[1]
        name = pieces[2]
        allDay = time == "00:00"
        timeDescription = if (allDay) wholeDay else since + time
    }
}