package eu.adamgiergun.wontgoshoppingpl.day

import android.content.Context
import android.widget.Toast
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleCalendarEvent
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleDate
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleReminder
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.common.today
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.db.Event
import java.util.*
import kotlin.collections.ArrayList

internal class Day(private val event: Event) : Comparable<Day> {
    private val date: SimpleDate
    val time: String
    val name: String
    private var calendarId: String?

    var calendarEventId: Long?
        private set

    init {
        calendarId = event.calendarId
        calendarEventId = event.calendarEventId
        val pieces = event.item.split(",".toRegex()).toTypedArray()
        date = SimpleDate.newInstance(pieces[0], SimpleDate.ORDER_YMD.toInt())
        time = pieces[1]
        name = pieces[2]
    }

    val isAddedToCalendar
        get() = calendarEventId != null

    val dateInMillis
        get() = date.timeInMillis

    val allDay: Boolean
        get() = time == "00:00"

    private val type: Type
        get() = Type.getType(event.typeId)!!

    val backgroundColorResource = when (type) {
        Type.HOLIDAY -> R.color.colorHoliday
        Type.NON_COMMERCE_DAY -> R.color.colorNonCommerceDay
        Type.COMMERCE_SUNDAY -> R.color.colorCommerceSunday
    }

    val backgroundResource = when (type) {
        Type.HOLIDAY -> R.drawable.card_bg_red
        Type.NON_COMMERCE_DAY -> R.drawable.card_bg_orange
        Type.COMMERCE_SUNDAY -> R.drawable.card_bg_green
    }

    private var noOfConsecutiveDays = 0
    private var reminder: SimpleReminder? = null
    private var description = name
    private var isReminderNeeded = true

    override fun compareTo(other: Day): Int {
        return date.compareTo(other.date)
    }

    suspend fun addToGoogleCalendar(appContext: Context, timeZoneId: String): Boolean {
        calendarId = AppPreferences.getDefaultCalendarId(appContext)
        newCalendarEventInstance(appContext, timeZoneId).let { calendarEvent ->
            when {
                calendarEvent == null -> updateEventInDb(null, appContext)
                calendarEvent.exists(appContext, calendarId) -> calendarEvent.writeReminders(appContext)
                calendarEvent.exists(appContext) -> {
                    if (calendarEventId != null && calendarEvent.delete(appContext)) {
                        deleteCalendarInfo(appContext, false)
                        calendarId = AppPreferences.getDefaultCalendarId(appContext)
                    }
                    updateEventInDb(calendarEvent.write(appContext), appContext)
                }
                else -> updateEventInDb(calendarEvent.write(appContext), appContext)
            }
        }
        return calendarEventId != null
    }

    private fun newCalendarEventInstance(appContext: Context, timeZoneId: String): SimpleCalendarEvent? {
        return calendarId?.let { calendarId ->
            SimpleCalendarEvent.Builder(calendarId, date, SimpleTime(time), timeZoneId, allDay)
                    .setCalendarEventId(calendarEventId ?: 0)
                    .setEndCalendar(endDate, SimpleTime("23:59"))
                    .setReminders(reminders)
                    .setTitle("$description - " + appContext.getString( if (type == Type.COMMERCE_SUNDAY) R.string.go_shopping else R.string.go_shopping_earlier))
                    .build()
        }
    }

    private val endDate
        get() = date.addDays(noOfConsecutiveDays)

    private val reminders: SimpleReminder.List
        get() {
            return SimpleReminder.List().apply { add(reminder) }
        }

    private suspend fun deleteCalendarInfo(appContext: Context, showInfo: Boolean) {
        if (showInfo) Toast.makeText(appContext, appContext.getString(R.string.info_deleted), Toast.LENGTH_SHORT).show()
        updateEventInDb(null, appContext)
    }

    private suspend fun updateEventInDb(newCalendarEventId: Long?, appContext: Context) {
        calendarEventId = newCalendarEventId
        if (newCalendarEventId == null) calendarId = null
        val database = AppDatabase.getInstance(appContext).eventDao()
        event.calendarId = calendarId
        event.calendarEventId = calendarEventId
        database.update(event)
    }

    suspend fun deleteFromGoogleCalendar(context: Context) {
        calendarEventId.let {
            if (it == null || !SimpleCalendarEvent.exists(context, it))
                deleteCalendarInfo(context, false)
            else
                deleteCalendarInfo(context, SimpleCalendarEvent.delete(it, context, calendarId))
        }
    }

    val dateAndWeekdayString: String
        get() = """
               $date
               ${date.weekdayName}
               """.trimIndent()

    val descriptionForDialog: String
        get() = if (type == Type.HOLIDAY) name else dateAndWeekdayString

    val widgetText: CharSequence
        get() = """
               $date
               PL: $name
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
        return date.compareTo(comparedDate)
    }

    private fun incrementNoOfConsecutiveDays() {
        noOfConsecutiveDays++
    }

    private fun isTomorrowOf(otherDay: Day): Boolean {
        return date == otherDay.date.addDays(1)
    }

    enum class Type(val code: Int) {
        NON_COMMERCE_DAY(0), HOLIDAY(1), COMMERCE_SUNDAY(2);

        class Set : TreeSet<Type>()

        companion object {
            private val map = values().associateBy(Type::code)

            fun getType(type: Int) = map[type]

            fun newSet(addNonCommerceDays: Boolean, addHolidays: Boolean, addCommerceSundays: Boolean): Set {
                return Set().apply {
                    if (addNonCommerceDays) add(NON_COMMERCE_DAY)
                    if (addHolidays) add(HOLIDAY)
                    if (addCommerceSundays) add(COMMERCE_SUNDAY)
                }
            }
        }
    }

    class List : ArrayList<Day>() {
        companion object {
            private fun skipReminderForConsecutiveDays(list: ArrayList<Day>): ArrayList<Day> {
                val iterator: ListIterator<Day> = list.listIterator()
                if (iterator.hasNext()) {
                    var nextDay = iterator.next()
                    while (iterator.hasNext()) {
                        val firstDay = nextDay
                        var previousDay = firstDay
                        nextDay = iterator.next()
                        while (!firstDay.isCommerceSunday && !nextDay.isCommerceSunday && nextDay.isTomorrowOf(previousDay)) {
                            nextDay.isReminderNeeded = false
                            firstDay.incrementNoOfConsecutiveDays()
                            firstDay.appendToDescription(nextDay.name)
                            if (iterator.hasNext()) {
                                previousDay = nextDay
                                nextDay = iterator.next()
                            } else {
                                break
                            }
                        }
                    }
                }
                return list
            }

            suspend fun addToCalendar(
                    appContext: Context,
                    dayTypes: Type.Set,
                    reminder: SimpleReminder?,
                    timeZoneId: String,
                    till: SimpleDate): Boolean {

                val dayTypesArray = dayTypes.map { type ->
                    type.code.toString()
                }.toList()

                val database = AppDatabase.getInstance(appContext).eventDao()

                val list = database.getAllEventsOfTypeNotBefore(dayTypesArray, today)?.map { event ->
                    Day(event)
                } as ArrayList<Day>

                val finalList = skipReminderForConsecutiveDays(list)
                var result = true
                for (day in finalList) {
                    if (day.compareDateTo(till) <= 0 && day.isReminderNeeded) {
                        day.setReminder(reminder)
                        result = result && day.addToGoogleCalendar(appContext, timeZoneId)
                    }
                }
                return result
            }
        }
    }

    companion object {
        fun getDayForWidget(event: Event?): Day? {
            return event?.let { Day(it) }
        }
    }
}