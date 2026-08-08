package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.calendar.SimpleReminder
import eu.adamgiergun.wontgoshoppingpl.calendar.SimpleTime

abstract class MyAppPreferences {
    protected companion object {
        @JvmStatic
        fun getDefaultCalendarId(appContext: Context): String? {
            val key = appContext.getString(R.string.default_calendar_preference_key)
            val defaultValue = appContext.getString(R.string.id_for_default_calendar_not_set)
            return getSharedPreferences(appContext).getString(key, defaultValue)
        }

        @JvmStatic
        private fun getSharedPreferences(appContext: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(appContext)
        }

        @JvmStatic
        fun getReminder(context: Context, method: Int, timeZoneId: String?): SimpleReminder {
            return SimpleReminder(method, getReminderTime(context), getReminderDays(context), timeZoneId)
        }

        @JvmStatic
        private fun getReminderTime(appContext: Context): SimpleTime {
            val key = appContext.getString(R.string.default_reminder_time_preference_key)
            val defaultValue = appContext.getString(R.string.default_reminder_time_preference_default_value)
            val time = getSharedPreferences(appContext).getString(key, defaultValue)
            return SimpleTime(time ?: defaultValue)
        }

        @JvmStatic
        private fun getReminderDays(appContext: Context): Int {
            val resources = appContext.resources
            val key = resources.getString(R.string.default_reminder_days_before_preference_key)
            val defaultValue = resources.getInteger(R.integer.reminder_days_before_default_value)
            return getSharedPreferences(appContext).getInt(key, defaultValue)
        }

        @JvmStatic
        fun isUseGoogleCalendarSet(appContext: Context): Boolean {
            val key = appContext.getString(R.string.use_google_calendar_preference_key)
            return getBoolean(appContext, key, false)
        }

        @JvmStatic
        fun isCalendarSet(appContext: Context): Boolean {
            val key = appContext.getString(R.string.default_calendar_preference_key)
            val defaultValue = appContext.getString(R.string.id_for_default_calendar_not_set)
            val valueNotSet = appContext.getString(R.string.id_for_no_calendars_available)
            val calendarId = getString(appContext, key, defaultValue)
            return !(calendarId == defaultValue || calendarId == valueNotSet)
        }

        @JvmStatic
        fun isFirstRun(appContext: Context): Boolean {
            val key = appContext.getString(R.string.first_run_preference_key)
            return getBoolean(appContext, key, true)
        }

        @JvmStatic
        fun setFirstRun(appContext: Context, value: Boolean) {
            val key = appContext.getString(R.string.first_run_preference_key)
            putBoolean(appContext, key, value)
        }

        @JvmStatic
        fun useLocalizedDigits(appContext: Context): Boolean {
            val key = appContext.getString(R.string.use_localized_digits_key)
            return getSharedPreferences(appContext).getBoolean(key, false)
        }

        @JvmStatic
        fun getBoolean(appContext: Context, key: String?, defaultValue: Boolean): Boolean {
            return getSharedPreferences(appContext).getBoolean(key, defaultValue)
        }

        @JvmStatic
        fun putBoolean(appContext: Context, key: String?, value: Boolean) {
            getSharedPreferences(appContext)
                    .edit()
                    .putBoolean(key, value)
                    .apply()
        }

        @JvmStatic
        fun getInt(appContext: Context, key: String?, defaultValue: Int): Int {
            return getSharedPreferences(appContext).getInt(key, defaultValue)
        }

        @JvmStatic
        fun putInt(appContext: Context, key: String?, value: Int) {
            getSharedPreferences(appContext)
                    .edit()
                    .putInt(key, value)
                    .apply()
        }

        @JvmStatic
        fun getString(appContext: Context, key: String, defaultValue: String): String {
            return getSharedPreferences(appContext).getString(key, defaultValue) ?: ""
        }

        @JvmStatic
        fun putString(appContext: Context, key: String?, value: String) {
            getSharedPreferences(appContext)
                    .edit()
                    .putString(key, value)
                    .apply()
        }
    }
}