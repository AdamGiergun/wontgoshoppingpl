package eu.adamgiergun.wontgoshoppingpl.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent;
import eu.adamgiergun.wontgoshoppingpl.day.Day;

class ViewModel {
    private Day nearestDay;
    private PendingIntent pendingIntent;

    private ViewModel() {}

    static ViewModel newInstance(Context appContext) {
        ViewModel viewModel = new ViewModel();
        viewModel.nearestDay = Day.List.getFirst(appContext);
        Intent intent = new Intent(appContext, eu.adamgiergun.wontgoshoppingpl.main.Activity.class);
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
        FirebaseEvent.logAppWidgetUpdated(appContext);
    }

    static void logFirstAdded(Context appContext) {
        FirebaseEvent.logAppWidgetFirstAdded(appContext);
    }

    static void logLastRemoved(Context appContext) {
        FirebaseEvent.logAppWidgetLastRemoved(appContext);
    }
}
