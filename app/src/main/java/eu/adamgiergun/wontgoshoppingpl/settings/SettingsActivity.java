package eu.adamgiergun.wontgoshoppingpl.settings;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.*;

import eu.adamgiergun.mylibrary.*;
import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;
import eu.adamgiergun.wontgoshoppingpl.common.AppCompatActivityWithTheme;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.common.ThisAppDialogFragment;
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity;

public class SettingsActivity extends AppCompatActivityWithTheme
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
        NoticeDialogFragment.NoticeDialogListener,
        NoticeDialogFragment.NoticeDialogViewListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private SettingsViewModel mViewModel;

    private boolean startWithNotificationPreferenceFragment;
    private int settingsFragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        setupHomeInBar();
        removeAllFragments();
        if (isScreenSizeLesserThanLarge())
            popBackStackCompletely();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.settings_activity);

        mViewModel.setOrientationLandscape(isOrientationLandscape());

        Intent intent = getIntent();
        startWithNotificationPreferenceFragment = intent.getBooleanExtra("startWithNotificationPreferenceFragment", false);

        ArrayList<FragmentWithTag> settingsFragments = new ArrayList<>();
        if (isScreenNotWideEnough()) {
            settingsFragments.add(new FragmentWithTag(new SettingsHeadersFragment(), SettingsViewModel.headers, shouldBeDetached(SettingsViewModel.headers)));
            settingsFragments.add(new FragmentWithTag(new LookPreferenceFragment(), SettingsViewModel.look, shouldBeDetached(SettingsViewModel.look)));
            if (areCalendarPermissionsGranted()) {
                settingsFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), SettingsViewModel.notifications, shouldBeDetached(SettingsViewModel.notifications)));
            }
        } else {
            ArrayList<FragmentWithTag> headersFragments = new ArrayList<>();
            headersFragments.add(new FragmentWithTag(new SettingsHeadersFragment(), SettingsViewModel.headers, shouldBeDetached(SettingsViewModel.headers)));
            addFragments(R.id.headersFragment, headersFragments);
            settingsFragments.add(new FragmentWithTag(new LookPreferenceFragment(), SettingsViewModel.look, shouldBeDetached(SettingsViewModel.look)));
            if (areCalendarPermissionsGranted()) {
                settingsFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), SettingsViewModel.notifications, shouldBeDetached(SettingsViewModel.notifications)));
            }
        }
        settingsFragmentId = getSettingsFragmentId();
        addFragments(settingsFragmentId, settingsFragments);
    }

    private void removeAllFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (Fragment fragment : fragments)
            fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void popBackStackCompletely() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

    private boolean shouldBeDetached(String fragmentTag) {
        if (isScreenNotWideEnough()) {
            return !mViewModel.getLastFragment().equals(fragmentTag);
        } else {
            if (fragmentTag.equals(SettingsViewModel.headers)) {
                return false;
            } else {
                if (mViewModel.getLastFragment().equals(SettingsViewModel.headers) && fragmentTag.equals(SettingsViewModel.look)) {
                    return false;
                } else {
                    return !mViewModel.getLastFragment().equals(fragmentTag);
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
                mViewModel.setLastFragment(SettingsViewModel.notifications);
            } else {
                switchFragment(SettingsViewModel.look, SettingsViewModel.notifications);
            }
        }
        if (isScreenNotWideEnough()) {
            switchFragment(SettingsViewModel.headers, mViewModel.getLastFragment());
        }
    }

    private boolean switchFragment(String fromTag, String toTag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fromTag != null) {
            Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(fromTag);
            if (oldFragment != null)
                fragmentTransaction.detach(oldFragment);
        }
        Fragment newFragment = getSupportFragmentManager().findFragmentByTag(toTag);
        if (newFragment != null)
            fragmentTransaction.attach(newFragment);
        mViewModel.setLastFragment(toTag);
        if (isScreenNotWideEnough() && fromTag!=null && !toTag.equals(SettingsViewModel.HEADERS))
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isScreenNotWideEnough() && !mViewModel.getLastFragment().equals(SettingsViewModel.HEADERS)) {
            super.onBackPressed();
            switchFragment(mViewModel.getLastFragment(), SettingsViewModel.HEADERS);
        } else {
            mViewModel.setLastFragment(SettingsViewModel.HEADERS);
            super.onBackPressed();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        String headerTitle = pref.getTitle().toString();
        if (headerTitle.equals(getString(R.string.pref_header_look))) {
            if (isScreenNotWideEnough()) {
                return switchFragment(SettingsViewModel.headers, SettingsViewModel.look);
            } else {
                return switchFragment(SettingsViewModel.notifications, SettingsViewModel.look);
            }
        } else if (headerTitle.equals(getString(R.string.pref_header_notifications))) {
            if (checkCalendarPermissions()) {
                if (isScreenNotWideEnough()) {
                    return switchFragment(SettingsViewModel.headers, SettingsViewModel.notifications);
                } else {
                    return switchFragment(SettingsViewModel.look, SettingsViewModel.notifications);
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
        if (requestCode == PermissionType.CALENDAR.getCode()) {
            if (areAllPermissionsGranted(grantResults)) {
                ArrayList<FragmentWithTag> newFragments = new ArrayList<>();
                if (isScreenNotWideEnough()) {
                    newFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), SettingsViewModel.notifications, true));
                    addFragments(settingsFragmentId, newFragments);
                    switchFragment(SettingsViewModel.headers, SettingsViewModel.notifications);
                } else {
                    newFragments.add(new FragmentWithTag(new NotificationPreferenceFragment(), SettingsViewModel.notifications, true));
                    addFragments(settingsFragmentId, newFragments);
                    switchFragment(SettingsViewModel.look, SettingsViewModel.notifications);
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

    @Override
    public void onDialogPositiveClick(NoticeDialogFragment noticeDialogFragment) {

    }

    @Override
    public void onDialogNegativeClick(NoticeDialogFragment noticeDialogFragment) {
        Toast.
                makeText(this, R.string.info_cancelled, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onViewClick(NoticeDialogFragment stub) {
        //stub
    }

    public static class SettingsHeadersFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.pref_headers);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            String title = preference.getTitle().toString();
            SettingsActivity activity = (SettingsActivity) getActivity();
            if (activity != null && !activity.isScreenNotWideEnough()) {
                if (title.equals(getString(R.string.pref_header_look))) {
                    getListView().getChildAt(0).setBackgroundColor(Color.LTGRAY);
                    getListView().getChildAt(1).setBackgroundColor(Color.TRANSPARENT);
                } else {
                    getListView().getChildAt(1).setBackgroundColor(Color.LTGRAY);
                    getListView().getChildAt(0).setBackgroundColor(Color.TRANSPARENT);
                }
            }
            return super.onPreferenceTreeClick(preference);
        }
    }

    public static class LookPreferenceFragment extends BasePreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_look, rootKey);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.app_look_preference_key))) {
                SettingsViewModel viewModel = ViewModelProviders.of(getActivity()).get(SettingsViewModel.class);
                viewModel.setLastFragment(SettingsViewModel.HEADERS);
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

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals(getString(R.string.key_delete_events))) {
                DialogFragment dialogFragment = new ChooseCalendarsForCleaning();
                if (this.getFragmentManager() != null) {
                    dialogFragment.show(this.getFragmentManager(), "test");
                }
                return true;
            } else {
                return super.onPreferenceTreeClick(preference);
            }
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

    public static class ChooseCalendarsForCleaning extends ThisAppDialogFragment {
        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = getStyledBuilder();
            builder
                    .setMultiChoiceItems(null, null, null)//items, checkedItems, listener)
                    .setPositiveButton(android.R.string.ok, getPositiveButtonListener(ChooseCalendarsForCleaning.this))
                    .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(ChooseCalendarsForCleaning.this));
            return builder.create();
        }
    }
}
