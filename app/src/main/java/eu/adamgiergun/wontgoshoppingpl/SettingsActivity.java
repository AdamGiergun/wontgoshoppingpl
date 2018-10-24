package eu.adamgiergun.wontgoshoppingpl;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.*;

import eu.adamgiergun.mylibrary.*;
import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;

public class SettingsActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String HEADERS = "headers";
    private static final String HEADERS_LAND = "headersWide";
    private static final String HEADERS_LOOK = "headersLook";
    private static final String HEADERS_NOTIFICATIONS = "headersNotifications";
    private static final String LOOK = "look";
    private static final String NOTIFICATIONS = "notifications";
    private static final String LOOK_LAND = "lookWide";
    private static final String NOTIFICATIONS_LAND = "notificationsWide";
    private boolean startWithNotificationPreferenceFragment;
    private int settingsFragmentId;
    private static String lastFragment = HEADERS;
    private String headers;
    private String look;
    private String notifications;

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        AppPreferences appPreferences = new AppPreferences(this);
        if (appPreferences.isLookRich(this)) {
            theme.applyStyle(R.style.AppThemeCustomizationsRich, true);
        } else {
            theme.applyStyle(R.style.AppThemeCustomizationsMini, true);
        }
        return theme;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isScreenSizeLesserThanLarge())
            popBackStackCompletely();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.settings_activity);

        if (isOrientationLandscape()) {
            headers = HEADERS_LAND;
            look = LOOK_LAND;
            notifications = NOTIFICATIONS_LAND;
            switch (lastFragment) {
                case HEADERS:
                    lastFragment = HEADERS_LAND;
                    break;
                case LOOK:
                    lastFragment = LOOK_LAND;
                    break;
                case NOTIFICATIONS:
                    lastFragment = NOTIFICATIONS_LAND;
                    break;
                default:
                    break;
            }
        } else {
            headers = HEADERS;
            look = LOOK;
            notifications = NOTIFICATIONS;
            switch (lastFragment) {
                case HEADERS_LAND:
                    lastFragment = HEADERS;
                    break;
                case LOOK_LAND:
                    lastFragment = LOOK;
                    break;
                case NOTIFICATIONS_LAND:
                    lastFragment = NOTIFICATIONS;
                    break;
                default:
                    break;
            }
        }
        Intent intent = getIntent();
        startWithNotificationPreferenceFragment = intent.getBooleanExtra("startWithNotificationPreferenceFragment", false);

        ArrayList<FragmentWithTag> settingsFragments = new ArrayList<>();
        if (isScreenNotWideEnough()) {
            settingsFragments.add(new FragmentWithTag(new SettingsHeadersFragment(), headers, shouldBeDetached(headers)));
            settingsFragments.add(new FragmentWithTag(new LookPreferenceFragment(), look, shouldBeDetached(look)));
            if (areCalendarPermissionsGranted()) {
                settingsFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), notifications, shouldBeDetached(notifications)));
            }
        } else {
            ArrayList<FragmentWithTag> headersFragments = new ArrayList<>();
            headersFragments.add(new FragmentWithTag(new SettingsHeadersFragment(), headers, shouldBeDetached(headers)));
            addFragments(R.id.headersFragment, headersFragments);
            settingsFragments.add(new FragmentWithTag(new LookPreferenceFragment(), look, shouldBeDetached(look)));
            if (areCalendarPermissionsGranted()) {
                settingsFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), notifications, shouldBeDetached(notifications)));
            }
        }
        settingsFragmentId = getSettingsFragmentId();
        addFragments(settingsFragmentId, settingsFragments);
        testBS("endOnCreate");
    }

    private void popBackStackCompletely() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

    private boolean shouldBeDetached(String fragmentTag) {
        if (isScreenNotWideEnough()) {
            return !lastFragment.equals(fragmentTag);
        } else {
            if (fragmentTag.equals(headers)) {
                return false;
            } else {
                if (lastFragment.equals(headers) && fragmentTag.equals(look)) {
                    return false;
                } else {
                    return !lastFragment.equals(fragmentTag);
                }
            }
        }
    }

    private void addFragments(int container, ArrayList<FragmentWithTag> list) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        for (FragmentWithTag fragmentWithTag : list) {
            ft.add(container, fragmentWithTag.fragment, fragmentWithTag.tag);
            if (fragmentWithTag.detach)
                ft.detach(fragmentWithTag.fragment);
        }
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
        testBS("endAddFragments");
    }

    private int getSettingsFragmentId() {
        if (isScreenSizeLesserThanLarge()) {
            if (isOrientationLandscape()) {
                return R.id.settingsFragmentLand;
            } else {
                return R.id.settingsFragment;
            }
        } else {
            if (isOrientationLandscape()) {
                return R.id.settingsFragmentLargeLand;
            } else {
                return R.id.settingsFragmentLarge;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (startWithNotificationPreferenceFragment) {
            startWithNotificationPreferenceFragment = false;
            if (isScreenNotWideEnough()) {
                lastFragment = notifications;
            } else {
                switchFragment(look, notifications);
            }
        }
        if (isScreenNotWideEnough()) {
            switchFragment(headers, lastFragment);
        }
        testBS("endOnResume");
    }

    private boolean switchFragment(String fromTag, String toTag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fromTag != null) {
            Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(fromTag);
            fragmentTransaction.detach(oldFragment);
        }
        Fragment newFragment = getSupportFragmentManager().findFragmentByTag(toTag);
        fragmentTransaction.attach(newFragment);
        lastFragment = toTag;
        if (isScreenNotWideEnough() && fromTag!=null && !toTag.equals(HEADERS))
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
        testBS("endSwitchFragment");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                 onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        testBS("startOnBackPressed");
        if (isScreenNotWideEnough() && !lastFragment.equals(HEADERS)) {
            super.onBackPressed();
            switchFragment(lastFragment, HEADERS);
        } else {
            lastFragment = HEADERS;
            super.onBackPressed();
            //super.onBackPressed();
        }
    }


    private void testBS(String place) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.d("BACKSTACK", String.valueOf(fragmentManager.getBackStackEntryCount()) + "_" + place);
    }

    private void removeFragmentFromBackStack(String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag(tag);
        trans.detach(fragment);
        trans.commit();
        manager.popBackStack();
        manager.executePendingTransactions();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        testBS("startOnPreferenceStartFragment");
        String headerTitle = pref.getTitle().toString();
        if (headerTitle.equals(getString(R.string.pref_header_look))) {
            if (isScreenNotWideEnough()) {
                return switchFragment(headers, look);
            } else {
                return switchFragment(notifications, look);
            }
        } else if (headerTitle.equals(getString(R.string.pref_header_notifications))) {
            if (checkCalendarPermissions()) {
                if (isScreenNotWideEnough()) {
                    return switchFragment(headers, notifications);
                } else {
                    return switchFragment(look, notifications);
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isScreenNotWideEnough() {
        if (isOrientationLandscape()) {
            return false;
        } else {
            return (isScreenSizeLesserThanLarge());
        }
    }

    private boolean isOrientationLandscape() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private boolean isScreenSizeLesserThanLarge() {
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return (screenSize < Configuration.SCREENLAYOUT_SIZE_LARGE);
    }

    private boolean checkCalendarPermissions() {
        if (!isSystemAtLeastMarshmallow() || areCalendarPermissionsGranted()) {
            return true;
        } else {
            requestCalendarPermissions();
            return false;
        }
    }

    private boolean isSystemAtLeastMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private boolean areCalendarPermissionsGranted() {
        return isPermissionGranted(Manifest.permission.READ_CALENDAR) && isPermissionGranted(Manifest.permission.WRITE_CALENDAR) ;
    }

    private boolean isPermissionGranted(String permission) {
        int permissionState = ContextCompat.checkSelfPermission(this, permission);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCalendarPermissions() {
        String[] listOfRequestedPermissions = new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
        int permissionTypeCode = PermissionType.CALENDAR.getCode();
        requestPermissions(listOfRequestedPermissions, permissionTypeCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        testBS("startOnRequestPermissionsResult");
        if (requestCode == PermissionType.CALENDAR.getCode()) {
            if (areAllPermissionsGranted(grantResults)) {
                ArrayList<FragmentWithTag> newFragments = new ArrayList<>();
                if (isScreenNotWideEnough()) {
                    newFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), notifications, true));
                    addFragments(settingsFragmentId, newFragments);
                    switchFragment(headers, notifications);
                } else {
                    newFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), notifications, true));
                    addFragments(settingsFragmentId, newFragments);
                    switchFragment(look, notifications);
                }
            } else {
                Toast.makeText(this, eu.adamgiergun.mylibrary.R.string.info_explain_calendar_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean areAllPermissionsGranted(int[] grantResults) {
        boolean result = true;
        boolean permissionGranted;
        for (int grantResult : grantResults) {
            permissionGranted = (grantResult == PackageManager.PERMISSION_GRANTED);
            result = result && permissionGranted;
        }
        return result;
    }

    public static class SettingsHeadersFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_headers);
        }

    }

    public static class LookPreferenceFragment extends BasePreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_look, rootKey);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.app_look_preference_key))) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    public static class NotificationPreferenceFragment extends BasePreferenceFragmentCompat {

        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_notification, rootKey);
            setSummaries();
        }

        private void setSummaries() {
            IntegerDialogPreferenceNew daysBefore = (IntegerDialogPreferenceNew) findPreference(getString(R.string.default_reminder_days_before_preference_key));
            daysBefore.setSummary(daysBefore.toString());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.default_calendar_preference_key))) {
                Preference connectionPref = findPreference(key);
                if (connectionPref instanceof CalendarsListPreferenceNew) {
                    CharSequence calendarId = sharedPreferences.getString(key, "0");
                    CharSequence summary = ((CalendarsListPreferenceNew) connectionPref).getCalendarName(calendarId);
                    connectionPref.setSummary(summary);
                }
            }
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            DialogFragment dialogFragment = null;
            if (preference instanceof IntegerDialogPreferenceNew) {
                dialogFragment = new IntegerPreferenceDialogFragmentCompat();
            } else if (preference instanceof TimePreferenceNew) {
                dialogFragment = new TimePreferenceDialogFragmentCompat();
            }

            if (dialogFragment != null) {
                setDialogFragmentArguments(dialogFragment, preference.getKey());
                dialogFragment.setTargetFragment(this, 0);
                if (this.getFragmentManager() != null) {
                    dialogFragment.show(this.getFragmentManager(), "androidx.preference.PreferenceFragmentCompat.DIALOG");
                }
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        private void setDialogFragmentArguments(DialogFragment dialogFragment, String key) {
            Bundle bundle = new Bundle(1);
            bundle.putString("key", key);
            dialogFragment.setArguments(bundle);
        }
    }

    private class FragmentWithTag {
        PreferenceFragmentCompat fragment;
        String tag;
        boolean detach;

        FragmentWithTag(PreferenceFragmentCompat fragment, String tag, boolean detach) {
            this.fragment = fragment;
            this.tag = tag;
            this.detach = detach;
        }
    }
}
