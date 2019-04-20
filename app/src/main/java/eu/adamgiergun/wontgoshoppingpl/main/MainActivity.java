package eu.adamgiergun.wontgoshoppingpl.main;

/*
    TODO check Types chooser behaviour

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

import java.text.DateFormat;
import java.util.Calendar;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;
import eu.adamgiergun.wontgoshoppingpl.common.AppCompatActivityWithTheme;
import eu.adamgiergun.wontgoshoppingpl.help.HelpActivity;
import eu.adamgiergun.wontgoshoppingpl.day.*;
import eu.adamgiergun.wontgoshoppingpl.common.*;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsActivity;

import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.*;

public class MainActivity
        extends AppCompatActivityWithTheme
        implements NoticeDialogFragment.NoticeDialogListener,
        DaysFragment.OnListFragmentInteractionListener,
        NoticeDialogFragment.NoticeDialogViewListener {

    private static final String MAIN_FRAGMENT_TAG = "main_fragment";

    private MainViewModel mViewModel;
//    private Calendar calStart;

    //region ActivityLifeCycleRegion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        calStart = Calendar.getInstance();
        setContentView(R.layout.activity_main);
        setupBannerAds();
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        if (savedInstanceState == null) {
            mViewModel.initialize(this);
            if (mViewModel.isFirstRun(this)) {
                addFragment(AskForLook.newInstance());
            } else {
                setDayFragment();
            }
        } else {
            if (mViewModel.isRecreatedOnDemand()) {
                setDayFragment();
            }
        }
    }

    private void setDayFragment() {
        addFragment(new DaysFragment());
        showWidgetInfoIfNeeded();
        mViewModel.logLook(this);
    }

    private void showWidgetInfoIfNeeded() {
        if (!mViewModel.wasAppWidgetInfoDisplayed(this)) {
            WidgetInfoDialog
                    .newInstance()
                    .show(getSupportFragmentManager(), "");
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
        if (mViewModel.checkIfDayTypesChanged(this)) {
            refreshList();
        }
        if (mViewModel.isLookRich(this)) {
            setLookRich();
        }
//        showStartTime();
    }

    private void refreshList() {
        //addFragment(new DaysFragment());
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (fragment instanceof DaysFragment) {
            ((DaysFragment) fragment).refreshRecyclerView();
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
        Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(MAIN_FRAGMENT_TAG);
        if (oldFragment != null)
            ft.detach(oldFragment);
        ft.add(R.id.main_fragment, fragment, MAIN_FRAGMENT_TAG);
        ft.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

//    private void showStartTime() {
//        Calendar calEnd = Calendar.getInstance();
//        String timeDifference = String.valueOf(calEnd.getTimeInMillis() - calStart.getTimeInMillis()) + " ms";
//        Toast.makeText(this, timeDifference, Toast.LENGTH_LONG).show();
//    }
    //endregion

    //region AppMenuHandlingRegion
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuLayoutId = mViewModel.isFirstRun(this) ? R.menu.main_activity_actions_first_run : R.menu.main_activity_actions;
        getMenuInflater().inflate(menuLayoutId, menu);
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
                askForDayTypesAddedToCalendar();
            } else {
                askForReminderSettings();
            }
        } else {
            startSettingsActivity(true);
        }
    }

    private void askForReminderSettings() {
        mViewModel.setReminderDialogArgs(this);
        ReminderSettingsDialog
                .newInstance()
                .show(getSupportFragmentManager(), "");
    }

    private void askForDayTypesAddedToCalendar() {
        DayTypesAddedToCalendarDialog
                .newInstance()
                .show(getSupportFragmentManager(), "dialog");
    }

    private void startHelpActivity() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }
    //endregion

    @Override
    public void onListFragmentInteraction(Day day) {
        mViewModel.setLastTouchedDay(day);
        checkCalendarPermissions();
    }

    //region ShowingInfoRegion
    private void showInfo(boolean resultIsPositive, int positiveInfo, int negativeInfo) {
        int info = resultIsPositive ? positiveInfo : negativeInfo;
        showInfo(info, false);
    }

    private void showInfo(int info, boolean isLengthLong) {
        int length = isLengthLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast
                .makeText(this, info, length)
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

    //region on...ClickRegion
    @Override
    public void onDialogPositiveClick(NoticeDialogFragment dialog) {
        if (dialog instanceof DayTypesAddedToCalendarDialog) {
            askForReminderSettings();
        } else if (dialog instanceof ReminderSettingsDialog) {
            addToCalendar();
        }
    }

    private void addToCalendar() {
        showBigAd();

        boolean isResultPositive = mViewModel.addToCalendar(this);
        if (isResultPositive) {
            refreshList();
        }
        showInfo(isResultPositive, R.string.info_added_to_calendar, mViewModel.getError());
    }

    private void showBigAd() {
        InterstitialAd interstitialAd = mViewModel.getInterstitialAd();
        if (interstitialAd != null) {
            interstitialAd.show();
        }
    }

    @Override
    public void onDialogNegativeClick(NoticeDialogFragment dialog) {
        if (dialog instanceof DayTypesAddedToCalendarDialog || dialog instanceof ReminderSettingsDialog) {
            showInfo(R.string.info_cancelled, false);
        }
    }

    public void onLookButtonClick(View view) {
        boolean isLookRich = (view.getId() == R.id.imageButtonRich);
        mViewModel.onInitialLookChoice(this, isLookRich);
        recreate();
    }

    @Override
    public void onViewClick(NoticeDialogFragment noticeDialogFragment) {
        noticeDialogFragment.show(getSupportFragmentManager(), "");
    }
    //endregion

    public static class DayTypesAddedToCalendarDialog extends MainDialogFragment {
        private Button dialogButtonPositive;

        public static NoticeDialogFragment newInstance() {
            return new DayTypesAddedToCalendarDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            final boolean[] checkedItems = getCheckedItems();
            AlertDialog dialog = getAlertDialog(checkedItems);
            dialog.show();
            dialogButtonPositive = getDialogButtonPositive(dialog, checkedItems);
            return dialog;
        }

        private boolean[] getCheckedItems() {
            boolean[] checkedItems = new boolean[3];
            List dayTypesToBeAddedToCalendar = getViewModel().getDayTypesToBeAddedToCalendar();

            checkedItems[NON_COMMERCE_DAY.getCode()] = dayTypesToBeAddedToCalendar.contains(NON_COMMERCE_DAY);
            checkedItems[HOLIDAY.getCode()] = dayTypesToBeAddedToCalendar.contains(HOLIDAY);
            checkedItems[COMMERCE_SUNDAY.getCode()] = dayTypesToBeAddedToCalendar.contains(COMMERCE_SUNDAY);
            return checkedItems;
        }

        private AlertDialog getAlertDialog(final boolean[] checkedItems) {
            CharSequence[] items = getItems();
            DialogInterface.OnMultiChoiceClickListener listener = getOnMultiChoiceClickListener(checkedItems);

            AlertDialog.Builder builder = getStyledBuilder(getContext());
            builder
                    .setTitle(R.string.title_choose_events)
                    .setMultiChoiceItems(items, checkedItems, listener)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getViewModel().setDayTypesToBeAddedToCalendar(getDayTypesList(checkedItems));
                            DayTypesAddedToCalendarDialog.this.noticeDialogListener.onDialogPositiveClick(DayTypesAddedToCalendarDialog.this);
                        }
                    })
                    .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))

                    .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this));
            return builder.create();
        }

        private CharSequence[] getItems() {
            return new CharSequence[] {
                    getString(R.string.days_of_limited_commerce),
                    getString(R.string.holidays),
                    getString(R.string.commerce_sundays)
            };
        }

        private DialogInterface.OnMultiChoiceClickListener getOnMultiChoiceClickListener(final boolean[] checkedItems) {
            return new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    getViewModel().setDayTypesToBeAddedToCalendar(getDayTypesList(checkedItems));
                    dialogButtonPositive.setEnabled(isAnyValueInBooleanArrayTrue(checkedItems));
                }
            };
        }

        private Button getDialogButtonPositive(AlertDialog dialog, boolean[] checkedItems) {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setEnabled(isAnyValueInBooleanArrayTrue(checkedItems));
            return button;
        }

        private boolean isAnyValueInBooleanArrayTrue(boolean[] array) {
            for (boolean value : array)
                if (value) return true;
            return false;
        }

        private List getDayTypesList(boolean[] checkedItems) {
            return Day.Type.newList(
                    checkedItems[NON_COMMERCE_DAY.getCode()],
                    checkedItems[HOLIDAY.getCode()],
                    checkedItems[COMMERCE_SUNDAY.getCode()]);
        }
    }

    public static class ReminderSettingsDialog extends MainDialogFragment {

        public static NoticeDialogFragment newInstance() {
            return new ReminderSettingsDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = getStyledBuilder(getContext());
            MainViewModel viewModel = getViewModel();
            String title = viewModel.isActionAddList() ? getString(R.string.title_reminder) : getString(R.string.title_reminder_of) + " " + viewModel.getDayDescription();
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
            MainViewModel viewModel = getViewModel();
            int viewLayoutId = viewModel.isActionAddList() ? R.layout.reminder_and_date_settings_dialog_view : R.layout.reminder_settings_dialog_view;
            View view = inflater.inflate(viewLayoutId, null);

            initializeReminderViews(view, viewModel);
            if (viewModel.isActionAddList()) {
                initializeTillView(view, viewModel);
            }

            return view;
        }

        private void initializeTillView(View view, MainViewModel viewModel) {
            AppCompatTextView tillView = getTillView(view);
            tillView.setText(getDateAsString(viewModel.getTill()));
            tillView.setOnClickListener(getViewListener(TillDateDialog.newInstance()));
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
            daysView.setOnClickListener(getViewListener(ReminderDaysBeforeDialog.newInstance()));

            final AppCompatTextView timeView = getTimeView(view);
            timeView.setText(viewModel.getReminder().getTime().toString());
            timeView.setOnClickListener(getViewListener(ReminderTimeDialog.newInstance()));

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
    }

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

    public static class ReminderDaysBeforeDialog extends MainDialogFragment {
        private static final String VALUE = "value";

        private NumberPicker picker;

        public static NoticeDialogFragment newInstance() {
            return new ReminderDaysBeforeDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            picker = getPicker(savedInstanceState);
            AlertDialog.Builder builder = getStyledBuilder(getContext());
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
            return savedInstanceState == null ?
                    MyNumberPicker.newInstance(getContext(), getViewModel().getReminderDaysBefore()) :
                    MyNumberPicker.newInstance(getContext(), savedInstanceState.getInt(VALUE));
        }

        private void updateReminder(int daysBefore) {
            getViewModel().setReminderDaysBefore(daysBefore);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(VALUE, picker.getValue());
        }
    }

    public static class ReminderTimeDialog extends MainDialogFragment {
        private static final String HOUR = "hour";
        private static final String MINUTE = "minute";

        private MyTimePicker picker;

        public static NoticeDialogFragment newInstance() {
            return new ReminderTimeDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            picker = getPicker(savedInstanceState);
            AlertDialog.Builder builder = getStyledBuilder(getContext());
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
            return getViewModel().getReminder().getTime();
        }

        private void updateReminder(SimpleTime time) {
            getViewModel().setReminderTime(time);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(HOUR, picker.getTime().getHour());
            outState.putInt(MINUTE, picker.getTime().getMinute());
        }
    }

    public static class TillDateDialog extends MainDialogFragment {
        private static final String YEAR = "year";
        private static final String MONTH = "month";
        private static final String DAY = "day";

        private MyDatePicker picker;

        public static NoticeDialogFragment newInstance() {
            return new TillDateDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            picker = getPicker(savedInstanceState);
            AlertDialog.Builder builder = getStyledBuilder(getContext());
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
            MyDatePicker myDatePicker = MyDatePicker.newInstance(getContext(), getViewModel().getTill());
            if (savedInstanceState != null) {
                int year = savedInstanceState.getInt(YEAR);
                int month = savedInstanceState.getInt(MONTH);
                int day = savedInstanceState.getInt(DAY);
                myDatePicker.updateDate(year, month, day);
            }
            return myDatePicker;
        }

        private void updateTill(Calendar date) {
            getViewModel().setTill(date);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt(YEAR, picker.getYear());
            outState.putInt(MONTH, picker.getMonth());
            outState.putInt(DAY, picker.getDayOfMonth());
        }
    }

    public static class WidgetInfoDialog extends MainDialogFragment {

        public static NoticeDialogFragment newInstance() {
            return new WidgetInfoDialog();
        }

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = getStyledBuilder(getContext());
            builder.setView(getDialogView());
            return builder.create();
        }

        private View getDialogView() {
            View view = getActivity().getLayoutInflater().inflate(R.layout.info_about_app_widget, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getViewModel().setAppWidgetInfoDisplayed(getContext());
                    getDialog().dismiss();
                }
            });
            return view;
        }
    }
}
