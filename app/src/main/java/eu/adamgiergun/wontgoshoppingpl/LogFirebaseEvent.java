package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;

//used only for some internal tests
interface LogFirebaseEvent {

    void logFirebaseEvent(Context context, String full_text);

    /* example body
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString("full_text", event_text);
        mFirebaseAnalytics.logEvent("app_widget", params);
    */
}
