package eu.adamgiergun.niekupiszpl;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;

public class MainActivity extends AppCompatActivity implements DayFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkCalendarPermission();
    }


    @Override
    public void onListFragmentInteraction(CommerceFreeDay day) {
        if (day.addToGoogleCalendar())
            showInfo(R.string.info_added_to_calendar);
        else
            showInfo(R.string.error_adding_to_calendar_failed);
    }

    private void showInfo(int info) {
        Toast.makeText(
                getBaseContext(),
                info,
                Toast.LENGTH_LONG)
                .show();
    }

    private void checkCalendarPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(
                    getBaseContext(),
                    Manifest.permission.WRITE_CALENDAR);

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                setCalendarButton();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.WRITE_CALENDAR},
                        PermissionType.CALENDAR.getCode());
            }
        } else {
            setCalendarButton();
        }
    }

    private void setCalendarButton() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionType.CALENDAR.getCode()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setCalendarButton();
            } else {
                showInfo(eu.adamgiergun.mylibrary.R.string.info_explain_calendar_permission);
            }
        }
    }
}
