package eu.adamgiergun.wontgoshoppingpl;

/*
    TODO "usuń wszystko z Kalendarza" w ustawieniach
    TODO grupa info w ustawieniach
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.view.*;
import android.widget.*;

//import com.google.android.gms.ads.AdListener;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;
import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;

public class MainActivity
        extends AppCompatActivityWithTheme
        implements NoticeDialogFragment.NoticeDialogListener,
                    DayFragment.OnListFragmentInteractionListener,
                    NoticeDialogFragment.NoticeDialogViewListener {

    private LimitedCommerceDay lastTouchedDay;
    private LimitedCommerceDayType.TypesList typesAddedToCalendar;
    private LimitedCommerceDayType.TypesList lastTypesToBeDisplayed;
    private AppPreferences appPreferences;
    private InterstitialAd mInterstitialAd;
    private Action actionRequestingPermissions;
    private SQLiteDatabase database;

    private enum Action {
        ADD_LIST,
        ADD_DAY
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new DatabaseHandler(this).getWritableDatabase();
        appPreferences = new AppPreferences(this);
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
        MobileAds.initialize(this, getString(R.string.ad_mob_app_id));
        AdView mAdView = findViewById(R.id.adView);
        //setAdListener(mAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
/*
    private void setAdListener(AdView adView) {
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                System.out.println("onAdLoaded() CALLBACK");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                System.out.println("onAdFailedToLoad() CALLBACK code: " + errorCode);
            }
        });
    }
*/
    private void setupInterstitialAds() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.wgs_interstitial_ad_id));
        loadInterstitialAd();
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (appPreferences.isFirstRun(this)) {
            getMenuInflater()
                    .inflate(R.menu.main_activity_actions_first_run, menu);
        } else {
            getMenuInflater()
                    .inflate(R.menu.main_activity_actions, menu);
        }
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
        ReminderSettingsDialog reminderSettingsDialog = new ReminderSettingsDialog();
        ReminderSettingsDialog.setReminder(appPreferences.getReminder(this, SimpleReminder.METHOD_ALERT));
        ReminderSettingsDialog.setAction(action);
        if (action == Action.ADD_DAY) {
            ReminderSettingsDialog.setDayDescription(
                    lastTouchedDay.getDescriptionForDialog());
        } else {
            ReminderSettingsDialog.setTill(getLastDayOfNextMonth());
        }
        reminderSettingsDialog.show(getSupportFragmentManager());
    }

    private Calendar getLastDayOfNextMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar;

    }

    private void askForTypesAddedToCalendar() {
        TypesAddedToCalendarDialog typesAddedToCalendarDialog = new TypesAddedToCalendarDialog();
        typesAddedToCalendarDialog.setChecks(
                appPreferences.showNonCommerceDays(this),
                appPreferences.showHolidays(this),
                appPreferences.showCommerceSundays(this));
        typesAddedToCalendarDialog.show(getSupportFragmentManager(), "dialog");
    }

    private void startHelpActivity() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
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
        day.setReminder(ReminderSettingsDialog.getReminder());
        displayInfo(
                day.addToGoogleCalendar(this),
                R.string.info_added_to_calendar,
                R.string.error_adding_to_calendar_failed);
    }

    @Override
    public void onDialogPositiveClick(NoticeDialogFragment dialog) {
        if (dialog instanceof TypesAddedToCalendarDialog) {
            TypesAddedToCalendarDialog typesAddedToCalendarDialog = (TypesAddedToCalendarDialog) dialog;
            typesAddedToCalendar = typesAddedToCalendarDialog.getTypes();
            askForReminderSettings(Action.ADD_LIST);
        }

        if (dialog instanceof ReminderSettingsDialog) {
            if (ReminderSettingsDialog.getAction() == Action.ADD_LIST) {
                addListToCalendar();
            } else {
                addDayToCalendar(lastTouchedDay);
            }
        }

        if (dialog instanceof ReminderDaysBeforeDialog) {
            ReminderDaysBeforeDialog reminderDaysBeforeDialog = (ReminderDaysBeforeDialog) dialog;
            ReminderSettingsDialog reminderSettingsDialog = ReminderSettingsDialog.getActiveInstance(getSupportFragmentManager());
            reminderSettingsDialog.setDaysBefore(reminderDaysBeforeDialog.getDaysBefore());
        }

        if (dialog instanceof ReminderTimeDialog) {
            ReminderSettingsDialog reminderSettingsDialog = ReminderSettingsDialog.getActiveInstance(getSupportFragmentManager());
            reminderSettingsDialog.setTime(ReminderTimeDialog.getTime());
        }

        if (dialog instanceof TillDateDialog) {
            ReminderSettingsDialog reminderSettingsDialog = ReminderSettingsDialog.getActiveInstance(getSupportFragmentManager());
            TillDateDialog tillDateDialog = (TillDateDialog) dialog;
            reminderSettingsDialog.setTillAndView(tillDateDialog.getDate());
        }
    }

    private void addListToCalendar() {
        showBigAd();
        ListOfLimitedCommerceDays list =
                ListOfLimitedCommerceDays.getList(this, typesAddedToCalendar);
        Calendar tillDate = ReminderSettingsDialog.getTill();
        SimpleReminder reminder = ReminderSettingsDialog.getReminder();
        boolean resultIsPositive = list.addToCalendar(this, reminder, tillDate);
        displayInfo(resultIsPositive, R.string.info_added_to_calendar, R.string.error_sth_went_wrong);
    }

    @Override
    public void onDialogNegativeClick(NoticeDialogFragment dialog) {
        if (dialog instanceof TypesAddedToCalendarDialog || dialog instanceof ReminderSettingsDialog) {
            showInfo(R.string.info_cancelled, false);
        }
    }

    @Override
    public void onViewClick(String tag) {
        switch (tag) {
            case ReminderDaysBeforeDialog.TAG:
                onReminderDaysBeforeClick(tag);
                break;
            case ReminderTimeDialog.TAG:
                onReminderTimeClick(tag);
                break;
            case TillDateDialog.TAG:
                onTillDateClick(tag);
                break;
            default:
                break;
        }
    }

    private void onReminderDaysBeforeClick(String tag) {
        ReminderDaysBeforeDialog reminderDaysBeforeDialog = new ReminderDaysBeforeDialog();
        reminderDaysBeforeDialog.setDaysBefore(ReminderSettingsDialog.getReminder().getDaysBefore());
        reminderDaysBeforeDialog.show(getSupportFragmentManager(), tag);
    }

    private void onReminderTimeClick(String tag) {
        ReminderTimeDialog reminderTimeDialog = new ReminderTimeDialog();
        ReminderTimeDialog.setTime(ReminderSettingsDialog.getReminder().getTime());
        reminderTimeDialog.show(getSupportFragmentManager(), tag);
    }

    private void onTillDateClick(String tag) {
        TillDateDialog tillDateDialog = new TillDateDialog();
        tillDateDialog.setDate(ReminderSettingsDialog.getTill());
        tillDateDialog.show(getSupportFragmentManager(), tag);
    }

    public static class TypesAddedToCalendarDialog extends ThisAppDialogFragment {
        private Button dialogButtonPositive;
        private static boolean addNonCommerceDays;
        private static boolean addHolidays;
        private static boolean addCommerceSundays;

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            CharSequence[] items = new CharSequence[]
                    {getString(R.string.days_of_limited_commerce),
                    getString(R.string.holidays),
                    getString(R.string.commerce_sundays)};
            final boolean[] checkedItems = new boolean[3];
            checkedItems[0] = addNonCommerceDays;
            checkedItems[1] = addHolidays;
            checkedItems[2] = addCommerceSundays;

            DialogInterface.OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    checkedItems[which] = isChecked;
                    switch (which) {
                        case 0:
                            addNonCommerceDays = isChecked;
                            break;
                        case 1:
                            addHolidays = isChecked;
                            break;
                        case 2:
                            addCommerceSundays = isChecked;
                            break;
                        default:
                            break;
                    }
                    dialogButtonPositive.setEnabled(isAnyValueInBooleanArrayTrue(checkedItems));
                }
            };

            AlertDialog.Builder builder = getStyledBuilder();
            builder.setTitle(R.string.title_choose_events)
                    .setMultiChoiceItems(items, checkedItems, listener)
                    .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))
                    .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this));
            AlertDialog dialog = builder.create();
            dialog.show();
            dialogButtonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            dialogButtonPositive.setEnabled(isAnyValueInBooleanArrayTrue(checkedItems));
            return dialog;
        }

        private void setChecks(boolean checkNonCommerceDays, boolean checkHolidays, boolean checkCommerceSundays) {
            addNonCommerceDays = checkNonCommerceDays;
            addHolidays = checkHolidays;
            addCommerceSundays = checkCommerceSundays;
        }

        private boolean isAnyValueInBooleanArrayTrue(boolean[] array) {
            for (boolean value : array)
                if (value) return true;
            return false;
        }

        private LimitedCommerceDayType.TypesList getTypes() {
            return LimitedCommerceDayType.getTypes(
                    addNonCommerceDays,
                    addHolidays,
                    addCommerceSundays);
        }
    }

    public static class ReminderSettingsDialog extends ThisAppDialogFragment {
        private static final String TAG = "reminder_dialog";
        private static SimpleReminder reminder;
        private static Action action;
        private static String dayDescription;
        private static Calendar till;
        private AppCompatTextView daysView;
        private AppCompatTextView timeView;
        private AppCompatTextView tillView;

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = getStyledBuilder();
            String title;
            if (action == Action.ADD_LIST) {
                title = getString(R.string.title_reminder);
            } else {
                title = getString(R.string.title_reminder_of) + " " + dayDescription ;
            }
            builder
                    .setTitle(title)
                    .setView(getDialogView())
                    .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))
                    .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this));
            return builder.create();
        }

        @SuppressLint("InflateParams")
        private View getDialogView() {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view;
            if (action == Action.ADD_LIST) {
                view = inflater.inflate(R.layout.reminder_and_date_settings_dialog_view, null);
                initializeTillView(view);
            } else {
                view = inflater.inflate(R.layout.reminder_settings_dialog_view, null);
            }
            initializeReminderViews(view);
            return view;
        }

        private void initializeTillView(View view) {
            tillView = view.findViewById(R.id.tillDateView);
            tillView.setText(getTillAsString());
            tillView.setOnClickListener(getViewListener(TillDateDialog.TAG));
        }

        private static String getTillAsString() {
            DateFormat dateFormat = DateFormat.getDateInstance();
            return dateFormat.format(till.getTime());
        }

        private void initializeReminderViews(View view) {
            daysView = view.findViewById(R.id.numberOfDaysBeforeView);
            daysView.setText(String.valueOf(reminder.getDaysBefore()));
            daysView.setOnClickListener(getViewListener(ReminderDaysBeforeDialog.TAG));

            timeView = view.findViewById(R.id.timeView);
            timeView.setText(reminder.getTime().toString());
            timeView.setOnClickListener(getViewListener(ReminderTimeDialog.TAG));
        }

        private void show(FragmentManager fragmentManager) {
            show(fragmentManager, TAG);
        }

        private static SimpleReminder getReminder() {
            return reminder;
        }

        private static void setReminder(SimpleReminder simpleReminder) {
            reminder = simpleReminder;
        }

        private static Action getAction() {
            return action;
        }

        private static void setAction(Action action) {
            ReminderSettingsDialog.action = action;
        }

        private static void setDayDescription(String description) {
            dayDescription = description;
        }

        private void setDaysBefore(int number) {
            reminder.setDaysBefore(number);
            daysView.setText(String.valueOf(number));
        }

        private void setTime(SimpleTime time) {
            reminder.setTime(time);
            timeView.setText(time.toString());
        }

        private static Calendar getTill() {
            return till;
        }

        private static void setTill(Calendar calendar) {
            till = calendar;
        }

        private void setTillAndView(Calendar calendar) {
            setTill(calendar);
            tillView.setText(getTillAsString());
        }

        private static ReminderSettingsDialog getActiveInstance(FragmentManager fragmentManager) {
            return (ReminderSettingsDialog) fragmentManager.findFragmentByTag(TAG);
        }
    }

    public static class AskForLook extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.ask_for_look_fragment, container, false);
        }
    }

    public void onButtonMiniClick(View view){
        setPreferencesValues(false);
    }

    public void onButtonRichClick(View view){
        setPreferencesValues(true);
    }

    private void setPreferencesValues(boolean isLookRich) {
        AppPreferences appPreferences = new AppPreferences(this);
        appPreferences.setLookRich(this, isLookRich);
        appPreferences.setFirstRun(this, false);
        recreate();
    }

    public static class ReminderDaysBeforeDialog extends ThisAppDialogFragment {
        private static final String TAG = "ReminderDaysBeforeDialog";
        private static int daysBefore;
        private NumberPicker picker;

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = getStyledBuilder();
            initializePicker();
            builder
                    .setTitle(R.string.days_before)
                    .setView(picker)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            daysBefore = picker.getValue();
                            noticeDialogListener.onDialogPositiveClick(ReminderDaysBeforeDialog.this);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(ReminderDaysBeforeDialog.this));
            return builder.create();
        }

        private void initializePicker() {
            picker = new NumberPicker(getContext());
            picker.setMinValue(1);
            picker.setMaxValue(9);
            picker.setValue(daysBefore);
        }

        private int getDaysBefore() {
            return daysBefore;
        }

        private void setDaysBefore(int daysBefore) {
            ReminderDaysBeforeDialog.daysBefore = daysBefore;
        }
    }

    public static class ReminderTimeDialog extends ThisAppDialogFragment {
        private static final String TAG = "ReminderTimeDialog";
        private TimePicker picker;
        private static SimpleTime time;

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            this.setRetainInstance(true);
            AlertDialog.Builder builder = getStyledBuilder();
            initializePicker();
            builder
                    .setView(picker)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            time = getTimeFromPicker();
                            noticeDialogListener.onDialogPositiveClick(ReminderTimeDialog.this);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            noticeDialogListener.onDialogNegativeClick(ReminderTimeDialog.this);
                        }
                    });
            return builder.create();
        }

        @SuppressWarnings("deprecation")
        private void initializePicker() {
            picker = new TimePicker(getContext());
            picker.setIs24HourView(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                picker.setHour(time.getHour());
                picker.setMinute(time.getMinute());
            } else {
                picker.setCurrentHour(time.getHour());
                picker.setCurrentMinute(time.getMinute());
            }
        }

        @SuppressWarnings("deprecation")
        private SimpleTime getTimeFromPicker() {
            SimpleTime time;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                time = new SimpleTime(picker.getHour(), picker.getMinute());
            } else {
                time = new SimpleTime(picker.getCurrentHour(), picker.getCurrentMinute());
            }
            return time;
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        private static SimpleTime getTime() {
            return time;
        }

        private static void setTime(SimpleTime time) {
            ReminderTimeDialog.time = time;
        }
    }

    public static class TillDateDialog extends ThisAppDialogFragment {
        private static final String TAG = "TillDateDialog";
        private DatePicker picker;
        private static Calendar date;

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = getStyledBuilder();
            initializePicker();
            builder
                    .setView(picker)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            date = getDateFromPicker();
                            noticeDialogListener.onDialogPositiveClick(TillDateDialog.this);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            noticeDialogListener.onDialogNegativeClick(TillDateDialog.this);
                        }
                    });
            return builder.create();
        }

        private void initializePicker() {
            picker = new DatePicker(getContext());
            picker.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
            picker.setMaxDate(getMaxDate());
        }

        private long getMaxDate() {
            Calendar maxDate = Calendar.getInstance();
            maxDate.set(Calendar.YEAR, 2019);
            maxDate.set(Calendar.MONTH, 11);
            maxDate.set(Calendar.DAY_OF_MONTH, 31);
            return maxDate.getTimeInMillis();
        }

        private Calendar getDateFromPicker() {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, picker.getYear());
            cal.set(Calendar.MONTH, picker.getMonth());
            cal.set(Calendar.DAY_OF_MONTH, picker.getDayOfMonth());
            return cal;
        }

        private Calendar getDate() {
            return date;
        }

        private void setDate(Calendar date) {
            TillDateDialog.date = date;
        }
    }

}
