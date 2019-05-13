package eu.adamgiergun.wontgoshoppingpl.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent;
import eu.adamgiergun.wontgoshoppingpl.day.Day;
import eu.adamgiergun.wontgoshoppingpl.main.Activity;

import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.Type.APP_WIDGET;
import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.WidgetFullText.*;

class ViewModel {
    private Day nearestDay;
    private PendingIntent pendingIntent;

    private ViewModel() {}

    static ViewModel newInstance(Context appContext) {
        ViewModel viewModel = new ViewModel();
        viewModel.nearestDay = Day.List.getFirst(appContext);
        Intent intent = new Intent(appContext, Activity.class);
        viewModel.pendingIntent = PendingIntent.getActivity(appContext,0, intent,0);
        return viewModel;
    }

    int getLayoutId() {
        return nearestDay.isCommerceSunday() ? R.layout.app_widget_shopping : R.layout.app_widget_no_shopping;
    }

    CharSequence getText() {
        return nearestDay.getWidgetText();
    }

    PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    static void logUpdated(Context appContext) {
        logFirebaseEvent(appContext, UPDATED);
    }

    static void logFirstAdded(Context appContext) {
        logFirebaseEvent(appContext, FIRST_ADDED_TO_HOME_SCREEN);
    }

    static void logLastRemoved(Context appContext) {
        logFirebaseEvent(appContext, LAST_REMOVED_FROM_HOME_SCREEN);
    }

    private static void logFirebaseEvent(Context appContext, String fullText) {
        FirebaseEvent.log(appContext, APP_WIDGET, fullText);
    }
}
