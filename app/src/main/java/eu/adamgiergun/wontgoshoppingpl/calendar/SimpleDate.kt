package eu.adamgiergun.wontgoshoppingpl.calendar

import java.text.DateFormat
import java.util.*

class SimpleDate(val year: Int, val month: Int, val day: Int) : Comparable<SimpleDate> {

    val weekdayName: String?
        get() = asCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())

    override fun compareTo(other: SimpleDate): Int {
        val yearDiff = (year - other.year)
        if (yearDiff != 0)
            return yearDiff
        val monthDiff = month - other.month
        return if (monthDiff != 0)
            monthDiff
        else
            day - other.day
    }

    override fun equals(other: Any?): Boolean {
        return other is SimpleDate
                && compareTo(other) == 0
    }

    override fun hashCode(): Int {
        return year * 10000 + month * 100 + day
    }

    val asCalendar: Calendar
        get() {
            val calendar = clearedCalendarInstance
            calendar[year, month - 1] = day
            calendar.time
            return calendar
        }

    override fun toString(): String {
        return DateFormat
                .getDateInstance()
                .format(asCalendar.time)
    }

    fun addDays(value: Int): SimpleDate {
        val calendar = asCalendar
        calendar.add(Calendar.DAY_OF_MONTH, value)
        calendar.time // needed to recalculate calendar
        return SimpleDate(calendar[Calendar.YEAR], calendar[Calendar.MONTH] + 1, calendar[Calendar.DAY_OF_MONTH])
    }

    val timeInMillis: Long
        get() = asCalendar.timeInMillis

    companion object {
        const val ORDER_YDM: Byte = 0
        const val ORDER_YMD: Byte = 1

        //    @Override
        //    public SimpleDate clone() {
        //        try {
        //            return (SimpleDate) super.clone();
        //        } catch (CloneNotSupportedException e) {
        //            return SimpleDate.newInstance(2000, 12, 31);
        //        }
        //    }

        @Deprecated("use constructor instead", ReplaceWith("SimpleDate()"))
        fun newInstance(year: Int, month: Int, day: Int): SimpleDate {
            return SimpleDate(year, month, day)
        }

        fun newInstance(string: String, order: Int): SimpleDate {
            return string.split("_".toRegex()).toTypedArray().run {
                val year = this[0].toInt()
                val month = when (order) {
                    ORDER_YMD.toInt() -> Integer.valueOf(this[1])
                    else -> Integer.valueOf(this[2])
                }
                val day = when (order) {
                    ORDER_YMD.toInt() -> Integer.valueOf(this[2])
                    else -> Integer.valueOf(this[1])
                }
                SimpleDate(year, month, day)
            }
        }

        fun getLastDayOfMonth(year: Int, month: Int): SimpleDate {
            return Calendar.getInstance().run {
                this[year, month - 1] = 1
                this[Calendar.DAY_OF_MONTH] = this.getActualMaximum(Calendar.DAY_OF_MONTH)
                this.time
                SimpleDate(
                        this[Calendar.YEAR],
                        this[Calendar.MONTH] + 1,
                        this[Calendar.DAY_OF_MONTH])
            }
        }

        val yesterday: SimpleDate
            get() {
                Calendar.getInstance().run {
                    this.add(Calendar.DAY_OF_MONTH, -1)
                    this.time
                    return SimpleDate(
                            this[Calendar.YEAR],
                            this[Calendar.MONTH] + 1,
                            this[Calendar.DAY_OF_MONTH])
                }
            }

        private val clearedCalendarInstance: Calendar
            get() = TimeZone.getTimeZone("UTC").run {
                val calendar = Calendar.getInstance(this)
                calendar.clear()
                calendar
            }
    }
}