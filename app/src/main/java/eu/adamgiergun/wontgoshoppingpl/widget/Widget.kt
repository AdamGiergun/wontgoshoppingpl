package eu.adamgiergun.wontgoshoppingpl.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.widget.WidgetViewModel.Companion.logFirstAdded
import eu.adamgiergun.wontgoshoppingpl.widget.WidgetViewModel.Companion.logLastRemoved
import eu.adamgiergun.wontgoshoppingpl.widget.WidgetViewModel.Companion.logUpdated
import eu.adamgiergun.wontgoshoppingpl.widget.WidgetViewModel.Companion.newInstance

class Widget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        val remoteViews = newRemoteViews(context)
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
        logUpdated(context.applicationContext)
    }

    private fun newRemoteViews(context: Context): RemoteViews {
        val widgetViewModel = newInstance(context.applicationContext)
        val views = RemoteViews(context.packageName, widgetViewModel.layoutId)
        views.setTextViewText(R.id.appwidget_text, widgetViewModel.text)
        views.setOnClickPendingIntent(R.id.appwidget_text, widgetViewModel.pendingIntent)
        return views
    }

    override fun onEnabled(context: Context) {
        logFirstAdded(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        logLastRemoved(context.applicationContext)
    }
}