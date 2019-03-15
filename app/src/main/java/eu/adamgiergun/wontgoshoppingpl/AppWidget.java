package eu.adamgiergun.wontgoshoppingpl;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AppWidget extends AppWidgetProvider implements LogFirebaseEvent {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        logFirebaseEvent(context, "app widgets updated");
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        LimitedCommerceDay nextDay = LimitedCommerceDay.getNextDay(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), nextDay.getWidgetLayout());
        views.setTextViewText(R.id.appwidget_text, nextDay.getWidgetText());
        setListener(context, views);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setListener(Context context, RemoteViews views) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent,0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
    }

    @Override
    public void onEnabled(Context context) {
        logFirebaseEvent(context, "first app widget added to home screen");
    }

    @Override
    public void onDisabled(Context context) {
        logFirebaseEvent(context, "last app widget removed from home screen");
    }

    @Override
    public void logFirebaseEvent(Context context, String full_text) {
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString("full_text", full_text);
        mFirebaseAnalytics.logEvent("app_widget", params);
    }
}

