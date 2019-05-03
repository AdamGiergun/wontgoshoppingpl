package eu.adamgiergun.wontgoshoppingpl.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.TimeZone;

import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.wontgoshoppingpl.common.*;
import eu.adamgiergun.wontgoshoppingpl.day.*;
import eu.adamgiergun.wontgoshoppingpl.R;

import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.CalendarFullText.*;
import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.LookFullText.*;
import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.Type.APP_THEME;
import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.Type.CALENDAR_EVENT;

public class MainViewModel extends AppViewModel {
    private boolean actionAddList;
    private MutableLiveData<Calendar> till;
    private MutableLiveData<SimpleReminder> reminder;
    private Day lastTouchedDay;
    private Day.Type.List dayTypesToBeAddedToCalendar;
    private Day.Type.List dayTypesToBeDisplayed;
    private boolean recreatedOnDemand;

    public MainViewModel(@NonNull Application application) {
        super(application);
    }

    void initialize() {
        actionAddList = false;
        recreatedOnDemand = false;
        dayTypesToBeDisplayed = getAppPreferences().newDayTypesList(getApplication());
        dayTypesToBeAddedToCalendar = dayTypesToBeDisplayed;
    }

    void setReminderDialogArgs() {
        reminder = new MutableLiveData<>();
        reminder.setValue(getReminderPreference());
        if (isActionAddList()) {
            till = new MutableLiveData<>();
            till.setValue(getLastDayOfNextMonth());
        }
    }

    private Calendar getLastDayOfNextMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar;
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

    MutableLiveData<Calendar> getTillObservable() {
        return till;
    }

    Calendar getTill() {
        return till.getValue();
    }

    void setTill(Calendar till) {
        this.till.setValue(till);
    }

    void setAppWidgetInfoDisplayed() {
        AppPreferences appPreferences = AppPreferences.getInstance(getApplication());
        appPreferences.setAppWidgetInfoDisplayed(getApplication());
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
                    getApplication(),
                    dayTypesToBeAddedToCalendar,
                    reminder.getValue(),
                    getTimeZoneId(),
                    till.getValue());
        } else {
            lastTouchedDay.setReminder(reminder.getValue());
            result = lastTouchedDay.addToGoogleCalendar(getApplication(), getTimeZoneId());
        }

        logCalendarEvent(result);

        return result;
    }

    private String getTimeZoneId() {
        return getAppPreferences().getUsePolishTimeZone(getApplication()) ?
                "Europe/Warsaw":
                TimeZone.getDefault().getID();
    }

    Day.Type.List getDayTypesToBeAddedToCalendar() {
        return dayTypesToBeAddedToCalendar==null ?
                Day.Type.newList(true, true, true)
                : dayTypesToBeAddedToCalendar;
    }

    Day.Type.List newDayTypeList(boolean addNonCommerceDays, boolean addHolidays, boolean addCommerceSundays) {
        return Day.Type.newList(addNonCommerceDays, addHolidays, addCommerceSundays);
    }

    long getLastDateInMillis() {
        return Day.List.getLastDateInMillis(getApplication());
    }

    void setDayTypesToBeAddedToCalendar(Day.Type.List dayTypes) {
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
        return getAppPreferences().getReminder(getApplication(), SimpleReminder.METHOD_ALERT, getTimeZoneId());
    }

    boolean isFirstRun() {
        return getAppPreferences().isFirstRun(getApplication());
    }

    boolean wasAppWidgetInfoDisplayed() {
        return getAppPreferences().wasAppWidgetInfoDisplayed(getApplication());
    }

    boolean isCalendarEnabledAndChosen() {
        return  getAppPreferences().isUseGoogleCalendarSet(getApplication()) && getAppPreferences().isCalendarSet(getApplication());
    }

    boolean checkIfDayTypesChanged() {
        Day.Type.List oldDayTypesToBeDisplayed = dayTypesToBeDisplayed;
        dayTypesToBeDisplayed = getAppPreferences().newDayTypesList(getApplication());
        return !(isFirstRun() ||
                dayTypesToBeDisplayed.equals(oldDayTypesToBeDisplayed));
    }

    private void setNotFirstRun() {
        getAppPreferences().setFirstRun(getApplication(), false);
    }
    //endregion

    void logLook() {
        String tag = isLookRich() ? RICH : MINI;
        logFirebaseEvent(APP_THEME, tag);
    }

    private void logCalendarEvent(boolean result) {
        String tag = actionAddList ? LIST : SINGLE;
        tag = result ? tag : tag + FAILED;
        logFirebaseEvent(CALENDAR_EVENT, tag);
    }

    private void logFirebaseEvent(String event, String fullText) {
        FirebaseEvent.log(getApplication(), event, fullText);
    }

    int getError() {
        return actionAddList ? R.string.error_sth_went_wrong : R.string.error_adding_to_calendar_failed;
    }
}
