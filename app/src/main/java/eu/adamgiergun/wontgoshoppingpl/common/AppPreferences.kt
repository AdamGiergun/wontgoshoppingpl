package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import androidx.preference.PreferenceManager
import eu.adamgiergun.mykotlinlibrary.MyAppPreferences
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.day.Day.Type.Companion.newSet

internal class AppPreferences {
    companion object: MyAppPreferences() {
        fun isLookRich(appContext: Context): Boolean {
            val key = appContext.getString(R.string.app_look_preference_key)
            return getBoolean(appContext, key, false)
        }

        fun setLookRich(appContext: Context, value: Boolean) {
            val key = appContext.getString(R.string.app_look_preference_key)
            putBoolean(appContext, key, value)
        }

        fun newDayTypeSet(appContext: Context): Day.Type.Set {
            return newSet(
                    showNonCommerceDays(appContext),
                    showHolidays(appContext),
                    showCommerceSundays(appContext))
        }

        private fun showCommerceSundays(appContext: Context): Boolean {
            val key = appContext.getString(R.string.include_commerce_sundays_preference_key)
            return getBoolean(appContext, key, true)
        }

        private fun showHolidays(appContext: Context): Boolean {
            val key = appContext.getString(R.string.include_holidays_preference_key)
            return getBoolean(appContext, key, true)
        }

        private fun showNonCommerceDays(appContext: Context): Boolean {
            val key = appContext.getString(R.string.include_non_commerce_days_preference_key)
            return getBoolean(appContext, key, true)
        }

        fun wasAppWidgetInfoDisplayed(appContext: Context): Boolean {
            val key = appContext.getString(R.string.key_app_widget_info_displayed)
            return getBoolean(appContext, key, false)
        }

        fun setAppWidgetInfoDisplayed(appContext: Context) {
            val key = appContext.getString(R.string.key_app_widget_info_displayed)
            putBoolean(appContext, key, true)
        }

        fun getUsePolishTimeZone(appContext: Context): Boolean {
            val key = appContext.getString(R.string.key_use_pl_time_zone)
            return getBoolean(appContext, key, true)
        }

        fun getReminderDaysBefore(appContext: Context): Int {
            val key = appContext.getString(R.string.default_reminder_days_before_preference_key)
            val defaultValue = appContext.resources.getInteger(R.integer.reminder_days_before_default_value)
            return getInt(appContext, key, defaultValue)
        }

        fun getReminderTime(appContext: Context): String {
            val key = appContext.getString(R.string.default_reminder_time_preference_key)
            val defaultValue = appContext.getString(R.string.default_reminder_time_preference_default_value)
            return getString(appContext, key, defaultValue)
        }

        fun getDefaultCalendarId(appContext: Context) = MyAppPreferences.getDefaultCalendarId(appContext)
        fun isCalendarSet(appContext: Context) = MyAppPreferences.isCalendarSet(appContext)
        fun isFirstRun(appContext: Context) = MyAppPreferences.isFirstRun(appContext)
        fun setFirstRun(appContext: Context, value: Boolean) = MyAppPreferences.setFirstRun(appContext, value)
        fun isUseGoogleCalendarSet(appContext: Context) = MyAppPreferences.isUseGoogleCalendarSet(appContext)
    }
}