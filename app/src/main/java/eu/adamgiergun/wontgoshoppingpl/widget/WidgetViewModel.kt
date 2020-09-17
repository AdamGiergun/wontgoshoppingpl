package eu.adamgiergun.wontgoshoppingpl.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.common.setLocalizedConst
import eu.adamgiergun.wontgoshoppingpl.common.today
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity

internal class WidgetViewModel private constructor(val layoutId: Int, val text: CharSequence, val pendingIntent: PendingIntent?) {

    internal companion object {
        const val widgetTextViewId = R.id.appwidget_text

        suspend fun newInstance(appContext: Context?): WidgetViewModel {
            val nearestDay = getNearestDay(appContext!!)
            val intent = Intent(appContext, MainActivity::class.java)
            return WidgetViewModel(
                    if (nearestDay!!.isCommerceSunday) R.layout.app_widget_shopping else R.layout.app_widget_no_shopping,
                    nearestDay.widgetText,
                    PendingIntent.getActivity(appContext, 0, intent, 0)
            )
        }

        private suspend fun getNearestDay(appContext: Context): Day? {
            val database = AppDatabase.getInstance(appContext).eventDao()
            return Day.getDayForWidget(database.getFirstEventNotBefore(today)!!)
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

        fun setLocalizedConstants(appContext: Context) {
            setLocalizedConst(appContext)
        }
    }
}