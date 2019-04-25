package eu.adamgiergun.wontgoshoppingpl.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent;
import eu.adamgiergun.wontgoshoppingpl.day.*;
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity;

import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.Type.APP_WIDGET;
import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.WidgetFullText.*;

class WidgetViewModel {

    static void logUpdated(Context appContext) {
        logFirebaseEvent(appContext, UPDATED);
    }

    static void logFirstAdded(Context appContext) {
        logFirebaseEvent(appContext, FIRST_ADDED_TO_HOME_SCREEN);
    }

    static void logLastRemoved(Context appContext) {
        logFirebaseEvent(appContext, LAST_REMOVED_FROM_HOME_SCREEN);
    }

    private static void logFirebaseEvent(Context appContext, String fullText) {
        FirebaseEvent.log(appContext, APP_WIDGET, fullText);
    }

    static RemoteViews newRemoteViews(Context appContext) {
        WidgetData widgetData = WidgetData.newInstance(appContext);
        RemoteViews views = new RemoteViews(appContext.getPackageName(), widgetData.layout);
        views.setTextViewText(R.id.appwidget_text, widgetData.text);
        views.setOnClickPendingIntent(R.id.appwidget_text, widgetData.pendingIntent);
        return views;
    }

    private static class WidgetData {
        int layout;
        CharSequence text;
        PendingIntent pendingIntent;

        private static WidgetData newInstance(Context appContext) {
            WidgetData widgetData = new WidgetData();
            Day day = ListOfDays.getFirst(appContext);

            widgetData.layout = (day.getType() == Day.Type.COMMERCE_SUNDAY) ? R.layout.app_widget_shopping : R.layout.app_widget_no_shopping;
            widgetData.text = day.getWidgetText();
            Intent intent = new Intent(appContext, MainActivity.class);
            widgetData.pendingIntent = PendingIntent.getActivity(appContext,0, intent,0);

            return widgetData;
        }
    }
}
