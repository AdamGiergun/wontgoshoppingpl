package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseEvent {

    public class Type {
        public static final String APP_THEME = "app_theme";
        public static final String CALENDAR_EVENT = "calendar_event";
        public static final String APP_WIDGET = "app_widget";
    }

    public class LookFullText {
        public static final String RICH = "look_rich";
        public static final String MINI = "look_mini";
    }

    public class CalendarFullText {
        public static final String SINGLE = "single";
        public static final String LIST = "list";
        public static final String FAILED = " failed";
    }

    public class WidgetFullText {
        public static final String UPDATED = "updated";
        public static final String FIRST_ADDED_TO_HOME_SCREEN = "first_added";
        public static final String LAST_REMOVED_FROM_HOME_SCREEN = "last_removed";
    }

    private static final String FULL_TEXT = "full_text";

    public static void log(Context appContext, String event, String fullText) {
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(appContext);
        mFirebaseAnalytics.logEvent(event, getFullTextParams(fullText));
    }

    private static Bundle getFullTextParams(String fullText) {
        Bundle params = new Bundle();
        params.putString(FULL_TEXT, fullText);
        return params;
    }



/* example method
    private void logFirebaseEvent(Context context, String event, String full_text) {
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString(FULL_TEXT, full_text);
        mFirebaseAnalytics.logEvent(event, params);
    }
    */
}
