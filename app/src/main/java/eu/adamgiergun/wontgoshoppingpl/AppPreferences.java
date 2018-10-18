package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.content.SharedPreferences;

import eu.adamgiergun.mylibrary.MyAppPreferences;

class AppPreferences extends MyAppPreferences {
    private SharedPreferences sharedPreferences;

    AppPreferences(Context context) {
        super(context);
    }

    boolean showHolidays(Context context) {
        String key = context.getString(R.string.include_holidays_preference_key);
        return getBooleanValueOfPreference(key, true);
    }

    boolean isLookRich(Context context) {
        String key = context.getString(R.string.app_look_preference_key);
        return getBooleanValueOfPreference(key, false);
    }
}
