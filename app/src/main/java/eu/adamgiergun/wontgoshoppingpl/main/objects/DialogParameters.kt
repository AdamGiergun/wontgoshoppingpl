package eu.adamgiergun.wontgoshoppingpl.main.objects

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleDate
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleReminder
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences

internal object DialogParameters {
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

    private val _tillDate = MutableLiveData<SimpleDate>()
    val tillDate: LiveData<SimpleDate>
        get() = _tillDate

    fun setTillDate(date: SimpleDate) {
        _tillDate.value = date
    }

    var lastDateInMillis = 0L
}