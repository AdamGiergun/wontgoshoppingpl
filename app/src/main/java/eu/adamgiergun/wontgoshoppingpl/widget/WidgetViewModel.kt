package eu.adamgiergun.wontgoshoppingpl.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.common.today
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.day.DayPalette
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity

internal class WidgetViewModel private constructor(
    val layoutId: Int,
    val text: CharSequence,
    val pendingIntent: PendingIntent
) {

    internal companion object {
        // Celowo `val`, nie `const val`: identyfikator zasobu nie jest stałą kompilacji
        // przy nietranzytywnych klasach R w AGP 9, a próba jego zwinięcia wywraca
        // ewaluator stałych Kotlina (AssertionError w buildzie przyrostowym).
        val widgetTextViewId = R.id.appwidget_text

        suspend fun newInstance(appContext: Context): WidgetViewModel {
            if (AppPreferences.isFirstRun(appContext)) {
                AppDatabase.getInstance(appContext).eventDao().initialize()
            }
            // Zestaw kolorystyczny czyta ten, kto rysuje — patrz DayPalette.
            val zestaw = AppPreferences.colourSet(appContext)
            var layoutId = DayPalette.widgetLayout(Day.Type.NON_COMMERCE_DAY, zestaw)
            var widgetText: CharSequence = ""
            getNearestDay(appContext)?.let {
                layoutId = DayPalette.widgetLayout(it.type, zestaw)
                widgetText = it.widgetText
            }
            val intent = Intent(appContext, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, FLAG_IMMUTABLE)
            return WidgetViewModel(layoutId, widgetText, pendingIntent)
        }

        private suspend fun getNearestDay(appContext: Context): Day? {
            val database = AppDatabase.getInstance(appContext).eventDao()
            return Day.getDayForWidget(database.getFirstEventNotBefore(today), appContext)
        }

        fun logUpdated(appContext: Context) {
            FirebaseEvent.logAppWidgetUpdated(appContext)
        }

        fun logFirstAdded(appContext: Context) {
            FirebaseEvent.logAppWidgetFirstAdded(appContext)
        }

        fun logLastRemoved(appContext: Context) {
            FirebaseEvent.logAppWidgetLastRemoved(appContext)
        }
    }
}