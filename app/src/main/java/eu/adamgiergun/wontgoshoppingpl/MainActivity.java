package eu.adamgiergun.wontgoshoppingpl;

/*
    TODO niedziele handlowe (zielone)
    TODO reklamy po dodaniu do kalendarza
    TODO odświeżanie listy głównej zaraz po zmianie ustawienia o pokazywaniu świąt
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;
import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;

public class MainActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener, DayFragment.OnListFragmentInteractionListener {

    private ActionCallingRequest lastActionCallingRequest;
    private LimitedCommerceDay lastTouchedDayFromList;
    private boolean addLimitedCommerceDays;
    private boolean addHolidays;
    private int reminderDaysBefore;
    private SimpleTime reminderTime;
    private AppPreferences appPreferences;
    private boolean addList;
    private InterstitialAd mInterstitialAd;

    private enum ActionCallingRequest {
        ADD_ALL_EVENTS,
        ADD_ONE_EVENT
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        appPreferences = new AppPreferences(this);
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
        setContentView(R.layout.activity_main);
        setupAds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appPreferences.isFirstRun(this)) {
            View view = findViewById(R.id.ask_for_look);
            view.setVisibility(View.VISIBLE);
            view = findViewById(R.id.recycler_view);
            view.setVisibility(View.GONE);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.ask_for_look, new AskForLook());
            ft.commit();
        } else {
            setLook();
            addFragment(R.id.list_of_days_fragment, new DayFragment());
        }
    }

    private void setLook() {
        if (appPreferences.isLookRich(this)) {
            View recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setBackgroundResource(R.drawable.bg_red);
            View adViewLayout = findViewById(R.id.adViewLayout);
            adViewLayout.setBackgroundResource(R.drawable.bg_red);
        }
    }

    private void addFragment(int container, Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment oldFragment = getSupportFragmentManager().findFragmentByTag("list");
        if (oldFragment != null)
            ft.detach(oldFragment);
        ft.add(container, fragment, "list");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void setupAds() {
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, getString(R.string.sample_admob_app_id));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.sample_interstitial_ad_unit_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
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
                startSettingsActivity(false);
                return true;
            case R.id.calendar:
                addList = true;
                startWizardForAddingEventsToCalendarIfConditionsAreMet();
                return true;
            case R.id.help:
                startHelpActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSettingsActivity(boolean startWithNotificationPreferenceFragment) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("startWithNotificationPreferenceFragment", startWithNotificationPreferenceFragment);
        startActivity(intent);
    }

    private void startHelpActivity() {
        //TODO help
    }

    private void startWizardForAddingEventsToCalendarIfConditionsAreMet() {
        if (checkCalendarPermissions(ActionCallingRequest.ADD_ALL_EVENTS)) {
            boolean useGoogleCalendar = appPreferences.isUseGoogleCalendarSet(this);
            boolean isCalendarSet = appPreferences.isCalendarSet(this);
            if (useGoogleCalendar && isCalendarSet) {
                askForKindsOfEventsAddedToCalendar();
            } else {
                startSettingsActivity(true);
            }
        }
    }

    private void askForKindsOfEventsAddedToCalendar() {
        AppCompatDialogFragment dialogFragment = new AskForKindsOfEventsToAddToCalendar();
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onListFragmentInteraction(LimitedCommerceDay day) {
        if (checkCalendarPermissions(ActionCallingRequest.ADD_ONE_EVENT)) {
            boolean useGoogleCalendar = appPreferences.isUseGoogleCalendarSet(this);
            boolean isCalendarSet = appPreferences.isCalendarSet(this);
            if (useGoogleCalendar && isCalendarSet) {
                lastTouchedDayFromList = day;
                addList = false;
                askForReminderSettings();
            } else {
                startSettingsActivity(true);
            }
        } else {
            lastTouchedDayFromList = day;
        }
    }

    private void addDayToCalendar(LimitedCommerceDay day) {
        if (appPreferences.isUseGoogleCalendarSet(this)) {
            showBigAd();
            setReminderParametersFromPreferences();
            setReminderParametersToDay(day);
            displayInfo(day.addToGoogleCalendar(this), R.string.info_added_to_calendar, R.string.error_adding_to_calendar_failed);
        } else {
            startSettingsActivity(true);
        }
    }

    private void showBigAd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    private void displayInfo(boolean resultIsPositive, int positiveInfo, int negativeInfo) {
        if (resultIsPositive)
            showInfo(positiveInfo, false);
        else
            showInfo(negativeInfo, false);
    }

    private void setReminderParametersFromPreferences() {
        reminderDaysBefore = appPreferences.getReminderDays(this);
        reminderTime = appPreferences.getReminderTime(this);
    }

    private void setReminderParametersToDay(LimitedCommerceDay day) {
        day.setReminderDaysBefore(reminderDaysBefore);
        day.setReminderTime(reminderTime);
    }

    private void showInfo(int info, boolean isLengthLong) {
        int length;
        if (isLengthLong) {
            length = Toast.LENGTH_LONG;
        } else {
            length = Toast.LENGTH_SHORT;
        }
        Toast.makeText(this, info, length)
                .show();
    }

    private boolean checkCalendarPermissions(ActionCallingRequest actionCallingRequest) {
        lastActionCallingRequest = actionCallingRequest;
        if (!isSystemAtLeastMarshmallow() || areCalendarPermissionsGranted()) {
            return true;
        } else {
            requestCalendarPermissions(actionCallingRequest);
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
    private void requestCalendarPermissions(ActionCallingRequest actionCallingRequest) {
        lastActionCallingRequest = actionCallingRequest;
        String[] listOfRequestedPermissions = new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
        int permissionTypeCode = PermissionType.CALENDAR.getCode();
        requestPermissions(listOfRequestedPermissions, permissionTypeCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionType.CALENDAR.getCode()) {
            if (areAllPermissionsGranted(grantResults)) {
                switch (lastActionCallingRequest) {
                    case ADD_ONE_EVENT:
                        addDayToCalendar(lastTouchedDayFromList);
                        break;
                    case ADD_ALL_EVENTS:
                        startWizardForAddingEventsToCalendarIfConditionsAreMet();
                        break;
                    default:
                        break;
                }
            } else {
                showInfo(eu.adamgiergun.mylibrary.R.string.info_explain_calendar_permission, true);
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
    public void onDialogPositiveClick(AppCompatDialogFragment dialog) {
        if (dialog instanceof AskForKindsOfEventsToAddToCalendar) {
            getUserChoicesOfDaysAddedToCalendar(dialog);
            askForReminderSettings();
        }
        if (dialog instanceof AskForReminderSettings) {
            getReminderParameters(dialog);
            if (addList) {
                addListToCalendar();
            } else {
                addDayToCalendar(lastTouchedDayFromList);
            }
        }
    }

    private void addListToCalendar() {
        showBigAd();
        ListOfLimitedCommerceDays listOfLimitedCommerceDaysToAddToCalendar =
                ListOfLimitedCommerceDays.getListOfLimitedCommerceDays(this, addHolidays, addLimitedCommerceDays);
        boolean resultIsPositive = listOfLimitedCommerceDaysToAddToCalendar.addToCalendar(
                reminderDaysBefore,
                reminderTime, this);
        displayInfo(resultIsPositive, R.string.info_added_to_calendar, R.string.error_sth_went_wrong);
    }

    private void getUserChoicesOfDaysAddedToCalendar(AppCompatDialogFragment dialog) {
        AskForKindsOfEventsToAddToCalendar myDialog = (AskForKindsOfEventsToAddToCalendar) dialog;
        addLimitedCommerceDays = myDialog.addLimitedCommerceDays();
        addHolidays = myDialog.addHolidays();
    }

    private void askForReminderSettings() {
        AskForReminderSettings dialogFragment = new AskForReminderSettings();
        dialogFragment.setAddList(addList);
        if (!addList) {
            if (lastTouchedDayFromList.isHoliday()) {
                dialogFragment.setDayDescription(lastTouchedDayFromList.getDescription());
            } else {
                dialogFragment.setDayDescription(lastTouchedDayFromList.getDateString());
            }
        }
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    private void getReminderParameters(AppCompatDialogFragment dialog) {
        AskForReminderSettings myDialog = (AskForReminderSettings) dialog;
        reminderDaysBefore = myDialog.getReminderDaysBefore();
        reminderTime = myDialog.getReminderTime();
    }

    @Override
    public void onDialogNegativeClick(AppCompatDialogFragment dialog) {
        showInfo(R.string.info_cancelled, false);
    }

    public static class AskForKindsOfEventsToAddToCalendar extends NoticeDialogFragment {
        private Button dialogButtonPositive;
        private boolean addLimitedCommerceDays;
        private boolean addHolidays;

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            CharSequence[] items = new CharSequence[]
                    {getString(R.string.days_of_limited_commerce),
                    getString(R.string.holidays)};

            final boolean[] checkedItems = new boolean[2];
            checkedItems[0] = true;
            AppPreferences appPreferences = new AppPreferences(getContext());
            checkedItems[1] = appPreferences.showHolidays(getContext());

            DialogInterface.OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    checkedItems[which] = isChecked;
                    dialogButtonPositive.setEnabled(isAnyValueInBooleanArrayTrue(checkedItems));
                }
            };

            AlertDialog.Builder builder;
            if (appPreferences.isLookRich(getContext())) {
                builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }
            builder.setTitle(R.string.title_choose_events)
                    .setMultiChoiceItems(items, checkedItems, listener)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            addLimitedCommerceDays = checkedItems[0];
                            addHolidays = checkedItems[1];
                            noticeDialogListener.onDialogPositiveClick(AskForKindsOfEventsToAddToCalendar.this);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            noticeDialogListener.onDialogNegativeClick(AskForKindsOfEventsToAddToCalendar.this);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialogButtonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            return dialog;
        }

        private boolean isAnyValueInBooleanArrayTrue(boolean[] array) {
            for (boolean value : array)
                if (value) return true;
            return false;
        }

        boolean addLimitedCommerceDays() {
            return addLimitedCommerceDays;
        }

        boolean addHolidays() {
            return addHolidays;
        }
    }

    public static class AskForReminderSettings extends NoticeDialogFragment {
        private int reminderDaysBefore;
        private SimpleTime reminderTime;
        private boolean addList;
        private String dayDescription;

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder;
            AppPreferences appPreferences = new AppPreferences(getContext());
            if (appPreferences.isLookRich(getContext())) {
                builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }
            String title;
            if (addList) {
                title = getString(R.string.title_reminder);
            } else {
                title = getString(R.string.title_reminder_of) + " " + dayDescription ;
            }
            builder
                    .setTitle(title)
                    .setView(getDialogView())
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            noticeDialogListener.onDialogPositiveClick(AskForReminderSettings.this);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            noticeDialogListener.onDialogNegativeClick(AskForReminderSettings.this);
                        }
                    });
            return builder.create();
        }

        private View getDialogView() {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            @SuppressLint("InflateParams")
            View view = inflater.inflate(R.layout.reminder_settings_dialog_view, null);

            AppCompatTextView daysView = view.findViewById(R.id.numberOfDaysBeforeView);
            AppPreferences appPreferences = new AppPreferences(getContext());
            reminderDaysBefore = appPreferences.getReminderDays(getContext());
            daysView.setText(String.valueOf(reminderDaysBefore));

            AppCompatTextView timeView = view.findViewById(R.id.timeView);
            reminderTime = appPreferences.getReminderTime(getContext());
            timeView.setText(reminderTime.toString());
            return view;
        }

        int getReminderDaysBefore() {
            return reminderDaysBefore;
        }

        SimpleTime getReminderTime() {
            return reminderTime;
        }

        void setAddList(boolean addList) {
            this.addList = addList;
        }

        void setDayDescription(String dayDescription) {
            this.dayDescription = dayDescription;
        }
    }

    public static class AskForLook extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.ask_for_look_fragment, container, false);
        }
    }

    public void onButtonMiniClicked(View view){
        setPreferencesValues(false);
    }

    public void onButtonRichClicked(View view){
        setPreferencesValues(true);
    }

    private void setPreferencesValues(boolean isLookRich) {
        AppPreferences appPreferences = new AppPreferences(this);
        appPreferences.setLookRich(this, isLookRich);
        appPreferences.setFirstRun(this, false);
        recreate();
    }
/*
    public static class AskForReminderTime extends NoticeDialogFragment {

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setTitle(R.string.title_choose_events)
                    .setView(R.layout.reminder_settings_dialog_view)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((MainActivity) getActivity()).showInfo(R.string.info_cancelled,false);
                        }
                    });

            return builder.create();
        }
    }
    */
}
