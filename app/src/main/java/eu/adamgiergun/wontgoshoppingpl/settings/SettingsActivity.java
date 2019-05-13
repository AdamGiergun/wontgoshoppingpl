package eu.adamgiergun.wontgoshoppingpl.settings;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.*;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.*;

import java.util.List;

import eu.adamgiergun.mylibrary.*;
import eu.adamgiergun.wontgoshoppingpl.common.AppCompatActivityWithTheme;
import eu.adamgiergun.wontgoshoppingpl.R;

import static eu.adamgiergun.wontgoshoppingpl.settings.SettingsViewModel.HEADERS;

public class SettingsActivity extends AppCompatActivityWithTheme
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
        NoticeDialogFragment.NoticeDialogListener,
        NoticeDialogFragment.NoticeDialogViewListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private SettingsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupHomeInBar();
        initializeViewModel();
        clearActivity();
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        setContentView(R.layout.settings_activity);
        addFragments(mViewModel.newFragmentsWithTag());
    }

    private void initializeViewModel() {
        if (mViewModel==null)
            mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        mViewModel.initialize(getIntent().getBooleanExtra(SettingsViewModel.START_FROM_NOTIFICATIONS_IN_SETTINGS, false));
    }

    private void clearActivity() {
        removeAllFragments();
        if (mViewModel.isScreenSizeLesserThanLarge())
            popBackStackCompletely();
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

    private void addFragments(FragmentWithTag.Set set) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        for (FragmentWithTag fragmentWithTag : set) {
            ft.add(fragmentWithTag.containerId, fragmentWithTag.fragment, fragmentWithTag.tag);
            if (fragmentWithTag.detach)
                ft.detach(fragmentWithTag.fragment);
        }
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mViewModel.isStartFragmentNotification()) {
            if (mViewModel.isScreenNotWideEnough()) {
                mViewModel.setLastFragment(mViewModel.getNotificationsTag());
            } else {
                switchFragment(mViewModel.getLookTag(), mViewModel.getNotificationsTag());
            }
        }
        if (mViewModel.isScreenNotWideEnough()) {
            switchFragment(mViewModel.getHeadersTag(), mViewModel.getLastFragment());
        }
    }

    private void switchFragment(String fromTag, String toTag) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        Fragment oldFragment = (fromTag == null) ? null : getSupportFragmentManager().findFragmentByTag(fromTag);
        if (oldFragment != null)
            fragmentTransaction.detach(oldFragment);

        Fragment newFragment = getSupportFragmentManager().findFragmentByTag(toTag);
        if (newFragment != null)
            fragmentTransaction.attach(newFragment);
        mViewModel.setLastFragment(toTag);

        if (mViewModel.isScreenNotWideEnough() && fromTag!=null && !toTag.equals(HEADERS))
            fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
        getSupportFragmentManager().executePendingTransactions();
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
        if (mViewModel.isScreenNotWideEnough() && !mViewModel.getLastFragment().equals(HEADERS)) {
            super.onBackPressed();
            switchFragment(mViewModel.getLastFragment(), HEADERS);
        } else {
            mViewModel.setLastFragment(HEADERS);
            super.onBackPressed();
            super.onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        String headerTitle = pref.getTitle().toString();
        if (headerTitle.equals(getString(R.string.pref_header_look))) {
            if (mViewModel.isScreenNotWideEnough()) {
                switchFragment(mViewModel.getHeadersTag(), mViewModel.getLookTag());
            } else {
                switchFragment(mViewModel.getNotificationsTag(), mViewModel.getLookTag());
            }
            return true;
        } else if (headerTitle.equals(getString(R.string.pref_header_notifications))) {
            if (checkCalendarPermissions()) {
                if (mViewModel.isScreenNotWideEnough()) {
                    switchFragment(mViewModel.getHeadersTag(), mViewModel.getNotificationsTag());
                } else {
                    switchFragment(mViewModel.getLookTag(), mViewModel.getNotificationsTag());
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
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
    protected void onAllPermissionsGranted() {
        addFragments(mViewModel.newNotificationPreferenceFragment());
        switchFragment(mViewModel.getSwitchedFragmentTag(), mViewModel.getNotificationsTag());
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

    // TODO ChooseCalendarsForCleaning
//    public static class ChooseCalendarsForCleaning extends AppDialogFragment {
//        @NonNull
//        @Override
//        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog.Builder builder = getStyledBuilder(getContext());
//            builder
//                    .setMultiChoiceItems(null, null, null)//items, checkedItems, listener)
//                    .setPositiveButton(android.R.string.ok, getPositiveButtonListener(ChooseCalendarsForCleaning.this))
//                    .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(ChooseCalendarsForCleaning.this));
//            return builder.create();
//        }
//    }
}
