package eu.adamgiergun.wontgoshoppingpl.common;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;

import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;

public class AppViewModel extends AndroidViewModel {

    public static final String START_FRAGMENT_ID_TAG = "start_fragment_id";

    public AppViewModel(@NonNull Application application) {
        super(application);
    }

    static boolean areAllPermissionsGranted(int[] grantResults) {
        boolean result = true;
        for (int grantResult : grantResults) {
            result = result && (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        return result;
    }

    public boolean areCalendarPermissionsGranted() {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) ||
                (isPermissionGranted(Manifest.permission.READ_CALENDAR) && isPermissionGranted(Manifest.permission.WRITE_CALENDAR));
    }

    private boolean isPermissionGranted(String permission) {
        int permissionState = ContextCompat.checkSelfPermission(getAppContext(), permission);
        return permissionState == PackageManager.PERMISSION_GRANTED;
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

    static String[] getCalendarPermissions() {
        return new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
    }

    static int getCalendarPermissionsCode() {
        return PermissionType.CALENDAR.getCode();
    }
}
