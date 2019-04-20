package eu.adamgiergun.wontgoshoppingpl.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

public class AppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        WidgetViewModel.logUpdated(context);
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        appWidgetManager.updateAppWidget(appWidgetId, WidgetViewModel.newRemoteViews(context));
    }

    @Override
    public void onEnabled(Context context) {
        WidgetViewModel.logFirstAdded(context);
    }

    @Override
    public void onDisabled(Context context) {
        WidgetViewModel.logLastRemoved(context);
    }
}

