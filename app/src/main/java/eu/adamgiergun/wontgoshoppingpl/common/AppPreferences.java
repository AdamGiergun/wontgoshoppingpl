package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;

import eu.adamgiergun.mylibrary.MyAppPreferences;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.day.Day;

public class AppPreferences extends MyAppPreferences {

    private static AppPreferences soleInstance;

    private AppPreferences(Context appContext) {
        super(appContext);
    }

    public static AppPreferences getInstance(Context appContext) {
        return (soleInstance == null) ? (soleInstance = new AppPreferences(appContext)) : soleInstance;
    }

    public boolean isLookRich(Context appContext) {
        String key = appContext.getString(R.string.app_look_preference_key);
        return getBoolean(key, false);
    }

    void setLookRich(Context appContext, boolean value) {
        String key = appContext.getString(R.string.app_look_preference_key);
        putBoolean(key, value);
    }

    public Day.Type.List newDayTypesList(Context appContext) {
        return Day.Type.newList(
                showNonCommerceDays(appContext),
                showHolidays(appContext),
                showCommerceSundays(appContext));
    }

    private boolean showCommerceSundays(Context appContext) {
        String key = appContext.getString(R.string.include_commerce_sundays_preference_key);
        return getBoolean(key, true);
    }

    private boolean showHolidays(Context appContext) {
        String key = appContext.getString(R.string.include_holidays_preference_key);
        return getBoolean(key, true);
    }

    private boolean showNonCommerceDays(Context appContext) {
        String key = appContext.getString(R.string.include_non_commerce_days_preference_key);
        return getBoolean(key, true);
    }

    public boolean wasAppWidgetInfoDisplayed(Context appContext) {
        String key = appContext.getString(R.string.key_app_widget_info_displayed);
        return getBoolean(key, false);
    }

    public void setAppWidgetInfoDisplayed(Context appContext) {
        String key = appContext.getString(R.string.key_app_widget_info_displayed);
        putBoolean(key, true);
    }

    public boolean getUsePolishTimeZone(Context appContext) {
        String key = appContext.getString(R.string.key_use_pl_time_zone);
        return getBoolean(key, true);
    }
}
