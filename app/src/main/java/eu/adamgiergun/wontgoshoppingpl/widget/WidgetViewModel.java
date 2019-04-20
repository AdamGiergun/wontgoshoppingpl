package eu.adamgiergun.wontgoshoppingpl.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.google.firebase.analytics.FirebaseAnalytics;

import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.day.Day;
import eu.adamgiergun.wontgoshoppingpl.day.ListOfDays;
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity;

import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.*;

class WidgetViewModel {

    static void logUpdated(Context context) {
        logFirebaseEvent(context, UPDATED);
    }


    static void logFirstAdded(Context context) {
        logFirebaseEvent(context, FIRST_ADDED_TO_HOME_SCREEN);
    }

    static void logLastRemoved(Context context) {
        logFirebaseEvent(context, LAST_REMOVED_FROM_HOME_SCREEN);
    }

    private static void logFirebaseEvent(Context context, String full_text) {
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString(FULL_TEXT, full_text);
        mFirebaseAnalytics.logEvent(APP_WIDGET, params);
    }

    static RemoteViews newRemoteViews(Context context) {
        WidgetData widgetData = WidgetData.newInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), widgetData.layout);
        views.setTextViewText(R.id.appwidget_text, widgetData.text);
        views.setOnClickPendingIntent(R.id.appwidget_text, widgetData.pendingIntent);
        return views;
    }

    private static class WidgetData {
        int layout;
        CharSequence text;
        PendingIntent pendingIntent;

        private static WidgetData newInstance(Context context) {
            WidgetData widgetData = new WidgetData();
            Day day = ListOfDays.getFirst(context);

            widgetData.layout = (day.getType() == Day.Type.COMMERCE_SUNDAY) ? R.layout.app_widget_shopping : R.layout.app_widget_no_shopping;
            widgetData.text = day.getWidgetText();
            Intent intent = new Intent(context, MainActivity.class);
            widgetData.pendingIntent = PendingIntent.getActivity(context,0, intent,0);

            return widgetData;
        }
    }
}
