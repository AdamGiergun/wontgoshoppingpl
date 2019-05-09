package eu.adamgiergun.wontgoshoppingpl.common;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class AppViewModel extends AndroidViewModel {

    public AppViewModel(@NonNull Application application) {
        super(application);
    }

    public boolean isLookRich() {
        return AppPreferences.isLookRich(getAppContext());
    }

    protected void setLookRich(boolean isLookRich) {
        AppPreferences.setLookRich(getAppContext(), isLookRich);
    }

    protected Context getAppContext() {
        return getApplication().getApplicationContext();
    }
}
