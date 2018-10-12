package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;

class AppPreferences {
    private SharedPreferences sharedPreferences;

    AppPreferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    String getDefaultCalendarId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.default_calendar_preference_key);
        String defaultValue = context.getString(R.string.id_for_default_calendar_not_set);
        return prefs.getString(key, defaultValue);
    }

    Integer getReminderDays(Context context) {
        String key = context.getString(R.string.default_reminder_days_before_preference_key);
        Resources resources = context.getResources();
        int defaultValue = resources.getInteger(R.integer.reminder_days_before_default_value);
        return sharedPreferences.getInt(key, defaultValue);
    }

    SimpleTime getReminderTime(Context context) {
        String key = context.getString(R.string.default_reminder_time_preference_key);
        String defaultValue = context.getString(R.string.default_reminder_time_preference_default_value);
        String time = sharedPreferences.getString(key, defaultValue);
        return new SimpleTime(time);
    }

    boolean isUseGoogleCalendarSet(Context context) {
        String key = context.getString(R.string.use_google_calendar_preference_key);
        return getBooleanValueOfPreference(key, false);
    }

    private boolean getBooleanValueOfPreference(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    boolean showHolidays(Context context) {
        String key = context.getString(R.string.include_holidays_preference_key);
        return getBooleanValueOfPreference(key, true);
    }
}
