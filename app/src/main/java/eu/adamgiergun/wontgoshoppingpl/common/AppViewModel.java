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
        return getAppPreferences().isLookRich(getContext());
    }

    protected void setLookRich(boolean isLookRich) {
        getAppPreferences().setLookRich(getContext(), isLookRich);
    }

    protected AppPreferences getAppPreferences() {
        return AppPreferences.getInstance(getContext());
    }

    private Context getContext() {
        return getApplication().getApplicationContext();
    }
}
