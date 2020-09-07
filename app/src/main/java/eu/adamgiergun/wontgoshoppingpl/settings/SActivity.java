package eu.adamgiergun.wontgoshoppingpl.settings;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.*;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.*;

import eu.adamgiergun.mylibrary.*;
import eu.adamgiergun.wontgoshoppingpl.common.ActivityWithTheme;
import eu.adamgiergun.wontgoshoppingpl.R;

public class SActivity extends ActivityWithTheme
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
        NoticeDialogFragment.NoticeDialogListener,
        NoticeDialogFragment.NoticeDialogViewListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private SettingsViewModel mSettingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        setContentView(R.layout.settings_activity);
        setupHomeInBar();

        mSettingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        if (savedInstanceState == null) {
            String startFragmentId = getIntent().getStringExtra(SettingsViewModel.START_FRAGMENT_ID_TAG);
            addFragments(mSettingsViewModel.getInitialFragments(startFragmentId));
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M ||
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 ||
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                onScreenWideEnoughChanged();
            }
        }
        setTitle(mSettingsViewModel.geTitle());
    }

    private void addFragments(SettingsFragment.List fragments) {
        for (SettingsFragment fragment : fragments) {
            FragmentTransaction fm = getSupportFragmentManager()
                    .beginTransaction();
            fm.replace(fragment.containerId, fragment.fragment);
            if (fragment.addToBackStack)
                fm.addToBackStack(null);
            fm.commit();
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onScreenWideEnoughChanged();
    }

    private void onScreenWideEnoughChanged() {
        if (mSettingsViewModel.hasScreenWideEnoughChanged()) {
            popBackStackCompletely();
            setContentView(R.layout.settings_activity);
            addFragments(mSettingsViewModel.getRotatedFragments());
            setTitle(mSettingsViewModel.geTitle());
        }
    }

    private void popBackStackCompletely() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        mSettingsViewModel.setStartedPreference(StartedPreference.newInstance(caller, pref));
        if (mSettingsViewModel.isStartedPreferenceNotNotifications() || checkCalendarPermissions()) {
            addStartedFragment();
        }
        return true;
    }

    private void addStartedFragment() {
        StartedPreference startedPreference = mSettingsViewModel.getStartedPreference();
        mSettingsViewModel.setLastFragmentId(startedPreference.getPref().getFragment());
        // Instantiate the new Fragment
        final Bundle args = startedPreference.getPref().getExtras();
        FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment fragment = fragmentManager.getFragmentFactory().instantiate(
                getClassLoader(),
                startedPreference.getPref().getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(startedPreference.getCaller(), 0);
        // Replace the existing Fragment with the new Fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(R.id.settings, fragment);
        if (!mSettingsViewModel.isScreenWideEnough())
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        setTitle(startedPreference.getPref().getTitle());
        mSettingsViewModel.setStartedPreference(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkCalendarPermissions() {
        if (mSettingsViewModel.areCalendarPermissionsGranted()) {
            return true;
        } else {
            requestCalendarPermissions();
            return false;
        }
    }

    @Override
    protected void onCalendarPermissionsGranted() {
        super.onCalendarPermissionsGranted();
        addStartedFragment();
    }

    @Override
    protected void onCalendarPermissionNotGranted() {
        super.onCalendarPermissionNotGranted();
        addEmptyNotificationsFragment();
    }

    private void addEmptyNotificationsFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment fragment = fragmentManager.getFragmentFactory().instantiate(
                getClassLoader(),
                EmptyPreferenceFragment.class.getName());
        fragment.setTargetFragment(mSettingsViewModel.getStartedPreference().getCaller(), 0);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(R.id.settings, fragment);
        if (!mSettingsViewModel.isScreenWideEnough())
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        setTitle(R.string.pref_header_notifications);
        mSettingsViewModel.setStartedPreference(null);
    }

    @Override
    public void onDialogPositiveClick(NoticeDialogFragment noticeDialogFragment) {

    }

    @Override
    public void onDialogNegativeClick(NoticeDialogFragment noticeDialogFragment) {
        Toast.
                makeText(getApplicationContext(), R.string.info_cancelled, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onViewClick(NoticeDialogFragment stub) {
        //stub
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            mSettingsViewModel.setLastFragmentIdHeaders();
            setTitle(mSettingsViewModel.geTitle());
            return true;
        }
        return super.onSupportNavigateUp();
    }
}
