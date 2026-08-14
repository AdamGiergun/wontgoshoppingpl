package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import eu.adamgiergun.wontgoshoppingpl.common.MyAppPreferences
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.day.Day.Type.Companion.newSet

internal object AppPreferences : MyAppPreferences() {


    /**
     * Wybrany zestaw kolorystyczny.
     *
     * Wartość jest ciągiem, a nie przełącznikiem logicznym, bo zestawów może
     * z czasem przybyć — a klucza preferencji nie da się potem zmienić bez
     * skasowania ustawienia użytkownika.
     *
     * Wartość domyślna, tak jak przy gęstości listy, żyje w trzech miejscach
     * i wszystkie muszą się zgadzać: tutaj, w xml/pref_look.xml
     * i w xml/preferences.xml.
     */
    fun colourSet(context: Context): ColourSet {
        val key = context.getString(R.string.colour_set_preference_key)
        return ColourSet.fromValue(getString(context, key, ColourSet.PIGMENTS.value))
    }

    /**
     * Zestawy kolorystyczne. `value` trafia do SharedPreferences i **nie wolno
     * go zmieniać** — zmiana skasowałaby wybór użytkownika.
     */
    enum class ColourSet(val value: String) {
        PIGMENTS("1"),
        CANDY("2");

        companion object {
            fun fromValue(v: String?) = entries.firstOrNull { it.value == v } ?: PIGMENTS
        }
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

    fun getUsePolishTimeZone(context: Context): Boolean {
        val key = context.getString(R.string.key_use_pl_time_zone)
        return getBoolean(context, key, true)
    }

    fun getReminderDaysBefore(context: Context): Int {
        val key = context.getString(eu.adamgiergun.wontgoshoppingpl.R.string.default_reminder_days_before_preference_key)
        val defaultValue = context.resources.getInteger(R.integer.reminder_days_before_default_value)
        return getInt(context, key, defaultValue)
    }

    fun setReminderDaysBefore(value: Int?, context: Context) {
        value?.let {
            val key = context.getString(eu.adamgiergun.wontgoshoppingpl.R.string.default_reminder_days_before_preference_key)
            putInt(context, key, it)
        }
    }

    fun getReminderTime(context: Context): String {
        val key = context.getString(eu.adamgiergun.wontgoshoppingpl.R.string.default_reminder_time_preference_key)
        val defaultValue = context.getString(eu.adamgiergun.wontgoshoppingpl.R.string.default_reminder_time_preference_default_value)
        return getString(context, key, defaultValue)
    }

    fun setReminderTime(value: String?, context: Context) {
        value?.let {
            val key = context.getString(eu.adamgiergun.wontgoshoppingpl.R.string.default_reminder_time_preference_key)
            putString(context, key, it)
        }
    }

    fun getDefaultCalendarId(context: Context) = MyAppPreferences.getDefaultCalendarId(context)
    fun isCalendarSet(context: Context) = MyAppPreferences.isCalendarSet(context)
    fun isFirstRun(context: Context) = MyAppPreferences.isFirstRun(context)
    fun setFirstRun(context: Context, value: Boolean) = MyAppPreferences.setFirstRun(context, value)
    fun isUseGoogleCalendarSet(context: Context) = MyAppPreferences.isUseGoogleCalendarSet(context)
}