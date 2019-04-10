package eu.adamgiergun.wontgoshoppingpl.main;

/*
    TODO "usuń wszystko z Kalendarza" w ustawieniach
    TODO grupa info w ustawieniach
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.*;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.*;
import androidx.lifecycle.*;

import com.google.android.gms.ads.*;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.DateFormat;
import java.util.Calendar;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;
import eu.adamgiergun.wontgoshoppingpl.common.AppCompatActivityWithTheme;
import eu.adamgiergun.wontgoshoppingpl.help.HelpActivity;
import eu.adamgiergun.wontgoshoppingpl.days.LimitedCommerceDay;
import eu.adamgiergun.wontgoshoppingpl.days.LimitedCommerceDayType;
import eu.adamgiergun.wontgoshoppingpl.days.ListOfLimitedCommerceDays;
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst;
import eu.adamgiergun.wontgoshoppingpl.common.LogFirebaseEvent;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity;
import eu.adamgiergun.wontgoshoppingpl.common.ThisAppDialogFragment;

public class MainActivity
        extends AppCompatActivityWithTheme
        implements NoticeDialogFragment.NoticeDialogListener,
        DayFragment.OnListFragmentInteractionListener,
        NoticeDialogFragment.NoticeDialogViewListener, LogFirebaseEvent {

    private MainViewModel mViewModel;

    //region ActivityLifeCycleRegion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBannerAds();
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        if (savedInstanceState == null) {
            mViewModel.initialize(this);

//        Calendar calEnd = Calendar.getInstance();
//        String timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
//        Toast.makeText(this, timeDifference, Toast.LENGTH_LONG).show();

            if (mViewModel.isFirstRun(this)) {
                addFragment(AskForLook.newInstance());
            } else {
                setDayFragment();
            }
        } else {
            if (mViewModel.isRecreatedOnDemand()) {
                mViewModel.setRecreatedOnDemand(false);
                setDayFragment();
            }
        }
    }

    private void setDayFragment() {
        addFragment(new DayFragment());
        if (!mViewModel.wasAppWidgetInfoDisplayed(this)) {
            WidgetInfoDialog.newInstance().show(getSupportFragmentManager(), "");
        }
        if (mViewModel.isLookRich(this)) {
            logFirebaseEvent(this, APP_THEME, LOOK_RICH);
        } else {
            logFirebaseEvent(this, APP_THEME, LOOK_MINI);
        }
    }

    private void setupBannerAds() {
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, getString(R.string.ad_mob_app_id));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.closeDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mViewModel.addDayFragment(this)) {
            addFragment(new DayFragment());
        }
        if (mViewModel.isLookRich(this)) {
            setLookRich();
        }
    }

    private void setLookRich() {
        View recyclerViewContainer = findViewById(R.id.main_view);
        recyclerViewContainer.setBackgroundResource(R.drawable.bg_red);

        View adViewLayout = findViewById(R.id.adViewLayout);
        adViewLayout.setBackgroundResource(R.drawable.bg_red);
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
    //endregion

    @Override
    public void logFirebaseEvent(Context context, String event, String full_text) {
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle params = new Bundle();
        params.putString("full_text", full_text);
        mFirebaseAnalytics.logEvent(event, params);
    }

    //region AppMenuHandlingRegion
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mViewModel.isFirstRun(this)) {
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
                mViewModel.setActionAddList(true);
                checkCalendarPermissions();
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

    private void checkCalendarPermissions() {
        if (isSystemLessThanMarshmallow() || areCalendarPermissionsGranted()) {
            checkCalendarSettings();
        } else {
            requestCalendarPermissions();
        }
    }

    private boolean isSystemLessThanMarshmallow() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    private boolean areCalendarPermissionsGranted() {
        return isPermissionGranted(Manifest.permission.READ_CALENDAR) && isPermissionGranted(Manifest.permission.WRITE_CALENDAR);
    }

    private void checkCalendarSettings() {
        if (mViewModel.isCalendarEnabledAndChosen(this)) {
            if (mViewModel.isActionAddList()) {
                askForTypesAddedToCalendar();
            } else {
                askForReminderSettings();
            }
        } else {
            startSettingsActivity(true);
        }
    }

    private void askForReminderSettings() {
        mViewModel.setReminderDialogArgs(this);
        ReminderSettingsDialog.newInstance().show(getSupportFragmentManager());
    }

    private void askForTypesAddedToCalendar() {
        TypesAddedToCalendarDialog typesAddedToCalendarDialog = new TypesAddedToCalendarDialog();
        typesAddedToCalendarDialog.setChecks(
                mViewModel.getAppPreferences().showNonCommerceDays(this),
                mViewModel.getAppPreferences().showHolidays(this),
                mViewModel.getAppPreferences().showCommerceSundays(this));
        typesAddedToCalendarDialog.show(getSupportFragmentManager(), "dialog");
    }

    private void startHelpActivity() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }
    //endregion

    @Override
    public void onListFragmentInteraction(LimitedCommerceDay day) {
        mViewModel.setLastTouchedDay(day);
        mViewModel.setActionAddList(false);
        checkCalendarPermissions();
    }

    //region ShowingInfoRegion
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
    //endregion

    //region PermissionsRegion
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
                checkCalendarSettings();
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
    //endregion

    //region onDialog...ClickRegion
    @Override
    public void onDialogPositiveClick(NoticeDialogFragment dialog) {
        if (dialog instanceof TypesAddedToCalendarDialog) {
            TypesAddedToCalendarDialog typesAddedToCalendarDialog = (TypesAddedToCalendarDialog) dialog;
            mViewModel.setTypesAddedToCalendar(typesAddedToCalendarDialog.getTypes());
            askForReminderSettings();
        }

        if (dialog instanceof ReminderSettingsDialog) {
            if (mViewModel.isActionAddList()) {
                addListToCalendar();
            } else {
                addDayToCalendar(mViewModel.getLastTouchedDay());
            }
        }
    }

    private void addListToCalendar() {
        showBigAd();
        ListOfLimitedCommerceDays list =
                ListOfLimitedCommerceDays.getList(this, mViewModel.getTypesAddedToCalendar());
        boolean resultIsPositive = list.addToCalendar(this, mViewModel.getReminder(), mViewModel.getTillObservable().getValue());

        logFirebaseEvent(this, CALENDAR_EVENT, LIST);
        displayInfo(resultIsPositive, R.string.info_added_to_calendar, R.string.error_sth_went_wrong);
    }

    private void addDayToCalendar(LimitedCommerceDay day) {
        showBigAd();
        day.setReminder(mViewModel.getReminder());
        displayInfo(
                day.addToGoogleCalendar(this),
                R.string.info_added_to_calendar,
                R.string.error_adding_to_calendar_failed);
        logFirebaseEvent(this, CALENDAR_EVENT, SINGLE);
    }

    private void showBigAd() {
        InterstitialAd mInterstitialAd = mViewModel.getInterstitialAd();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            if (!mInterstitialAd.isLoading()) {
                mViewModel.loadInterstitialAd();
            }
        }
    }

    @Override
    public void onDialogNegativeClick(NoticeDialogFragment dialog) {
        if (dialog instanceof TypesAddedToCalendarDialog || dialog instanceof ReminderSettingsDialog) {
            showInfo(R.string.info_cancelled, false);
        }
    }
    //endregion

    //region onViewClickRegion
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
        ReminderDaysBeforeDialog.newInstance().show(getSupportFragmentManager(), tag);
    }

    private void onReminderTimeClick(String tag) {
        ReminderTimeDialog.newInstance().show(getSupportFragmentManager(), tag);
    }

    private void onTillDateClick(String tag) {
        TillDateDialog.newInstance().show(getSupportFragmentManager(), tag);
    }
    //endregion

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

        static ReminderSettingsDialog newInstance() {
            return new ReminderSettingsDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = getStyledBuilder();
            String title;
            MainViewModel viewModel = getMainViewModel();

            if (viewModel.isActionAddList()) {
                title = getString(R.string.title_reminder);
            } else {
                title = getString(R.string.title_reminder_of) + " " + viewModel.getDayDescription();
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
            MainViewModel viewModel = getMainViewModel();

            if (viewModel.isActionAddList()) {
                view = inflater.inflate(R.layout.reminder_and_date_settings_dialog_view, null);
                initializeTillView(view, viewModel);
            } else {
                view = inflater.inflate(R.layout.reminder_settings_dialog_view, null);
            }
            initializeReminderViews(view, viewModel);

            return view;
        }

        private void initializeTillView(View view, MainViewModel viewModel) {
            AppCompatTextView tillView = getTillView(view);
            tillView.setText(getDateAsString(viewModel.getTill()));
            tillView.setOnClickListener(getViewListener(TillDateDialog.TAG));
        }

        private AppCompatTextView getTillView(View view) {
            return view.findViewById(R.id.tillDateView);
        }

        private static String getDateAsString(Calendar calendar) {
            DateFormat dateFormat = DateFormat.getDateInstance();
            return dateFormat.format(calendar.getTime());
        }

        private void initializeReminderViews(final View view, final MainViewModel viewModel) {
            final AppCompatTextView daysView = getDaysView(view);
            daysView.setText(String.valueOf(viewModel.getReminder().getDaysBefore()));
            daysView.setOnClickListener(getViewListener(ReminderDaysBeforeDialog.TAG));

            final AppCompatTextView timeView = getTimeView(view);
            timeView.setText(viewModel.getReminder().getTime().toString());
            timeView.setOnClickListener(getViewListener(ReminderTimeDialog.TAG));

            Observer<SimpleReminder> reminderObserver = new Observer<SimpleReminder>() {
                @Override
                public void onChanged(SimpleReminder reminder) {
                    daysView.setText(
                            String.valueOf(reminder.getDaysBefore()
                            ));
                    timeView.setText(
                            reminder.getTime().toString());
                }
            };

            viewModel.getReminderObservable().observe(this, reminderObserver);

            if (viewModel.isActionAddList()) {
                Observer<Calendar> tillObserver = new Observer<Calendar>() {
                    @Override
                    public void onChanged(Calendar till) {
                        getTillView(view).setText(
                                getDateAsString(till));
                    }
                };

                viewModel.getTillObservable().observe(this, tillObserver);
            }
        }

        private AppCompatTextView getTimeView(View view) {
            return view.findViewById(R.id.timeView);
        }

        private AppCompatTextView getDaysView(View view) {
            return view.findViewById(R.id.numberOfDaysBeforeView);
        }

        private void show(FragmentManager fragmentManager) {
            show(fragmentManager, TAG);
        }
    }

    //region InitialLookChoiceRegion
    public static class AskForLook extends Fragment {

        static AskForLook newInstance() {
            return new AskForLook();
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.ask_for_look_fragment, container, false);
            setText(view);
            return view;
        }

        void setText(View parentView) {
            TextView textView = parentView.findViewById(R.id.textView);
            textView.setText(LocalizedConst.getAskForLook());
        }
    }

    public void onButtonMiniClick(View view) {
        setPreferencesValues(false);
    }

    public void onButtonRichClick(View view) {
        setPreferencesValues(true);
    }


    private void setPreferencesValues(boolean isLookRich) {
        mViewModel.getAppPreferences().setLookRich(this, isLookRich);
        mViewModel.getAppPreferences().setFirstRun(this, false);
        mViewModel.setRecreatedOnDemand(true);
        recreate();
    }
    //endregion

    public static class ReminderDaysBeforeDialog extends ThisAppDialogFragment {
        private static final String TAG = "ReminderDaysBeforeDialog";
        private static final String VALUE = "value";

        private NumberPicker picker;

        static ReminderDaysBeforeDialog newInstance() {
            return new ReminderDaysBeforeDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            picker = getPicker(savedInstanceState);
            AlertDialog.Builder builder = getStyledBuilder();
            builder
                    .setTitle(R.string.days_before)
                    .setView(picker)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            updateReminder(picker.getValue());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }

        private NumberPicker getPicker(Bundle savedInstanceState) {
            if (savedInstanceState == null) {
                return MyNumberPicker.newInstance(getContext(), getMainViewModel().getReminderDaysBefore());
            } else {
                return MyNumberPicker.newInstance(getContext(), savedInstanceState.getInt(VALUE));
            }
        }

        private void updateReminder(int daysBefore) {
            getMainViewModel().setReminderDaysBefore(daysBefore);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(VALUE, picker.getValue());
        }
    }

    public static class ReminderTimeDialog extends ThisAppDialogFragment {
        private static final String TAG = "ReminderTimeDialog";
        private static final String HOUR = "hour";
        private static final String MINUTE = "minute";

        private MyTimePicker picker;

        static ReminderTimeDialog newInstance() {
            return new ReminderTimeDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            picker = getPicker(savedInstanceState);
            AlertDialog.Builder builder = getStyledBuilder();
            builder
                    .setView(picker)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            updateReminder(picker.getTime());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }

        private MyTimePicker getPicker(Bundle savedInstanceState) {
            if (savedInstanceState == null) {
                return MyTimePicker.newInstance(getContext(), getReminderTime());
            } else {
                int hour = savedInstanceState.getInt(HOUR);
                int minute = savedInstanceState.getInt(MINUTE);
                SimpleTime time = new SimpleTime(hour, minute);
                return MyTimePicker.newInstance(getContext(), time);
            }
        }

        private SimpleTime getReminderTime() {
            return getMainViewModel().getReminder().getTime();
        }

        private void updateReminder(SimpleTime time) {
            getMainViewModel().setReminderTime(time);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(HOUR, picker.getTime().getHour());
            outState.putInt(MINUTE, picker.getTime().getMinute());
        }
    }

    public static class TillDateDialog extends ThisAppDialogFragment {
        private static final String TAG = "TillDateDialog";
        private static final String YEAR = "year";
        private static final String MONTH = "month";
        private static final String DAY = "day";

        private MyDatePicker picker;

        static TillDateDialog newInstance() {
            return new TillDateDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            picker = getPicker(savedInstanceState);
            AlertDialog.Builder builder = getStyledBuilder();
            builder
                    .setView(picker)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            updateTill(picker.getDate());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }

        private MyDatePicker getPicker(Bundle savedInstanceState) {
            MyDatePicker myDatePicker = MyDatePicker.newInstance(getContext(), getMainViewModel().getTill());
            if (savedInstanceState != null) {
                int year = savedInstanceState.getInt(YEAR);
                int month = savedInstanceState.getInt(MONTH);
                int day = savedInstanceState.getInt(DAY);
                myDatePicker.updateDate(year, month, day);
            }
            return myDatePicker;
        }

        private void updateTill(Calendar date) {
            getMainViewModel().setTill(date);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(YEAR, picker.getYear());
            outState.putInt(MONTH, picker.getMonth());
            outState.putInt(DAY, picker.getDayOfMonth());
        }
    }

    public static class WidgetInfoDialog extends ThisAppDialogFragment {

        static WidgetInfoDialog newInstance() {
            return new WidgetInfoDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = getStyledBuilder();
            builder.setView(getDialogView());
            return builder.create();
        }

        private View getDialogView() {
            View view = getActivity().getLayoutInflater().inflate(R.layout.info_about_app_widget, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMainViewModel().setAppWidgetInfoDisplayed(getContext());
                    getDialog().dismiss();
                }
            });
            return view;
        }
    }
}
