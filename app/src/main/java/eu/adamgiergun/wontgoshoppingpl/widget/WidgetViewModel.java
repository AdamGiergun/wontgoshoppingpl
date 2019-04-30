package eu.adamgiergun.wontgoshoppingpl.widget;

import android.content.Context;

import eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent;

import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.Type.APP_WIDGET;
import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.WidgetFullText.*;

class WidgetViewModel {

    private WidgetViewModel() {
        throw new AssertionError();
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
