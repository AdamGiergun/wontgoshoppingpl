package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;

import androidx.lifecycle.ViewModel;

public class AppViewModel extends ViewModel {

    protected AppPreferences appPreferences;

    public void initialize(Context context) {
        appPreferences = AppPreferences.getInstance(context);
    }

    public boolean isLookRich(Context context) {
        return appPreferences.isLookRich(context);
    }

    protected void setLookRich(Context context, boolean isLookRich) {
        appPreferences.setLookRich(context, isLookRich);
    }
}
