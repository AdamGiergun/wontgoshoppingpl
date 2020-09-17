package eu.adamgiergun.wontgoshoppingpl.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.common.setLocalizedConst
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.AppDatabase
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity
import java.lang.StringBuilder
import java.util.*

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

        private val today
            get() =
                StringBuilder().run {
                    fun get0ifLessThen10(value: Int): String {
                        return if (value < 10) "0" else ""
                    }

                    val today = Calendar.getInstance()
                    val month = today.get(Calendar.MONTH) + 1
                    val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)

                    append(today.get(Calendar.YEAR))
                    append("_")
                    append(get0ifLessThen10(month))
                    append(month)
                    append("_")
                    append(get0ifLessThen10(dayOfMonth))
                    append(dayOfMonth)
                    toString()
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