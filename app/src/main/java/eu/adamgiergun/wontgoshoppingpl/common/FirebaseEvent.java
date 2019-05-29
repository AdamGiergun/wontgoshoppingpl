package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseEvent {

    private class Type {
        private static final String APP_THEME = "app_theme";
        private static final String CALENDAR_EVENT = "calendar_event";
        private static final String APP_WIDGET = "app_widget";
    }

    private class Look {
        private static final String RICH = "look_rich";
        private static final String MINI = "look_mini";
    }

    private class CalendarEvent {
        private static final String SINGLE = "single";
        private static final String LIST = "list";
        private static final String FAILED = "_failed";
    }

    private class Widget {
        private static final String UPDATED = "updated";
        private static final String FIRST_ADDED_TO_HOME_SCREEN = "first_added";
        private static final String LAST_REMOVED_FROM_HOME_SCREEN = "last_removed";
    }

    private static final String FULL_TEXT_TAG = "full_text";

    public static void logAppTheme(Context appContext, boolean isLookRich) {
        String fullText = isLookRich ? Look.RICH : Look.MINI;
        log(appContext, Type.APP_THEME, fullText);
    }

    public static void logCalendarEvent(Context appContext, boolean listAdded, boolean result) {
        String fullText = (listAdded ? CalendarEvent.LIST : CalendarEvent.SINGLE) + (result ? "" : CalendarEvent.FAILED);
        log(appContext, Type.CALENDAR_EVENT, fullText);
    }

    public static void logAppWidgetUpdated(Context appContext) {
        log(appContext, Type.APP_WIDGET, Widget.UPDATED);
    }

    public static void logAppWidgetFirstAdded(Context appContext) {
        log(appContext, Type.APP_WIDGET, Widget.FIRST_ADDED_TO_HOME_SCREEN);
    }

    public static void logAppWidgetLastRemoved(Context appContext) {
        log(appContext, Type.APP_WIDGET, Widget.LAST_REMOVED_FROM_HOME_SCREEN);
    }

    private static void log(Context appContext, String event, String fullText) {
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(appContext);
        mFirebaseAnalytics.logEvent(event, getFullTextParams(fullText));
    }

    private static Bundle getFullTextParams(String fullText) {
        Bundle params = new Bundle();
        params.putString(FULL_TEXT_TAG, fullText);
        return params;
    }
}
