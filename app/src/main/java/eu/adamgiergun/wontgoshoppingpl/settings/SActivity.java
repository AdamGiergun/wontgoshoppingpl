package eu.adamgiergun.wontgoshoppingpl.settings;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.*;
import androidx.lifecycle.ViewModelProviders;
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

    private ViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        setContentView(R.layout.settings_activity);
        setupHomeInBar();

        mViewModel = ViewModelProviders.of(this).get(ViewModel.class);
        if (savedInstanceState == null) {
            String startFragmentId = getIntent().getStringExtra(ViewModel.getStartFragmentIdTag());
            addFragments(mViewModel.getInitialFragments(startFragmentId));
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                onScreenWideEnoughChanged();
            }
        }
        setTitle(mViewModel.geTitle());
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
        if (mViewModel.hasScreenWideEnoughChanged()) {
            popBackStackCompletely();
            setContentView(R.layout.settings_activity);
            addFragments(mViewModel.getRotatedFragments());
            setTitle(mViewModel.geTitle());
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
        mViewModel.setStartedPreference(StartedPreference.newInstance(caller, pref));
        if (mViewModel.isStartedPreferenceNotNotifications() || checkCalendarPermissions()) {
            addStartedFragment();
            return true;
        } else {
            return false;
        }
    }

    private void addStartedFragment() {
        StartedPreference startedPreference = mViewModel.getStartedPreference();
        mViewModel.setLastFragmentId(startedPreference.pref.getFragment());
        // Instantiate the new Fragment
        final Bundle args = startedPreference.pref.getExtras();
        FragmentManager fragmentManager = getSupportFragmentManager();
        final Fragment fragment = fragmentManager.getFragmentFactory().instantiate(
                getClassLoader(),
                startedPreference.pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(startedPreference.caller, 0);
        // Replace the existing Fragment with the new Fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(R.id.settings, fragment);
        if (fragment instanceof PreferenceHeadersFragment) {
            fragmentManager.popBackStack();
        } else {
            if (!mViewModel.isScreenWideEnough())
                fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
        setTitle(startedPreference.pref.getTitle());
        mViewModel.setStartedPreference(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkCalendarPermissions() {
        if (mViewModel.areCalendarPermissionsGranted()) {
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
        mViewModel.setStartedPreference(null);
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

}
