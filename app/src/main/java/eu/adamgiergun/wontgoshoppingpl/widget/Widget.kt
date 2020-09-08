package eu.adamgiergun.wontgoshoppingpl.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class Widget : AppWidgetProvider() {

    override fun onUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray) {
        val remoteViews = newRemoteViews(context)
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
        WidgetViewModel.logUpdated(context.applicationContext)
    }

    private fun newRemoteViews(context: Context): RemoteViews {
        val widgetViewModel = WidgetViewModel.newInstance(context.applicationContext)
        return RemoteViews(
                context.packageName,
                widgetViewModel.layoutId
        ).apply {
            setTextViewText(WidgetViewModel.widgetTextViewId, widgetViewModel.text)
            setOnClickPendingIntent(WidgetViewModel.widgetTextViewId, widgetViewModel.pendingIntent)
        }
    }

    override fun onEnabled(context: Context) {
        WidgetViewModel.logFirstAdded(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        WidgetViewModel.logLastRemoved(context.applicationContext)
    }
}