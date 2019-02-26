package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;

import eu.adamgiergun.mylibrary.MyAppPreferences;

class AppPreferences extends MyAppPreferences {

    AppPreferences(Context context) {
        super(context);
    }

    boolean showCommerceSundays(Context context) {
        String key = context.getString(R.string.include_commerce_sundays_preference_key);
        return getBoolean(key, true);
    }

    boolean showHolidays(Context context) {
        String key = context.getString(R.string.include_holidays_preference_key);
        return getBoolean(key, true);
    }

    boolean showNonCommerceDays(Context context) {
        String key = context.getString(R.string.include_non_commerce_days_preference_key);
        return getBoolean(key, true);
    }

    boolean isLookRich(Context context) {
        String key = context.getString(R.string.app_look_preference_key);
        return getBoolean(key, false);
    }

    void setLookRich(Context context, boolean value) {
        String key = context.getString(R.string.app_look_preference_key);
        putBoolean(key, value);
    }

    LimitedCommerceDayType.TypesList getLimitedCommerceDayTypes(Context context) {
        return LimitedCommerceDayType.getTypes(
                showNonCommerceDays(context),
                showHolidays(context),
                showCommerceSundays(context));
    }

    boolean wasAppWidgetInfoDisplayed(Context context) {
        String key = context.getString(R.string.key_app_widget_info_displayed);
        return getBoolean(key, false);
    }

    void setAppWidgetInfoDisplayed(Context context) {
        String key = context.getString(R.string.key_app_widget_info_displayed);
        putBoolean(key, true);
    }
}
