package eu.adamgiergun.wontgoshoppingpl.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.TimeZone;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;
import eu.adamgiergun.wontgoshoppingpl.common.*;
import eu.adamgiergun.wontgoshoppingpl.day.*;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.settings.SettingsFragment;

public class ViewModel extends AppViewModel {
    private boolean actionAddList;
    private MutableLiveData<SimpleDate> till;
    private MutableLiveData<SimpleReminder> reminder;
    private Day lastTouchedDay;
    private Day.Type.Set dayTypesToBeAddedToCalendar;
    private Day.Type.Set dayTypesToBeDisplayed;
    private boolean recreatedOnDemand;

    public ViewModel(@NonNull Application application) {
        super(application);
    }

    void initialize() {
        actionAddList = false;
        recreatedOnDemand = false;
        dayTypesToBeDisplayed = AppPreferences.newDayTypeSet(getAppContext());
        dayTypesToBeAddedToCalendar = dayTypesToBeDisplayed;
    }

    void setReminderDialogArgs() {
        reminder = new MutableLiveData<>();
        reminder.setValue(getReminderPreference());
        if (isActionAddList()) {
            till = new MutableLiveData<>();
            Calendar today = Calendar.getInstance();
            till.setValue(SimpleDate.getLastDayOfMonth(today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 2));
        }
    }

    SimpleReminder getReminder() {
        return reminder.getValue();
    }

    MutableLiveData<SimpleReminder> getReminderObservable() {
        return reminder;
    }

    int getReminderDaysBefore() {
        return (reminder.getValue() == null)
                ? 1
                : reminder.getValue().getDaysBefore();
    }

    void setReminderDaysBefore(int daysBefore) {
        SimpleReminder simpleReminder = reminder.getValue();
        if (simpleReminder != null) {
            simpleReminder.setDaysBefore(daysBefore);
            reminder.setValue(simpleReminder);
        }
    }

    void setReminderTime(SimpleTime time) {
        SimpleReminder simpleReminder = reminder.getValue();
        if (simpleReminder != null) {
            simpleReminder.setTime(time);
            reminder.setValue(simpleReminder);
        }
    }

    String getDayDescriptionForDialog() {
        return lastTouchedDay.getDescriptionForDialog();
    }

    MutableLiveData<SimpleDate> getTillObservable() {
        return till;
    }

    SimpleDate getTill() {
        return till.getValue();
    }

    void setTill(SimpleDate till) {
        this.till.setValue(till);
    }

    void setAppWidgetInfoDisplayed() {
        AppPreferences.setAppWidgetInfoDisplayed(getAppContext());
    }

    void setLastTouchedDay(Day day) {
        lastTouchedDay = day;
        setActionAddList(false);
    }

    void setActionAddList(boolean actionAddList) {
        this.actionAddList = actionAddList;
    }

    boolean isActionAddList() {
        return actionAddList;
    }

    boolean addToCalendar() {
        boolean result;
        if (actionAddList) {
            result = Day.List.addToCalendar(
                    getAppContext(),
                    getDayTypesToBeAddedToCalendar(),
                    reminder.getValue(),
                    getTimeZoneId(),
                    till.getValue());
        } else {
            lastTouchedDay.setReminder(reminder.getValue());
            result = lastTouchedDay.addToGoogleCalendar(getAppContext(), getTimeZoneId());
        }

        logCalendarEvent(result);

        return result;
    }

    private String getTimeZoneId() {
        return AppPreferences.getUsePolishTimeZone(getAppContext()) ?
                "Europe/Warsaw":
                TimeZone.getDefault().getID();
    }

    Day.Type.Set getDayTypesToBeAddedToCalendar() {
        return dayTypesToBeAddedToCalendar==null ?
                AppPreferences.newDayTypeSet(getAppContext())
                : dayTypesToBeAddedToCalendar;
    }

    Day.Type.Set newDayTypesSet(boolean addNonCommerceDays, boolean addHolidays, boolean addCommerceSundays) {
        return Day.Type.newSet(addNonCommerceDays, addHolidays, addCommerceSundays);
    }

    long getLastDateInMillis() {
        return Day.List.getLastDateInMillis(getAppContext());
    }

    void setDayTypesToBeAddedToCalendar(Day.Type.Set dayTypes) {
        dayTypesToBeAddedToCalendar = dayTypes;
    }

    boolean isRecreatedOnDemand() {
        if (recreatedOnDemand) {
            recreatedOnDemand = false;
            return true;
        } else {
            return false;
        }
    }

    void onInitialLookChoice(boolean isLookRich) {
        setLookRich(isLookRich);
        setNotFirstRun();
        recreatedOnDemand = true;
    }

    //region AppPreferences proxy region
    private SimpleReminder getReminderPreference() {
        return AppPreferences.getReminder(getAppContext(), SimpleReminder.METHOD_ALERT, getTimeZoneId());
    }

    boolean isFirstRun() {
        return AppPreferences.isFirstRun(getAppContext());
    }

    boolean wasAppWidgetInfoDisplayed() {
        return AppPreferences.wasAppWidgetInfoDisplayed(getAppContext());
    }

    boolean isCalendarEnabledAndChosen() {
        return  AppPreferences.isUseGoogleCalendarSet(getAppContext()) && AppPreferences.isCalendarSet(getAppContext());
    }

    boolean checkIfDayTypesChanged() {
        Day.Type.Set oldDayTypesToBeDisplayed = dayTypesToBeDisplayed;
        dayTypesToBeDisplayed = AppPreferences.newDayTypeSet(getAppContext());
        boolean typesChanged = !dayTypesToBeDisplayed.equals(oldDayTypesToBeDisplayed);
        if (typesChanged) dayTypesToBeAddedToCalendar = dayTypesToBeDisplayed;
        return !isFirstRun() && typesChanged;
    }

    private void setNotFirstRun() {
        AppPreferences.setFirstRun(getAppContext(), false);
    }
    //endregion

    void logLook() {
        FirebaseEvent.logAppTheme(getAppContext(), isLookRich());
    }

    private void logCalendarEvent(boolean result) {
        FirebaseEvent.logCalendarEvent(getAppContext(), actionAddList, result);
    }

    int getError() {
        return actionAddList ? R.string.error_sth_went_wrong : R.string.error_adding_to_calendar_failed;
    }

    static String getStartFragmentId(boolean startFromNotifications) {
        return SettingsFragment.getStartFragmentId(startFromNotifications);
    }
}
