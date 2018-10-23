package eu.adamgiergun.wontgoshoppingpl;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
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
    private static final String HEADERS_WIDE = "headersWide";
    private static final String HEADERS_LOOK = "headersLook";
    private static final String HEADERS_NOTIFICATIONS = "headersNotifications";
    private static final String LOOK = "look";
    private static final String NOTIFICATIONS = "notifications";
    private static final String LOOK_WIDE = "lookWide";
    private static final String NOTIFICATIONS_WIDE = "notificationsWide";
    private boolean startWithNotificationPreferenceFragment;
    private int settingsFragmentId;
    private static String lastFragment = HEADERS;

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
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.settings_activity);
        if (isScreenNotWideEnough()) {
            switch (lastFragment) {
                case HEADERS_WIDE:
                    lastFragment = HEADERS;
                    break;
                case LOOK_WIDE:
                    lastFragment = LOOK;
                    break;
                case NOTIFICATIONS_WIDE:
                    lastFragment = NOTIFICATIONS;
                    break;
                default:
                    break;
            }
        } else {
            switch (lastFragment) {
                case HEADERS:
                    lastFragment = HEADERS_WIDE;
                    break;
                case LOOK:
                    lastFragment = LOOK_WIDE;
                    removeFragmentFromBackStack(LOOK);
                    break;
                case NOTIFICATIONS:
                    lastFragment = NOTIFICATIONS_WIDE;
                    removeFragmentFromBackStack(NOTIFICATIONS);
                    break;
                default:
                    break;
            }
        }

        Intent intent = getIntent();
        startWithNotificationPreferenceFragment = intent.getBooleanExtra("startWithNotificationPreferenceFragment", false);

        ArrayList<FragmentWithTag> settingsFragments = new ArrayList<>();
        if (isScreenNotWideEnough()) {
            settingsFragmentId = R.id.settingsFragment;
            settingsFragments.add(new FragmentWithTag(new SettingsHeadersFragment(), HEADERS, shouldBeDetached(HEADERS)));
            settingsFragments.add(new FragmentWithTag(new LookPreferenceFragment(), LOOK, shouldBeDetached(LOOK)));
            if (areCalendarPermissionsGranted()) {
                settingsFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), NOTIFICATIONS, shouldBeDetached(NOTIFICATIONS)));
            }
        } else {
            settingsFragmentId = R.id.settingsWideFragment;
            ArrayList<FragmentWithTag> headersFragments = new ArrayList<>();
            headersFragments.add(new FragmentWithTag(new SettingsHeadersFragment(), HEADERS_WIDE, shouldBeDetached(HEADERS_WIDE)));
            addFragments(R.id.headersFragment, headersFragments);
            settingsFragments.add(new FragmentWithTag(new LookPreferenceFragment(), LOOK_WIDE, shouldBeDetached(LOOK_WIDE)));
            if (areCalendarPermissionsGranted()) {
                settingsFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), NOTIFICATIONS_WIDE, shouldBeDetached(NOTIFICATIONS_WIDE)));
            }
        }
        addFragments(settingsFragmentId, settingsFragments);
    }

    private boolean shouldBeDetached(String fragmentTag) {
        if (isScreenNotWideEnough()) {
            return !lastFragment.equals(fragmentTag);
        } else {
            if (fragmentTag.equals(HEADERS_WIDE)) {
                return false;
            } else {
                if (lastFragment.equals(HEADERS_WIDE) && fragmentTag.equals(LOOK_WIDE)) {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
/*        if (startWithNotificationPreferenceFragment) {
            startWithNotificationPreferenceFragment = false;
            if (isScreenNotWideEnough()) {
                switchFragment(HEADERS, NOTIFICATIONS);
            } else {
                switchFragment(LOOK_WIDE, NOTIFICATIONS_WIDE);
            }
        }*/
        if (startWithNotificationPreferenceFragment) {
            startWithNotificationPreferenceFragment = false;
            if (isScreenNotWideEnough()) {
                lastFragment = NOTIFICATIONS;
            } else {
                switchFragment(LOOK_WIDE, NOTIFICATIONS_WIDE);
            }
        }
        if (isScreenNotWideEnough()) {
            switchFragment(HEADERS, lastFragment);
        }
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (isScreenNotWideEnough() && !lastFragment.equals(HEADERS)) {
            removeFragmentFromBackStack(lastFragment);
            switchFragment(lastFragment, HEADERS);
/*            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            Fragment lastFrag = manager.findFragmentByTag(lastFragment);
            trans.detach(lastFrag);
            trans.commit();
            manager.popBackStack();
            manager.executePendingTransactions();
            lastFragment = HEADERS;
*/
        } else {
            lastFragment = HEADERS;
            super.onBackPressed();
        }
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
        String headerTitle = pref.getTitle().toString();
        if (headerTitle.equals(getString(R.string.pref_header_look))) {
            if (isScreenNotWideEnough()) {
                return switchFragment(HEADERS, LOOK);
            } else {
                return switchFragment(NOTIFICATIONS_WIDE, LOOK_WIDE);
            }
        } else if (headerTitle.equals(getString(R.string.pref_header_notifications))) {
            if (checkCalendarPermissions()) {
                if (isScreenNotWideEnough()) {
                    return switchFragment(HEADERS, NOTIFICATIONS);
                } else {
                    return switchFragment(LOOK_WIDE, NOTIFICATIONS_WIDE);
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isScreenNotWideEnough() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        } else {
            int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
            return (screenSize < Configuration.SCREENLAYOUT_SIZE_LARGE);
        }
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
        if (requestCode == PermissionType.CALENDAR.getCode()) {
            if (areAllPermissionsGranted(grantResults)) {
                ArrayList<FragmentWithTag> newFragments = new ArrayList<>();
                if (isScreenNotWideEnough()) {
                    newFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), NOTIFICATIONS, true));
                    addFragments(settingsFragmentId, newFragments);
                    switchFragment(HEADERS, NOTIFICATIONS);
                } else {
                    newFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), NOTIFICATIONS_WIDE, true));
                    addFragments(settingsFragmentId, newFragments);
                    switchFragment(LOOK_WIDE, NOTIFICATIONS_WIDE);
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
