package eu.adamgiergun.wontgoshoppingpl.common;

public final class FirebaseEvent {

    public static final String APP_THEME = "app_theme";
    public static final String LOOK_RICH = "look_rich";
    public static final String LOOK_MINI = "look_mini";

    public static final String CALENDAR_EVENT = "calendar_event";
    public static final String SINGLE = "single";
    public static final String LIST = "list";
    public static final String FAILED = " failed";

    public static final String APP_WIDGET = "app_widget";
    public static final String UPDATED = "updated";
    public static final String FIRST_ADDED_TO_HOME_SCREEN = "first_added";
    public static final String LAST_REMOVED_FROM_HOME_SCREEN = "last_removed";

    public static final String FULL_TEXT = "full_text";

/* example method
    private void logFirebaseEvent(Context context, String event, String full_text) {
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString(FULL_TEXT, full_text);
        mFirebaseAnalytics.logEvent(event, params);
    }
    */
}
