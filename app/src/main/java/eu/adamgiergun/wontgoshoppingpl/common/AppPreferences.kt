package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import eu.adamgiergun.mykotlinlibrary.MyAppPreferences
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.day.Day.Type.Companion.newSet

internal object AppPreferences : MyAppPreferences() {

    fun isLookRich(context: Context): Boolean {
        val key = context.getString(R.string.app_look_preference_key)
        return getBoolean(context, key, false)
    }

    fun setLookRich(context: Context, value: Boolean) {
        val key = context.getString(R.string.app_look_preference_key)
        putBoolean(context, key, value)
    }

    fun storedDayTypeSet(context: Context): Day.Type.Set {
        return newSet(
                showNonCommerceDays(context),
                showHolidays(context),
                showCommerceSundays(context))
    }

    private fun showCommerceSundays(context: Context): Boolean {
        val key = context.getString(R.string.include_commerce_sundays_preference_key)
        return getBoolean(context, key, true)
    }

    private fun showHolidays(context: Context): Boolean {
        val key = context.getString(R.string.include_holidays_preference_key)
        return getBoolean(context, key, true)
    }

    private fun showNonCommerceDays(context: Context): Boolean {
        val key = context.getString(R.string.include_non_commerce_days_preference_key)
        return getBoolean(context, key, true)
    }

    fun wasAppWidgetInfoDisplayed(context: Context): Boolean {
        val key = context.getString(R.string.key_app_widget_info_displayed)
        return getBoolean(context, key, false)
    }

    fun setAppWidgetInfoDisplayed(context: Context) {
        val key = context.getString(R.string.key_app_widget_info_displayed)
        putBoolean(context, key, true)
    }

    fun getAppWidgetTextSize(context: Context): String {
        val key = context.getString(R.string.key_app_widget_text_size)
        return getString(context, key, "14")
    }

    fun getUsePolishTimeZone(context: Context): Boolean {
        val key = context.getString(R.string.key_use_pl_time_zone)
        return getBoolean(context, key, true)
    }

    fun getReminderDaysBefore(context: Context): Int {
        val key = context.getString(R.string.default_reminder_days_before_preference_key)
        val defaultValue = context.resources.getInteger(R.integer.reminder_days_before_default_value)
        return getInt(context, key, defaultValue)
    }

    fun setReminderDaysBefore(value: Int?, context: Context) {
        value?.let {
            val key = context.getString(R.string.default_reminder_days_before_preference_key)
            putInt(context, key, it)
        }
    }

    fun getReminderTime(context: Context): String {
        val key = context.getString(R.string.default_reminder_time_preference_key)
        val defaultValue = context.getString(R.string.default_reminder_time_preference_default_value)
        return getString(context, key, defaultValue)
    }

    fun setReminderTime(value: String?, context: Context) {
        value?.let {
            val key = context.getString(R.string.default_reminder_time_preference_key)
            putString(context, key, it)
        }
    }

    fun getDefaultCalendarId(context: Context) = MyAppPreferences.getDefaultCalendarId(context)
    fun isCalendarSet(context: Context) = MyAppPreferences.isCalendarSet(context)
    fun isFirstRun(context: Context) = MyAppPreferences.isFirstRun(context)
    fun setFirstRun(context: Context, value: Boolean) = MyAppPreferences.setFirstRun(context, value)
    fun isUseGoogleCalendarSet(context: Context) = MyAppPreferences.isUseGoogleCalendarSet(context)
}