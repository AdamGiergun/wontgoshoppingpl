package eu.adamgiergun.wontgoshoppingpl.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import eu.adamgiergun.wontgoshoppingpl.R;

public class Widget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        RemoteViews remoteViews = newRemoteViews(context);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        ViewModel.logUpdated(context.getApplicationContext());
    }

    private RemoteViews newRemoteViews(Context context) {
        ViewModel viewModel = ViewModel.newInstance(context.getApplicationContext());
        RemoteViews views = new RemoteViews(context.getPackageName(), viewModel.getLayoutId());
        views.setTextViewText(R.id.appwidget_text, viewModel.getText());
        views.setOnClickPendingIntent(R.id.appwidget_text, viewModel.getPendingIntent());
        return views;
    }

    @Override
    public void onEnabled(Context context) {
        ViewModel.logFirstAdded(context.getApplicationContext());
    }

    @Override
    public void onDisabled(Context context) {
        ViewModel.logLastRemoved(context.getApplicationContext());
    }
}

