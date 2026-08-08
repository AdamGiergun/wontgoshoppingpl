package eu.adamgiergun.wontgoshoppingpl.calendar

import android.provider.CalendarContract
import java.util.*

class SimpleReminder (var method: Int, var time: SimpleTime, var daysBefore: Int, var timeZoneId: String?) {

    class List : ArrayList<SimpleReminder?>()

    companion object {
        const val METHOD_ALERT = CalendarContract.Reminders.METHOD_ALERT
        const val METHOD_EMAIL = CalendarContract.Reminders.METHOD_EMAIL

        @Deprecated("use constructor instead", ReplaceWith("SimpleReminder()"))
        fun newInstance(method: Int, time: SimpleTime, daysBefore: Int, timeZoneId: String?): SimpleReminder {
            return SimpleReminder(method, time, daysBefore, timeZoneId)
        }
    }
}