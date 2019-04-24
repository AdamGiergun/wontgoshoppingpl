package eu.adamgiergun.wontgoshoppingpl.common;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class AppViewModel extends AndroidViewModel {

    public AppViewModel(@NonNull Application application) {
        super(application);
    }

    public boolean isLookRich() {
        return getAppPreferences().isLookRich(getApplication());
    }

    protected void setLookRich(boolean isLookRich) {
        getAppPreferences().setLookRich(getApplication(), isLookRich);
    }

    protected AppPreferences getAppPreferences() {
        return AppPreferences.getInstance(getApplication());
    }
}
