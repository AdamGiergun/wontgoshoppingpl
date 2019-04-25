package eu.adamgiergun.wontgoshoppingpl.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.day.Day;
import eu.adamgiergun.wontgoshoppingpl.day.ListOfDays;
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity;

public class AppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        RemoteViews remoteViews = newRemoteViews(context);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        WidgetViewModel.logUpdated(context.getApplicationContext());
    }

    private RemoteViews newRemoteViews(Context context) {
        WidgetData widgetData = WidgetData.newInstance(context.getApplicationContext());
        RemoteViews views = new RemoteViews(context.getPackageName(), widgetData.layout);
        views.setTextViewText(R.id.appwidget_text, widgetData.text);
        views.setOnClickPendingIntent(R.id.appwidget_text, widgetData.pendingIntent);
        return views;
    }

    @Override
    public void onEnabled(Context context) {
        WidgetViewModel.logFirstAdded(context.getApplicationContext());
    }

    @Override
    public void onDisabled(Context context) {
        WidgetViewModel.logLastRemoved(context.getApplicationContext());
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

