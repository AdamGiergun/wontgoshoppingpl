package eu.adamgiergun.wontgoshoppingpl.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Widget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        if (context == null || appWidgetManager == null || appWidgetIds == null) return

        // goAsync jest tu konieczne, a nie kosmetyczne. Bez niego onReceive kończy się,
        // zanim korutyna zdąży odpytać bazę: `launch` tylko ją kolejkuje. System uznaje
        // wtedy odbiornik za zakończony i — skoro aplikacja nie ma innego działającego
        // komponentu — ubija proces w locie. updateAppWidget nigdy się nie wykonuje,
        // a launcher zostaje z samym initialLayout, czyli gołą ikoną bez daty.
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default + SupervisorJob()).launch {
            try {
                val remoteViews = newRemoteViews(context)
                appWidgetIds.forEach { appWidgetId ->
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                }
                WidgetViewModel.logUpdated(context.applicationContext)
            } catch (throwable: Throwable) {
                Log.w(TAG, "Nie udało się odświeżyć widgetu", throwable)
            } finally {
                // Musi zostać wywołane zawsze, także przy błędzie — inaczej system
                // trzyma proces aż do wyczerpania budżetu i zgłasza ANR odbiornika.
                pendingResult.finish()
            }
        }
    }

    private suspend fun newRemoteViews(context: Context): RemoteViews {
        val widgetViewModel = WidgetViewModel.newInstance(context.applicationContext)
        return RemoteViews(
            context.packageName,
            widgetViewModel.layoutId
        ).apply {
            setTextViewText(WidgetViewModel.widgetTextViewId, widgetViewModel.text)
            setTextViewTextSize(
                WidgetViewModel.widgetTextViewId,
                WidgetViewModel.widgetTextSizeUnit,
                widgetViewModel.textSize
            )
            setOnClickPendingIntent(WidgetViewModel.widgetTextViewId, widgetViewModel.pendingIntent)
        }
    }

    override fun onEnabled(context: Context) {
        WidgetViewModel.logFirstAdded(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        WidgetViewModel.logLastRemoved(context.applicationContext)
    }

    private companion object {
        const val TAG = "WGS_Widget"
    }
}
