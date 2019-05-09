package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;

import eu.adamgiergun.mylibrary.MyAppPreferences;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.day.Day.Type;

public class AppPreferences extends MyAppPreferences {

    public static boolean isLookRich(Context appContext) {
        String key = appContext.getString(R.string.app_look_preference_key);
        return AppPreferences.getBoolean(appContext, key, false);
    }

    static void setLookRich(Context appContext, boolean value) {
        String key = appContext.getString(R.string.app_look_preference_key);
        AppPreferences.putBoolean(appContext, key, value);
    }

    public static Type.Set newDayTypeSet(Context appContext) {
        return Type.newSet(
                showNonCommerceDays(appContext),
                showHolidays(appContext),
                showCommerceSundays(appContext));
    }

    private static boolean showCommerceSundays(Context appContext) {
        String key = appContext.getString(R.string.include_commerce_sundays_preference_key);
        return AppPreferences.getBoolean(appContext, key, true);
    }

    private static boolean showHolidays(Context appContext) {
        String key = appContext.getString(R.string.include_holidays_preference_key);
        return AppPreferences.getBoolean(appContext, key, true);
    }

    private static boolean showNonCommerceDays(Context appContext) {
        String key = appContext.getString(R.string.include_non_commerce_days_preference_key);
        return AppPreferences.getBoolean(appContext, key, true);
    }

    public static boolean wasAppWidgetInfoDisplayed(Context appContext) {
        String key = appContext.getString(R.string.key_app_widget_info_displayed);
        return AppPreferences.getBoolean(appContext, key, false);
    }

    public static void setAppWidgetInfoDisplayed(Context appContext) {
        String key = appContext.getString(R.string.key_app_widget_info_displayed);
        AppPreferences.putBoolean(appContext, key, true);
    }

    public static boolean getUsePolishTimeZone(Context appContext) {
        String key = appContext.getString(R.string.key_use_pl_time_zone);
        return AppPreferences.getBoolean(appContext, key, true);
    }
}
