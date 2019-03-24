package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;

interface LogFirebaseEvent {

    String APP_THEME = "app_theme";
    String LOOK_RICH = "look_rich";
    String LOOK_MINI = "look_mini";

    String CALENDAR_EVENT = "calendar_event";
    String SINGLE = "single";
    String LIST = "list";

    String APP_WIDGET = "app_widget";
    String UPDATED = "updated";
    String FIRST_ADDED_TO_HOME_SCREEN = "first_added";
    String LAST_REMOVED_FROM_HOME_SCREEN = "last_removed";


    void logFirebaseEvent(Context context, String event, String full_text);

    /* example body
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString("full_text", full_text);
        mFirebaseAnalytics.logEvent(event, params);
    */
}
