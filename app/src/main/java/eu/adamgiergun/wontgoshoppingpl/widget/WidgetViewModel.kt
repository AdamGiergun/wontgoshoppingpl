package eu.adamgiergun.wontgoshoppingpl.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.main.Activity

class WidgetViewModel private constructor() {
    private var nearestDay: Day? = null
    var pendingIntent: PendingIntent? = null
        private set
    val layoutId: Int
        get() = if (nearestDay!!.isCommerceSunday) R.layout.app_widget_shopping else R.layout.app_widget_no_shopping
    val text: CharSequence
        get() = nearestDay!!.widgetText

    companion object {
        @JvmStatic
        fun newInstance(appContext: Context?): WidgetViewModel {
            val widgetViewModel = WidgetViewModel()
            widgetViewModel.nearestDay = Day.List.getFirst(appContext)
            val intent = Intent(appContext, Activity::class.java)
            widgetViewModel.pendingIntent = PendingIntent.getActivity(appContext, 0, intent, 0)
            return widgetViewModel
        }

        @JvmStatic
        fun logUpdated(appContext: Context?) {
            FirebaseEvent.logAppWidgetUpdated(appContext)
        }

        @JvmStatic
        fun logFirstAdded(appContext: Context?) {
            FirebaseEvent.logAppWidgetFirstAdded(appContext)
        }

        @JvmStatic
        fun logLastRemoved(appContext: Context?) {
            FirebaseEvent.logAppWidgetLastRemoved(appContext)
        }
    }
}