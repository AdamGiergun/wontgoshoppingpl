package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.ads.*;
import java.util.Calendar;

import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.wontgoshoppingpl.common.*;
import eu.adamgiergun.wontgoshoppingpl.day.*;
import eu.adamgiergun.wontgoshoppingpl.db.DatabaseHandler;
import eu.adamgiergun.wontgoshoppingpl.R;

import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.CalendarFullText.*;
import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.LookFullText.*;
import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.Type.APP_THEME;
import static eu.adamgiergun.wontgoshoppingpl.common.FirebaseEvent.Type.CALENDAR_EVENT;

public class MainViewModel extends AppViewModel {
    private boolean actionAddList;
    private MutableLiveData<Calendar> till;
    private MutableLiveData<SimpleReminder> reminder;
    private SQLiteDatabase database;
    private Day lastTouchedDay;
    private Day.Type.List dayTypesToBeAddedToCalendar;
    private Day.Type.List dayTypesToBeDisplayed;
    private InterstitialAd interstitialAd;
    private boolean recreatedOnDemand;

    @Override
    public void initialize(Context context) {
        super.initialize(context);
        setLocalizedConst(context);
        actionAddList = false;
        database = new DatabaseHandler(context).getWritableDatabase();
        recreatedOnDemand = false;
        dayTypesToBeDisplayed = appPreferences.newDayTypesList(context);
        dayTypesToBeAddedToCalendar = dayTypesToBeDisplayed;
        initializeInterstitialAd(context);
    }

    private void setLocalizedConst(Context context) {
        if (!LocalizedConst.isSet()) {
            LocalizedConst.setWholeDay(context.getString(R.string.whole_day));
            LocalizedConst.setSince(context.getString(R.string.since));
            LocalizedConst.setAskForLook(context.getString(R.string.ask_for_look));
            LocalizedConst.setSet();
        }
    }

    private void initializeInterstitialAd(Context context) {
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(context.getString(R.string.interstitial_ad_id));
        loadInterstitialAd();
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    void setReminderDialogArgs(Context context) {
        reminder = new MutableLiveData<>();
        reminder.setValue(getReminderPreference(context));
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
        SimpleReminder simpleReminder = reminder.getValue();
        if (simpleReminder == null) {
            return 1;
        } else {
            return reminder.getValue().getDaysBefore();
        }
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

    String getDayDescription() {
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

    void setAppWidgetInfoDisplayed(Context context) {
        AppPreferences appPreferences = AppPreferences.getInstance(context);
        appPreferences.setAppWidgetInfoDisplayed(context);
    }

    void closeDatabase() {
        if (database != null)
            database.close();
    }

    InterstitialAd getInterstitialAd() {
        if (interstitialAd.isLoaded()) {
            return interstitialAd;
        } else {
            if (!interstitialAd.isLoading()) {
                loadInterstitialAd();
            }
            return null;
        }
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

    boolean addToCalendar(Context context) {
        boolean result;
        if (actionAddList) {
            result = ListOfDays.addToCalendar(
                    context,
                    dayTypesToBeAddedToCalendar,
                    reminder.getValue(),
                    till.getValue());
        } else {
            lastTouchedDay.setReminder(reminder.getValue());
            result = lastTouchedDay.addToGoogleCalendar(context);
        }

        logCalendarEvent(context, result);

        return result;
    }

    Day.Type.List getDayTypesToBeAddedToCalendar() {
        return dayTypesToBeAddedToCalendar;
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

    void onInitialLookChoice(Context context, boolean isLookRich) {
        setLookRich(context, isLookRich);
        setNotFirstRun(context);
        recreatedOnDemand = true;
    }

    //region AppPreferences proxy region
    private SimpleReminder getReminderPreference(Context context) {
        return appPreferences.getReminder(context, SimpleReminder.METHOD_ALERT);
    }

    boolean isFirstRun(Context context) {
        return appPreferences.isFirstRun(context);
    }

    boolean wasAppWidgetInfoDisplayed(Context context) {
        return appPreferences.wasAppWidgetInfoDisplayed(context);
    }

    boolean isCalendarEnabledAndChosen(Context context) {
        return  appPreferences.isUseGoogleCalendarSet(context) && appPreferences.isCalendarSet(context);
    }

    boolean checkIfDayTypesChanged(Context context) {
        Day.Type.List oldDayTypesToBeDisplayed = dayTypesToBeDisplayed;
        dayTypesToBeDisplayed = appPreferences.newDayTypesList(context);
        return !(isFirstRun(context) ||
                dayTypesToBeDisplayed.equals(oldDayTypesToBeDisplayed));
    }

    private void setNotFirstRun(Context context) {
        appPreferences.setFirstRun(context, false);
    }
    //endregion

    void logLook(Context context) {
        String tag = isLookRich(context) ? RICH : MINI;
        logFirebaseEvent(context, APP_THEME, tag);
    }

    private void logCalendarEvent(Context context, boolean result) {
        String tag = actionAddList ? LIST : SINGLE;
        tag = result ? tag : tag + FAILED;
        logFirebaseEvent(context, CALENDAR_EVENT, tag);
    }

    private void logFirebaseEvent(Context context, String event, String fullText) {
        FirebaseEvent.log(context, event, fullText);
    }

    int getError() {
        return actionAddList ? R.string.error_sth_went_wrong : R.string.error_adding_to_calendar_failed;
    }
}
