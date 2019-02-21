package eu.adamgiergun.wontgoshoppingpl;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class AppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        LimitedCommerceDay nextDay = LimitedCommerceDay.getNextDay(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), nextDay.getWidgetLayout(context));
        views.setTextViewText(R.id.appwidget_text, nextDay.getWidgetText());
        setListener(context, views);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void setListener(Context context, RemoteViews views) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0, intent,0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
    }

/*
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    */
}

