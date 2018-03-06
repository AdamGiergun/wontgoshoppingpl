package eu.adamgiergun.niekupiszpl;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;

public class MainActivity extends AppCompatActivity implements DayFragment.OnListFragmentInteractionListener {

    private boolean isCalendarPermissionRequestedBySettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkCalendarPermission())
            setCalendarButton();
        else if (isSystemAtLeastMarshmallow()) {
            requestWriteCalendarPermission();
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(
                R.menu.main_activity_actions,
                menu
        );
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                settings(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void settings(MenuItem menuitem) {
        if (checkCalendarPermission()) {
            startSettingsActivity();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isCalendarPermissionRequestedBySettings = true;
                requestWriteCalendarPermission();
            }
        }
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(
                getBaseContext(),
                SettingsActivity.class);
        startActivity(intent);
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

    private boolean checkCalendarPermission() {
        isCalendarPermissionRequestedBySettings = false;
        return !isSystemAtLeastMarshmallow() || isWriteCalendarGranted();
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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionType.CALENDAR.getCode()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isCalendarPermissionRequestedBySettings) {
                    startSettingsActivity();
                } else {
                    setCalendarButton();
                }
            } else {
                showInfo(eu.adamgiergun.mylibrary.R.string.info_explain_calendar_permission, true);
            }
        }
    }
}
