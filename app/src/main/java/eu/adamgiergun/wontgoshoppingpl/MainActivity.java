package eu.adamgiergun.wontgoshoppingpl;

/*
    TODO "usuń wszystko z Kalendarza" w ustawieniach
    TODO grupa info w ustawieniach
    TODO dodawanie do kalendarza filtr daty
    TODO zmiana ustawień przypomnień w dialogu podczas dodawania
    TODO wyłącz dostęp do opcji w menu przed first_use
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.text.DateFormat;
import java.util.Calendar;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;
import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;

public class MainActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener, DayFragment.OnListFragmentInteractionListener {

    private LimitedCommerceDay lastTouchedDay;
    private LimitedCommerceDayType.TypesList typesToBeAddedToCalendar;
    private LimitedCommerceDayType.TypesList lastTypesToBeDisplayed;
    private SimpleReminder reminder;
    private AppPreferences appPreferences;
    private InterstitialAd mInterstitialAd;
    private AskForReminderSettings reminderDialog;
    private AskForTypesAddedToCalendar typesToAddToCalendarDialog;
    private Action actionRequestingPermissions;

    private enum Action {
        ADD_LIST,
        ADD_DAY
    }

    @Override
    public Resources.Theme getTheme() {
//        calStart = Calendar.getInstance();
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
        setupBannerAds();
        lastTypesToBeDisplayed = appPreferences.getLimitedCommerceDayTypes(this);

//        Calendar calEnd = Calendar.getInstance();
//        String timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
//        Toast.makeText(this, timeDifference, Toast.LENGTH_LONG).show();

        if (appPreferences.isFirstRun(this)) {
            addFragment(new AskForLook());
        } else {
            addFragment(new DayFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupInterstitialAds();
        LimitedCommerceDayType.TypesList newTypesToBeDisplayed = appPreferences.getLimitedCommerceDayTypes(this);
        if (!appPreferences.isFirstRun(this) && !lastTypesToBeDisplayed.equals(newTypesToBeDisplayed)) {
            addFragment(new DayFragment());
        }
        lastTypesToBeDisplayed = newTypesToBeDisplayed;
        setLook();
    }

    private void setLook() {
        if (appPreferences.isLookRich(this)) {
            View recyclerView = findViewById(R.id.main_view);
            recyclerView.setBackgroundResource(R.drawable.bg_red);
            View adViewLayout = findViewById(R.id.adViewLayout);
            adViewLayout.setBackgroundResource(R.drawable.bg_red);
        }
    }

    private void addFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment oldFragment = getSupportFragmentManager().findFragmentByTag("list");
        if (oldFragment != null)
            ft.detach(oldFragment);
        ft.add(R.id.main_fragment, fragment, "list");
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    private void setupBannerAds() {
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, getString(R.string.sample_admob_app_id));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setupInterstitialAds() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.sample_interstitial_ad_unit_id));
        loadInterstitialAd();
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
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
                checkCalendarPermissions(Action.ADD_LIST);
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

    private void checkCalendarPermissions(Action action) {
        if (isSystemLessThanMarshmallow() || areCalendarPermissionsGranted()) {
            checkCalendarSettings(action);
        } else {
            requestCalendarPermissions(action);
        }
    }

    private boolean isSystemLessThanMarshmallow() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    private boolean areCalendarPermissionsGranted() {
        return isPermissionGranted(Manifest.permission.READ_CALENDAR) && isPermissionGranted(Manifest.permission.WRITE_CALENDAR) ;
    }

    private void checkCalendarSettings(Action action) {
        if (isCalendarEnabledAndChosen()) {
            switch (action) {
                case ADD_DAY:
                    askForReminderSettings(action);
                    break;
                case ADD_LIST:
                    askForTypesAddedToCalendar();
                    break;
                default:
                    break;
            }
        } else {
        startSettingsActivity(true);
        }
    }

    private boolean isCalendarEnabledAndChosen() {
        boolean useGoogleCalendar = appPreferences.isUseGoogleCalendarSet(this);
        boolean isCalendarSet = appPreferences.isCalendarSet(this);
        return useGoogleCalendar && isCalendarSet;
    }

    private void askForReminderSettings(Action action) {
        reminderDialog = new AskForReminderSettings();
        reminderDialog.setAction(action);
        if (action == Action.ADD_DAY) {
            reminderDialog.setDayDescription(
                    lastTouchedDay.getDescriptionForDialog());
        }
        reminderDialog.show(getSupportFragmentManager(), "dialog");
    }

    private void askForTypesAddedToCalendar() {
        typesToAddToCalendarDialog = new AskForTypesAddedToCalendar();
        typesToAddToCalendarDialog.show(getSupportFragmentManager(), "dialog");
    }

    private void startHelpActivity() {
        //TODO help
    }

    @Override
    public void onListFragmentInteraction(LimitedCommerceDay day) {
        lastTouchedDay = day;
        checkCalendarPermissions(Action.ADD_DAY);
    }

    private void showBigAd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        if (!mInterstitialAd.isLoading()) {
            loadInterstitialAd();
        }
    }

    private void displayInfo(boolean resultIsPositive, int positiveInfo, int negativeInfo) {
        if (resultIsPositive)
            showInfo(positiveInfo, false);
        else
            showInfo(negativeInfo, false);
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

    private boolean isPermissionGranted(String permission) {
        int permissionState = ContextCompat.checkSelfPermission(this, permission);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCalendarPermissions(Action action) {
        actionRequestingPermissions = action;
        String[] listOfRequestedPermissions = new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
        int permissionTypeCode = PermissionType.CALENDAR.getCode();
        requestPermissions(listOfRequestedPermissions, permissionTypeCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionType.CALENDAR.getCode()) {
            if (areAllPermissionsGranted(grantResults)) {
                checkCalendarSettings(actionRequestingPermissions);
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

    private void addDayToCalendar(LimitedCommerceDay day) {
            showBigAd();
            reminder = appPreferences.getReminder(this, SimpleReminder.METHOD_ALERT);
            day.setReminder(reminder);
            displayInfo(
                    day.addToGoogleCalendar(this),
                    R.string.info_added_to_calendar,
                    R.string.error_adding_to_calendar_failed);
    }

    @Override
    public void onDialogPositiveClick(AppCompatDialogFragment dialog) {
        if (dialog instanceof AskForTypesAddedToCalendar) {
            typesToBeAddedToCalendar = typesToAddToCalendarDialog.getTypes();
            askForReminderSettings(Action.ADD_LIST);
        }
        if (dialog instanceof AskForReminderSettings) {
            reminder = reminderDialog.getReminder();
            if (reminderDialog.getAction() == Action.ADD_LIST) {
                addListToCalendar();
            } else {
                addDayToCalendar(lastTouchedDay);
            }
        }
    }

    private void addListToCalendar() {
        showBigAd();
        ListOfLimitedCommerceDays list =
                ListOfLimitedCommerceDays.getList(this, typesToBeAddedToCalendar);
        boolean resultIsPositive = list.addToCalendar(this, reminder, reminderDialog.getTill());
        displayInfo(resultIsPositive, R.string.info_added_to_calendar, R.string.error_sth_went_wrong);
    }


    @Override
    public void onDialogNegativeClick(AppCompatDialogFragment dialog) {
        showInfo(R.string.info_cancelled, false);
    }

    public void onNumberOfDaysBeforeClick(View view) {
        //TODO stub
    }

    public static class AskForTypesAddedToCalendar extends NoticeDialogFragment {
        private Button dialogButtonPositive;
        private boolean addCommerceSundays;
        private boolean addHolidays;
        private boolean addNonCommerceDays;

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            CharSequence[] items = new CharSequence[]
                    {getString(R.string.days_of_limited_commerce),
                    getString(R.string.holidays),
                    getString(R.string.commerce_sundays)};

            Context context = getContext();
            AppPreferences appPreferences = new AppPreferences(context);
            final boolean[] checkedItems = new boolean[3];
            checkedItems[0] = appPreferences.showNonCommerceDays(context);
            checkedItems[1] = appPreferences.showHolidays(context);
            checkedItems[2] = appPreferences.showCommerceSundays(context);

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
                            addNonCommerceDays = checkedItems[0];
                            addHolidays = checkedItems[1];
                            addCommerceSundays = checkedItems[2];
                            noticeDialogListener.onDialogPositiveClick(AskForTypesAddedToCalendar.this);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            noticeDialogListener.onDialogNegativeClick(AskForTypesAddedToCalendar.this);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialogButtonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            dialogButtonPositive.setEnabled(isAnyValueInBooleanArrayTrue(checkedItems));
            return dialog;
        }

        private boolean isAnyValueInBooleanArrayTrue(boolean[] array) {
            for (boolean value : array)
                if (value) return true;
            return false;
        }

        LimitedCommerceDayType.TypesList getTypes() {
            return LimitedCommerceDayType.getTypes(
                    addNonCommerceDays,
                    addHolidays,
                    addCommerceSundays);
        }
    }

    public static class AskForReminderSettings extends NoticeDialogFragment {

        private SimpleReminder reminder;
        private Action action;
        private String dayDescription;
        private Calendar till;

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
            if (action == Action.ADD_LIST) {
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

        @SuppressLint("InflateParams")
        private View getDialogView() {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view;
            if (action == Action.ADD_LIST) {
                view = inflater.inflate(R.layout.reminder_and_date_settings_dialog_view, null);
                initializeTill(view);
            } else {
                view = inflater.inflate(R.layout.reminder_settings_dialog_view, null);
            }

            Context context = getContext();
            AppPreferences appPreferences = new AppPreferences(context);
            reminder = appPreferences.getReminder(context, SimpleReminder.METHOD_ALERT);

            AppCompatTextView daysView = view.findViewById(R.id.numberOfDaysBeforeView);
            daysView.setText(String.valueOf(reminder.getDaysBefore()));

            AppCompatTextView timeView = view.findViewById(R.id.timeView);
            timeView.setText(reminder.getTime().toString());

            return view;
        }

        SimpleReminder getReminder() {
            return reminder;
        }

        public Action getAction() {
            return action;
        }

        void setAction(Action action) {
            this.action = action;
        }

        void setDayDescription(String dayDescription) {
            this.dayDescription = dayDescription;
        }

        Calendar getTill() {
            return till;
        }

        private void initializeTill(View view) {
            AppCompatTextView tillView = view.findViewById(R.id.tillDateView);
            tillView.setText(initializeTill());
        }

        private String initializeTill() {
            till = Calendar.getInstance();
            till.add(Calendar.MONTH, 1);
            till.set(Calendar.DAY_OF_MONTH, till.getActualMaximum(Calendar.DAY_OF_MONTH));
            return getTillAsString();
        }

        private String getTillAsString() {
            DateFormat dateFormat = DateFormat.getDateInstance();
            return dateFormat.format(till.getTime());
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
