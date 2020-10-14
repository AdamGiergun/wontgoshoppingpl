package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

internal object FirebaseEvent {
    private const val FULL_TEXT_TAG = "full_text"

    fun logAppTheme(appContext: Context, isLookRich: Boolean) {
        val fullText = if (isLookRich) Look.RICH else Look.MINI
        log(appContext, Type.APP_THEME, fullText)
    }

    fun logCalendarEvent(appContext: Context, listAdded: Boolean, result: Boolean) {
        val fullText = (if (listAdded) CalendarEvent.LIST else CalendarEvent.SINGLE) + if (result) "" else CalendarEvent.FAILED
        log(appContext, Type.CALENDAR_EVENT, fullText)
    }

    fun logAppWidgetUpdated(appContext: Context) {
        log(appContext, Type.APP_WIDGET, Widget.UPDATED)
    }

    fun logAppWidgetFirstAdded(appContext: Context) {
        log(appContext, Type.APP_WIDGET, Widget.FIRST_ADDED_TO_HOME_SCREEN)
    }

    fun logAppWidgetLastRemoved(appContext: Context) {
        log(appContext, Type.APP_WIDGET, Widget.LAST_REMOVED_FROM_HOME_SCREEN)
    }

    private fun log(appContext: Context, event: String, fullText: String) {
        FirebaseAnalytics.getInstance(appContext)
                .logEvent(event, getFullTextParams(fullText))
    }

    private fun getFullTextParams(fullText: String): Bundle {
        return Bundle().apply {
            putString(FULL_TEXT_TAG, fullText)
        }
    }

    private object Type {
        const val APP_THEME = "app_theme"
        const val CALENDAR_EVENT = "calendar_event"
        const val APP_WIDGET = "app_widget"
    }

    private object Look {
        const val RICH = "look_rich"
        const val MINI = "look_mini"
    }

    private object CalendarEvent {
        const val SINGLE = "single"
        const val LIST = "list"
        const val FAILED = "_failed"
    }

    private object Widget {
        const val UPDATED = "updated"
        const val FIRST_ADDED_TO_HOME_SCREEN = "first_added"
        const val LAST_REMOVED_FROM_HOME_SCREEN = "last_removed"
    }
}