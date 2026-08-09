package eu.adamgiergun.wontgoshoppingpl.calendar

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.annotation.VisibleForTesting
import java.text.SimpleDateFormat
import java.util.*

class SimpleCalendarEvent private constructor(builder: Builder) {
    private val calendarID: String
    private var eventID: Long
    var state: State = State.NEW
    private val title: String?
    private val startCalendar: Calendar
    private val endCalendar: Calendar
    private val availability: Int = CalendarContract.Events.AVAILABILITY_FREE
    private val accessLevel: Int = CalendarContract.Events.ACCESS_PRIVATE
    private val reminders: Reminder.List?
    private val repetition: Repetition?
    private val eventTimeZoneId: String
    private val allDay: Boolean

    //    private String organizerEmail;
    //    private String eventLocation;
    //    private String eventDescription;
    //    private String eventEndTimeZoneId;
    //    private String duration; // see https://tools.ietf.org/html/rfc5545#section-3.8.2.5
    enum class State {
        TO_DELETE, TO_UPDATE, NON_EXISTING, NEW, EXISTING
    }

    fun write(context: Context): Long {
        val cr = context.contentResolver
        val uri = cr.insert(eventsUri, contentValues)
        if (uri == null) {
            eventID = NON_EXISTING.toLong()
        } else {
            eventID = findId(uri)
            writeReminders(context)
        }
        return eventID
    }

    /**
     * Strefa zapisywana z wydarzeniem.
     *
     * Dni bez handlu są **datami, nie momentami** — „niedziela niehandlowa"
     * dotyczy całej doby w Polsce, a nie punktu na osi czasu. Dostawca kalendarza
     * zapisuje taką datę jako północ UTC i wymaga wtedy `EVENT_TIMEZONE` równego
     * `"UTC"`; dopiero ta para sprawia, że wydarzenie wypada na tym samym dniu
     * kalendarza u każdego, niezależnie od strefy jego telefonu. Zakotwiczenie
     * startu w północy warszawskiej pokazałoby niedzielę niehandlową w sobotę
     * każdemu na zachód od UTC.
     *
     * Wydarzenia z godziną to co innego — tam moment ma znaczenie („sklepy
     * zamykają się o 13:00 w Wielką Sobotę") i strefą jest Polska, razem
     * z obowiązującą tego dnia zmianą czasu.
     */
    private val contentValuesTimeZoneId
        get() = if (allDay) TIMEZONE_UTC else eventTimeZoneId

    /**
     * Koniec wydarzenia całodobowego to północ dnia **następnego** po ostatnim
     * dniu ciągu, bo kontrakt wymaga czasu trwania będącego wielokrotnością doby.
     * Budowniczy dostaje z [eu.adamgiergun.wontgoshoppingpl.day.Day] godzinę
     * 23:59 ostatniego dnia — czytelną dla człowieka, ale dającą dobę bez minuty.
     */
    private val dtEnd: Long
        get() = if (!allDay) endCalendar.timeInMillis
        else (endCalendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
            time
        }.timeInMillis

    //contentValues.put(CalendarContract.Events.HAS_ALARM, true);
    // Widoczne w module, żeby dało się sprawdzić testem, co naprawdę trafia do
    // dostawcy kalendarza. Wcześniej testy sięgały tylko do kalendarzy startu
    // i końca, więc sprzeczność między DTSTART w UTC a strefą „Europe/Warsaw"
    // w sąsiednim polu przechodziła niezauważona.
    @VisibleForTesting
    internal val contentValues: ContentValues
        get() {
            return ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarID)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, "")
                put(CalendarContract.Events.EVENT_LOCATION, "")
                put(CalendarContract.Events.ACCESS_LEVEL, accessLevel)
                put(CalendarContract.Events.AVAILABILITY, availability)
                if (allDay) put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.DTSTART, startCalendar.timeInMillis)
                put(CalendarContract.Events.DTEND, dtEnd)
                put(CalendarContract.Events.EVENT_END_TIMEZONE, contentValuesTimeZoneId)
                put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
                put(CalendarContract.Events.EVENT_TIMEZONE, contentValuesTimeZoneId)
                put(CalendarContract.Events.HAS_ATTENDEE_DATA, true)
                //contentValues.put(CalendarContract.Events.HAS_ALARM, true);
                repetition?.let {
                    if (repetition.rule != "")
                        contentValues.put(CalendarContract.Events.RRULE, repetition.rule)
                }
            }
        }


    private fun findId(uri: Uri): Long {
        return uri.toString().run {
            val lastIndexOfSlashPlusOne = lastIndexOf("/") + 1
            val uriSubstringAfterSlash = substring(lastIndexOfSlashPlusOne)
            uriSubstringAfterSlash.toLong()
        }
    }

    fun writeReminders(context: Context) {
        reminders?.let {
            if (eventID > 0 && reminders.size > 0) {
                for (reminder in reminders) {
                    reminder?.write (context, eventID)
                }
            }
        }
    }

    fun delete(context: Context, calendarID: String?): Boolean {
        return delete(eventID, context, calendarID)
    }

    fun delete(context: Context): Boolean {
        return delete(eventID, context)
    }

    fun update(context: Context): Int {
        return context.contentResolver
                .update(
                        eventsUri,
                        contentValues,
                        "calendar_id=? and _id=?",
                        arrayOf(calendarID, eventID.toString())
                )
    }

    fun exists(context: Context, calendarID: String?): Boolean {
        calendarID?.let {
            return exists(
                    context.contentResolver,
                    "calendar_id=? and _id=?",
                    arrayOf(calendarID, eventID.toString())
            )
        }
        return false
    }

    fun exists(context: Context): Boolean {
        return exists(
                context.contentResolver,
                "_id=?",
                arrayOf(eventID.toString())
        )
    }

    fun getStartCalendar(): Calendar {
        return startCalendar.clone() as Calendar
    }

    fun getEndCalendar(): Calendar {
        return endCalendar.clone() as Calendar
    }

    class Reminder(eventStartCalendar: Calendar, simpleReminder: SimpleReminder) {
        private val method: Int = simpleReminder.method // use constants from CalendarContract.Reminders //TODO use something else (enum?)
        private val numberOfMinutesBeforeEvent: Long

        private fun getReminderCalendar(calendar: Calendar, numberOfDaysBefore: Int, time: SimpleTime): Calendar {
            return (calendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -1 * numberOfDaysBefore)
                this[Calendar.HOUR_OF_DAY] = time.hour
                this[Calendar.MINUTE] = time.minute
                this.time
            }
        }

        private fun getDifferenceInMinutes(firstCalendar: Calendar, latterCalendar: Calendar): Long {
            val difference = latterCalendar.timeInMillis - firstCalendar.timeInMillis
            return difference / 60000L
        }

        fun write(context: Context, eventID: Long) {
            val reminderCV = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventID)
                put(CalendarContract.Reminders.METHOD, method)
                put(CalendarContract.Reminders.MINUTES, numberOfMinutesBeforeEvent)
            }
            if (context.checkCallingOrSelfPermission("android.permission.WRITE_CALENDAR") == PackageManager.PERMISSION_GRANTED) {
                val cr = context.contentResolver
                cr.insert(CalendarContract.Reminders.CONTENT_URI, reminderCV)
            }
        }

        class List : ArrayList<Reminder?>()

        init {
            val reminderCalendar = getReminderCalendar(eventStartCalendar, simpleReminder.daysBefore, simpleReminder.time)
            numberOfMinutesBeforeEvent = getDifferenceInMinutes(reminderCalendar, eventStartCalendar)
        }
    }

    abstract class Repetition protected constructor(private val frequency: Frequency, private val interval: Int) {

        enum class Frequency(private val text: String) {
            DAILY("DAILY"), WEEKLY("WEEKLY"), MONTHLY("MONTHLY"), YEARLY("YEARLY"), NONE("NONE");

            override fun toString(): String {
                return text
            }
        }

        open val rule: String
            get() = "FREQ=$frequency;INTERVAL=$interval"
    }

    private class RepetitionUntil(private val untilDate: Calendar, frequency: Frequency, interval: Int)
        : Repetition(frequency, interval)
    {
        override val rule: String
            get() = super.rule + ";UNTIL=" + repetitionUntilDatePlusOneDay

        // according to RFC 5545 to set X to be the last day of repetition, set "UNTIL" date to X+1
        private val repetitionUntilDatePlusOneDay: String
            get() {
                @SuppressLint("SimpleDateFormat")
                val yyyyMMdd = SimpleDateFormat("yyyyMMdd")
                val untilPlusOne = (untilDate.clone() as Calendar).apply {
                    add(Calendar.DATE, 1) // according to RFC 5545 to set X to be the last day of repetition, set "UNTIL" date to X+1
                }
                return yyyyMMdd.format(untilPlusOne.time)
            }
    }

    private class RepetitionCount(private val count: Int, frequency: Frequency, interval: Int)
        : Repetition(frequency, interval)
    {
        override val rule: String
            get() = super.rule + ";COUNT=" + count.toString()
    }

    class Builder(val calendarID: String, startDate: SimpleDate, startTime: SimpleTime, timeZoneId: String?, val allDay: Boolean) {
        var calendarEventId: Long = 0
        var title: String? = null
        val timeZoneId: String = timeZoneId ?: TimeZone.getDefault().id
        val startCalendar: Calendar = getCalendar(startDate, startTime)
        var endCalendar: Calendar? = null
        var reminders: Reminder.List? = null
        var repetition: Repetition? = null

        fun setCalendarEventId(id: Long): Builder {
            calendarEventId = id
            return this
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setEndCalendar(endDate: SimpleDate, endTime: SimpleTime): Builder {
            endCalendar = getCalendar(endDate, endTime)
            return this
        }

        fun setReminders(simpleReminders: SimpleReminder.List): Builder {
            reminders = Reminder.List()
            simpleReminders.forEach {
                it?.let {
                    reminders?.add(Reminder(startCalendar, it))
                }
            }
            return this
        }

        fun setRepetitionUntil(until: Calendar, frequency: Repetition.Frequency, interval: Int): Builder {
            repetition = RepetitionUntil(until, frequency, interval)
            return this
        }

        fun setRepetitionCount(count: Int, frequency: Repetition.Frequency, interval: Int): Builder {
            repetition = RepetitionCount(count, frequency, interval)
            return this
        }

        fun setRepetition(repetition: Repetition): Builder {
            this.repetition = repetition
            return this
        }

        fun build(): SimpleCalendarEvent {
            return SimpleCalendarEvent(this)
        }

        private fun getCalendar(simpleDate: SimpleDate, time: SimpleTime): Calendar {
            return simpleDate.asCalendar.run {
                this[Calendar.HOUR_OF_DAY] = time.hour
                this[Calendar.MINUTE] = time.minute
                this.time
                if (allDay)
                    this
                else
                    setProperTime(this)
            }
        }

        private fun setProperTime(calendar: Calendar): Calendar {
            val utcTime = calendar.timeInMillis
            val offset = TimeZone.getTimeZone(timeZoneId).getOffset(utcTime).toLong()
            val timezoneAlteredTime = utcTime - offset
            return Calendar.getInstance(TimeZone.getTimeZone(timeZoneId)).apply {
                timeInMillis = timezoneAlteredTime
                time
            }
        }
    }

    private fun getEndCalendar(builderEndCalendar: Calendar?): Calendar {
        return builderEndCalendar ?:
            (startCalendar.clone() as Calendar).apply {
                add(Calendar.HOUR_OF_DAY, 1)
                time
            }
    }

    companion object {
        const val NON_EXISTING = 0

        /**
         * Wartość wymagana przez dostawcę kalendarza dla wydarzeń całodobowych.
         * Odpowiednik `android.text.format.Time.TIMEZONE_UTC`, wycofanego razem
         * z całą klasą `Time` w API 22.
         */
        const val TIMEZONE_UTC = "UTC"

        private val eventsUri = Uri.parse("content://com.android.calendar/events")

        val clearedCalendarInstance: Calendar
            get() {
                val utc = TimeZone.getTimeZone("UTC")
                return Calendar.getInstance(utc).apply {
                    clear()
                }
            }

        fun delete(id: Long, context: Context, calendarID: String?): Boolean {
            val cr = context.contentResolver
            return 0 < cr.delete(
                    eventsUri,
                    "calendar_id=? and _id=?",
                    arrayOf(calendarID, id.toString()))
        }

        fun delete(id: Long, context: Context): Boolean {
            val cr = context.contentResolver
            return 0 < cr.delete(
                    eventsUri,
                    "_id=?",
                    arrayOf(id.toString()))
        }

        fun exists(context: Context, eventID: Long, calendarID: String?): Boolean {
            calendarID?.let {
                return exists(
                        context.contentResolver,
                        "calendar_id=? and _id=?",
                        arrayOf(calendarID, eventID.toString())
                )
            }
            return false
        }

        fun exists(context: Context, eventID: Long): Boolean {
            return exists(
                    context.contentResolver,
                    "_id=?",
                    arrayOf(eventID.toString())
            )
        }

        private fun exists(contentResolver: ContentResolver, selection: String, selectionArgs: Array<String>): Boolean {
            return contentResolver.query(
                    eventsUri,
                    arrayOf("_id"),
                    selection,
                    selectionArgs,
                    null
            ).use {
                it != null && it.count > 0
            }
        }
    }

    init {
        allDay = builder.allDay
        calendarID = builder.calendarID
        eventID = builder.calendarEventId
        title = builder.title
        eventTimeZoneId = builder.timeZoneId
        startCalendar = builder.startCalendar
        endCalendar = getEndCalendar(builder.endCalendar)
        reminders = builder.reminders
        repetition = builder.repetition
//        organizerEmail;
//        eventLocation;
//        eventDescription;
//        String duration; // see https://tools.ietf.org/html/rfc5545#section-3.8.2.5
    }
}