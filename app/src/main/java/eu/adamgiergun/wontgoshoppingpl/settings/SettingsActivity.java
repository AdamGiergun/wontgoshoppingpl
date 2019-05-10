package eu.adamgiergun.wontgoshoppingpl.settings;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.*;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.*;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.*;

import java.util.List;

import eu.adamgiergun.mylibrary.*;
import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;
import eu.adamgiergun.wontgoshoppingpl.common.*;
import eu.adamgiergun.wontgoshoppingpl.main.MainActivity;
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
        clearActivity();
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        setContentView(R.layout.settings_activity);
        initializeViewModel();
        addFragments(mViewModel.getSettingsFragmentId(), getSettingsFragments());
    }

    private void clearActivity() {
        removeAllFragments();
        if (isScreenSizeLesserThanLarge())
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

    private void initializeViewModel() {
        mViewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        mViewModel.setTagsAndFragments(
                isOrientationLandscape());
        mViewModel.setStartFragmentNotification(
                getIntent().getBooleanExtra("startFragmentNotification", false));
        mViewModel.setSettingsFragmentId(getSettingsFragmentId());
    }

    private FragmentWithTag.List getSettingsFragments() {
        FragmentWithTag.List settingsFragments = new FragmentWithTag.List();
        if (isScreenNotWideEnough()) {
            settingsFragments.add(
                    FragmentWithTag.newInstance(
                            new SettingsHeadersFragment(),
                            mViewModel.getHeadersTag(),
                            mViewModel.shouldBeDetached(isScreenNotWideEnough(), mViewModel.getHeadersTag())));
        } else {
            FragmentWithTag.List headersFragments = new FragmentWithTag.List();
            headersFragments.add(
                    FragmentWithTag.newInstance(
                            new SettingsHeadersFragment(),
                            mViewModel.getHeadersTag(),
                            mViewModel.shouldBeDetached(isScreenNotWideEnough(), mViewModel.getHeadersTag())));
            addFragments(R.id.headersFragment, headersFragments);
        }
        settingsFragments.add(
                FragmentWithTag.newInstance(
                        new LookPreferenceFragment(),
                        mViewModel.getLookTag(),
                        mViewModel.shouldBeDetached(isScreenNotWideEnough(), mViewModel.getLookTag())));
        if (areCalendarPermissionsGranted()) {
            settingsFragments.add(
                    FragmentWithTag.newInstance(
                            new NotificationPreferenceFragment(),
                            mViewModel.getNotificationsTag(),
                            mViewModel.shouldBeDetached(isScreenNotWideEnough(), mViewModel.getNotificationsTag())));
        }
        return settingsFragments;
    }

    private void addFragments(int container, FragmentWithTag.List list) {
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
            return (isOrientationLandscape())
                    ? R.id.settingsFragmentLand
                    : R.id.settingsFragment;
        } else {
            return (isOrientationLandscape())
                    ? R.id.settingsFragmentLargeLand
                    : R.id.settingsFragmentLarge;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mViewModel.isStartFragmentNotification()) {
            mViewModel.setStartFragmentNotification(false);
            if (isScreenNotWideEnough()) {
                mViewModel.setLastFragment(mViewModel.getNotificationsTag());
            } else {
                switchFragment(mViewModel.getLookTag(), mViewModel.getNotificationsTag());
            }
        }
        if (isScreenNotWideEnough()) {
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

        if (isScreenNotWideEnough() && fromTag!=null && !toTag.equals(HEADERS))
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
        if (isScreenNotWideEnough() && !mViewModel.getLastFragment().equals(HEADERS)) {
            super.onBackPressed();
            switchFragment(mViewModel.getLastFragment(), HEADERS);
        } else {
            mViewModel.setLastFragment(HEADERS);
            super.onBackPressed();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        String headerTitle = pref.getTitle().toString();
        if (headerTitle.equals(getString(R.string.pref_header_look))) {
            if (isScreenNotWideEnough()) {
                switchFragment(mViewModel.getHeadersTag(), mViewModel.getLookTag());
            } else {
                switchFragment(mViewModel.getNotificationsTag(), mViewModel.getLookTag());
            }
            return true;
        } else if (headerTitle.equals(getString(R.string.pref_header_notifications))) {
            if (checkCalendarPermissions()) {
                if (isScreenNotWideEnough()) {
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

    private boolean isScreenNotWideEnough() {
        return (!isOrientationLandscape()) && isScreenSizeLesserThanLarge();
    }

    private boolean isOrientationLandscape() {
        int orientation = getResources().getConfiguration().orientation;
        return (orientation == Configuration.ORIENTATION_LANDSCAPE);
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
        int permissionState = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
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
                FragmentWithTag.List newFragments = new FragmentWithTag.List();
                newFragments.add(
                        FragmentWithTag.newInstance(new NotificationPreferenceFragment(), mViewModel.getNotificationsTag(), true));
                addFragments(mViewModel.getSettingsFragmentId(), newFragments);
                String switchedFragmentTag = isScreenNotWideEnough() ? mViewModel.getHeadersTag() : mViewModel.getLookTag();
                switchFragment(switchedFragmentTag, mViewModel.getNotificationsTag());
            } else {
                Toast.makeText(getApplicationContext(), eu.adamgiergun.mylibrary.R.string.info_explain_calendar_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean areAllPermissionsGranted(int[] grantResults) {
        boolean result = true;
        for (int grantResult : grantResults) {
            result = result && (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        return result;
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
                viewModel.setLastFragment(HEADERS);
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
            IntegerDialogPreference daysBefore = (IntegerDialogPreference) findPreference(getString(R.string.default_reminder_days_before_preference_key));
            daysBefore.setSummary(daysBefore.toString());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.default_calendar_preference_key))) {
                Preference preference = findPreference(key);
                if (preference instanceof CalendarsListPreferenceNew) {
                    CharSequence calendarId = sharedPreferences.getString(key, "0");
                    CharSequence summary = ((CalendarsListPreferenceNew) preference).getCalendarName(calendarId);
                    preference.setSummary(summary);
                }
            }
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            DialogFragment dialogFragment = null;
            if (preference instanceof IntegerDialogPreference) {
                dialogFragment = new IntegerPreferenceDialogFragmentCompat();
            } else if (preference instanceof TimePreference) {
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

        // TODO ChooseCalendarsForCleaning
//        @Override
//        public boolean onPreferenceTreeClick(Preference preference) {
//            if (preference.getKey().equals(getString(R.string.key_delete_events))) {
//                DialogFragment dialogFragment = new ChooseCalendarsForCleaning();
//                if (this.getFragmentManager() != null) {
//                    dialogFragment.show(this.getFragmentManager(), "test");
//                }
//                return true;
//            } else {
//                return super.onPreferenceTreeClick(preference);
//            }
//        }
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
