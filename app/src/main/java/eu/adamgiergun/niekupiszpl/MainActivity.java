package eu.adamgiergun.niekupiszpl;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;

public class MainActivity extends AppCompatActivity implements DayFragment.OnListFragmentInteractionListener {

    private boolean isCalendarPermissionGranted;

    boolean isCalendarPermissionGranted() {
        return isCalendarPermissionGranted;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkCalendarPermission();
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onListFragmentInteraction(CommerceFreeDay day) {
        if (day.addToGoogleCalendar())
            showInfo(R.string.info_added_to_calendar, false);
        else
            showInfo(R.string.error_adding_to_calendar_failed, false);
    }

    private void showInfo(int info, boolean length_long) {
        int length;
        if (length_long) {
            length = Toast.LENGTH_LONG;
        } else {
            length = Toast.LENGTH_SHORT;
        }

        Toast.makeText(
                getBaseContext(),
                info,
                length)
                .show();
    }

    private void checkCalendarPermission() {
        isCalendarPermissionGranted = false;
        if (isSystemAtLeastMarshmallow()) {
            if (isWriteCalendarGranted()) {
                setCalendarButton();
            } else {
                requestWriteCalendarPermission();
            }
        } else {
            setCalendarButton();
        }
    }

    private boolean isSystemAtLeastMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private boolean isWriteCalendarGranted() {
        int permissionCheck = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_CALENDAR);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestWriteCalendarPermission() {
        String[] listOfRequestedPermissions = new String[]{Manifest.permission.WRITE_CALENDAR};
        int permissionTypeCode = PermissionType.CALENDAR.getCode();
        requestPermissions(listOfRequestedPermissions, permissionTypeCode);
    }

    private void setCalendarButton() {
        isCalendarPermissionGranted = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionType.CALENDAR.getCode()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setCalendarButton();
            } else {
                showInfo(eu.adamgiergun.mylibrary.R.string.info_explain_calendar_permission, true);
            }
        }
    }
}
