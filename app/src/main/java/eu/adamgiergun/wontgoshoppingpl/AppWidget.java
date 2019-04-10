package eu.adamgiergun.wontgoshoppingpl;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.google.firebase.analytics.FirebaseAnalytics;

import eu.adamgiergun.wontgoshoppingpl.days.LimitedCommerceDay;
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity;

public class AppWidget extends AppWidgetProvider implements LogFirebaseEvent {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        logFirebaseEvent(context, APP_WIDGET, UPDATED);
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        LimitedCommerceDay nextDay = LimitedCommerceDay.getNextDay(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), getWidgetLayout(nextDay));
        views.setTextViewText(R.id.appwidget_text, nextDay.getWidgetText());
        setListener(context, views);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int getWidgetLayout(LimitedCommerceDay day) {
        switch (day.getType()) {
            case HOLIDAY:
            case NON_COMMERCE_DAY:
                return R.layout.app_widget_no_shopping;
            case COMMERCE_SUNDAY:
                return R.layout.app_widget_shopping;
            default:
                return 0;
        }
    }

    private static void setListener(Context context, RemoteViews views) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent,0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
    }

    @Override
    public void onEnabled(Context context) {
        logFirebaseEvent(context, APP_WIDGET, FIRST_ADDED_TO_HOME_SCREEN);
    }

    @Override
    public void onDisabled(Context context) {
        logFirebaseEvent(context, APP_WIDGET, LAST_REMOVED_FROM_HOME_SCREEN);
    }

    @Override
    public void logFirebaseEvent(Context context, String event, String full_text) {
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString("full_text", full_text);
        mFirebaseAnalytics.logEvent("app_widget", params);
    }
}

