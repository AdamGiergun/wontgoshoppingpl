package eu.adamgiergun.niekupiszpl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.*;
import android.support.v7.widget.AppCompatTextView;
import android.view.*;
import android.widget.*;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.permissionsmanagement.PermissionType;

public class MainActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener, DayFragment.OnListFragmentInteractionListener {

    private boolean isCalendarPermissionRequestedBySettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                startSettingsActivityIfCalendarPermissionGranted();
                return true;
            case R.id.calendar:
                addEverythingToCalendar();
                return true;
            case R.id.help:
                startHelpActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSettingsActivityIfCalendarPermissionGranted() {
        if (checkCalendarPermission()) {
            startSettingsActivity();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isCalendarPermissionRequestedBySettings = true;
                requestWriteCalendarPermission();
            }
        }
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(
                getBaseContext(),
                SettingsActivity.class);
        startActivity(intent);
    }

    private void startHelpActivity() {

    }

    private void addEverythingToCalendar() {
        boolean useGoogleCalendar = SettingsActivity.useGoogleCalendar(getBaseContext());
        if (useGoogleCalendar) {
            askForKindsOfEventsAddedToCalendar();
        } else {
            startSettingsActivity();
        }
    }

    private void askForKindsOfEventsAddedToCalendar() {
        AppCompatDialogFragment dialogFragment = new AskForKindsOfEventsToAddToCalendar();
        dialogFragment.show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onListFragmentInteraction(CommerceFreeDay day) {
        boolean useGoogleCalendar = SettingsActivity.useGoogleCalendar(getBaseContext());
        if (useGoogleCalendar) {
            if (day.addToGoogleCalendar())
                showInfo(R.string.info_added_to_calendar, false);
            else
                showInfo(R.string.error_adding_to_calendar_failed, false);
        } else {
            startSettingsActivity();
        }
    }

    private void showInfo(int info, boolean length_long) {
        int length;
        if (length_long) {
            length = Toast.LENGTH_LONG;
        } else {
            length = Toast.LENGTH_SHORT;
        }

        Toast.makeText(
                getBaseContext(),
                info,
                length)
                .show();
    }

    private boolean checkCalendarPermission() {
        isCalendarPermissionRequestedBySettings = false;
        return !isSystemAtLeastMarshmallow() || isWriteCalendarGranted();
    }

    private boolean isSystemAtLeastMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    private boolean isWriteCalendarGranted() {
        int permissionCheck = ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_CALENDAR);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestWriteCalendarPermission() {
        String[] listOfRequestedPermissions = new String[]{Manifest.permission.WRITE_CALENDAR};
        int permissionTypeCode = PermissionType.CALENDAR.getCode();
        requestPermissions(listOfRequestedPermissions, permissionTypeCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionType.CALENDAR.getCode()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isCalendarPermissionRequestedBySettings) {
                    startSettingsActivity();
                } else {
                    //TODO what to do when touching event caused request
                }
            } else {
                showInfo(eu.adamgiergun.mylibrary.R.string.info_explain_calendar_permission, true);
            }
        }
    }

    @Override
    public void onDialogPositiveClick(AppCompatDialogFragment dialog) {
        if (dialog instanceof AskForKindsOfEventsToAddToCalendar) {
            AppCompatDialogFragment dialogFragment = new AskForReminderSettings();
            dialogFragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onDialogNegativeClick(AppCompatDialogFragment dialog) {
        showInfo(R.string.info_cancelled, false);
    }

    public static class AskForKindsOfEventsToAddToCalendar extends NoticeDialogFragment {
        private Button dialogButtonPositive;

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            CharSequence[] items = new CharSequence[]
                    {getString(R.string.days_of_limited_commerce),
                    getString(R.string.holidays)};

            final boolean[] checkedItems = new boolean[2];
            checkedItems[0] = true;
            checkedItems[1] = SettingsActivity.showHolidays(getContext());

            DialogInterface.OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    checkedItems[which] = isChecked;
                    dialogButtonPositive.setEnabled(isAnyValueInBooleanArrayTrue(checkedItems));
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.title_choose_events)
                    .setMultiChoiceItems(items, checkedItems, listener)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
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
    }

    public static class AskForReminderSettings extends NoticeDialogFragment {

        @NonNull
        @Override
        public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder
                    .setTitle(R.string.title_reminder)
                    .setView(getDialogView())
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
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
            int daysValue = SettingsActivity.getReminderDaysFromPreferences(getContext());
            daysView.setText(String.valueOf(daysValue));

            AppCompatTextView timeView = view.findViewById(R.id.timeView);
            String timeValue = SettingsActivity.getReminderTime(getContext());
            timeView.setText(timeValue);
            return view;
        }
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
    /*

    public void askForAddingToGoogleCalendar(View view) {
        calendarEvent = getCalendarEvent();

        if (calendarEvent != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.ask_if_repeated)
                    .setView(setupRepetitionOptionsView())
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            addToGoogleCalendar(calendarEvent);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            calendarEvent.setNoRepetition();
                            addToGoogleCalendar(calendarEvent);
                        }
                    })
                    .setNeutralButton(android.R.string.cancel, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            buttonPositiveInRepetitionDialog = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            buttonPositiveInRepetitionDialog.setEnabled(false);
        }
    }

    public SimpleCalendarEvent getCalendarEvent() {
        String calendarId = preferences.getString(getString(R.string.default_calendar_key), getString(eu.adamgiergun.mylibrary.R.string.id_for_default_calendar_not_set));
        if (calendarId.equals(getString(eu.adamgiergun.mylibrary.R.string.id_for_default_calendar_not_set))) {
            Toast.makeText(
                    getBaseContext(),
                    getString(R.string.error_default_calendar_not_selected),
                    Toast.LENGTH_SHORT)
                    .show();
            return null;
        } else {
            SimpleCalendarEvent calendarEvent = new SimpleCalendarEvent(calendarId);

            calendarEvent.setTitle(getString(R.string.investment));
            Calendar expiryDate = getToday();
            switch (Investment.getTimeUnit()) {
                case DAY:
                    expiryDate.add(Calendar.DATE, Investment.getInterval().intValue());
                    break;
                case WEEK:
                    expiryDate.add(Calendar.DATE, 7 * Investment.getInterval().intValue());
                    break;
                case MONTH:
                    expiryDate.add(Calendar.MONTH, Investment.getInterval().intValue());
                    break;
                case YEAR:
                    expiryDate.add(Calendar.YEAR, Investment.getInterval().intValue());
                    break;
                default:
                    break;
            }
            int startHour = 23;
            int startMinute = 0;
            calendarEvent.setStartCalendar(expiryDate, startHour, startMinute);

            String reminderTime = preferences.getString(getString(R.string.default_reminder_time_key), getString(R.string.default_reminder_time_value));
            int reminderInMinutes = getTimeDifferenceInMinutes(
                    TimePreference.getHour(reminderTime),
                    TimePreference.getMinute(reminderTime),
                    startHour,
                    startMinute);
            calendarEvent.addReminder(
                    new SimpleCalendarEventReminder(
                            CalendarContract.Reminders.METHOD_ALERT,
                            SimpleCalendarEventReminder.Measure.MINUTE,
                            reminderInMinutes
                    )
            );

            return calendarEvent;
        }
    }

    private void addToGoogleCalendar(SimpleCalendarEvent calendarEvent) {
        if (calendarEvent == null || calendarEvent.addCalendarEventToCalendar(getBaseContext()) == SimpleCalendarEvent.NON_EXISTING)
            Toast.makeText(
                    getBaseContext(),
                    getString(R.string.error_not_added_to_calendar),
                    Toast.LENGTH_SHORT)
                    .show();
        else
            Toast.makeText(
                    getBaseContext(),
                    getString(R.string.info_added_to_calendar),
                    Toast.LENGTH_SHORT)
                    .show();
    }
    */
}
