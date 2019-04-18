package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;

import eu.adamgiergun.mylibrary.MyAppPreferences;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.days.LimitedCommerceDayType;

public class AppPreferences extends MyAppPreferences {

    private static AppPreferences soleInstance;

    private AppPreferences(Context context) {
        super(context);
    }

    public static AppPreferences getInstance(Context context) {
        if (soleInstance == null) {
            soleInstance = new AppPreferences(context);
        }
        return soleInstance;
    }

    public boolean isLookRich(Context context) {
        String key = context.getString(R.string.app_look_preference_key);
        return getBoolean(key, false);
    }

    void setLookRich(Context context, boolean value) {
        String key = context.getString(R.string.app_look_preference_key);
        putBoolean(key, value);
    }

    public LimitedCommerceDayType.TypesList newLimitedCommerceDayTypesList(Context context) {
        return LimitedCommerceDayType.newTypesList(
                showNonCommerceDays(context),
                showHolidays(context),
                showCommerceSundays(context));
    }

    private boolean showCommerceSundays(Context context) {
        String key = context.getString(R.string.include_commerce_sundays_preference_key);
        return getBoolean(key, true);
    }

    private boolean showHolidays(Context context) {
        String key = context.getString(R.string.include_holidays_preference_key);
        return getBoolean(key, true);
    }

    private boolean showNonCommerceDays(Context context) {
        String key = context.getString(R.string.include_non_commerce_days_preference_key);
        return getBoolean(key, true);
    }

    public boolean wasAppWidgetInfoDisplayed(Context context) {
        String key = context.getString(R.string.key_app_widget_info_displayed);
        return getBoolean(key, false);
    }

    public void setAppWidgetInfoDisplayed(Context context) {
        String key = context.getString(R.string.key_app_widget_info_displayed);
        putBoolean(key, true);
    }
}
