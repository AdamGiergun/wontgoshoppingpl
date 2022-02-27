package eu.adamgiergun.wontgoshoppingpl.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.util.TypedValue.COMPLEX_UNIT_SP
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.common.today
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity

internal class WidgetViewModel private constructor(
    val layoutId: Int,
    val text: CharSequence,
    val textSize: Float,
    val pendingIntent: PendingIntent
) {

    internal companion object {
        const val widgetTextViewId = R.id.appwidget_text
        const val widgetTextSizeUnit = COMPLEX_UNIT_SP

        suspend fun newInstance(appContext: Context): WidgetViewModel {
            if (AppPreferences.isFirstRun(appContext)) {
                AppDatabase.getInstance(appContext).eventDao().initialize()
            }
            var layoutId = R.layout.app_widget_no_shopping
            var widgetText: CharSequence = ""
            val textSize = AppPreferences.getAppWidgetTextSize(appContext).toFloat()
            getNearestDay(appContext)?.let {
                layoutId =
                    if (it.isCommerceSunday) R.layout.app_widget_shopping else R.layout.app_widget_no_shopping
                widgetText = it.widgetText
            }
            val intent = Intent(appContext, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                appContext,
                0,
                intent,
                if (SDK_INT < 23) 0 else FLAG_IMMUTABLE
            )
            return WidgetViewModel(layoutId, widgetText, textSize, pendingIntent)
        }

        private suspend fun getNearestDay(appContext: Context): Day? {
            val database = AppDatabase.getInstance(appContext).eventDao()
            return Day.getDayForWidget(database.getFirstEventNotBefore(today))
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