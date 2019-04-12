package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.Calendar;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences;
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst;
import eu.adamgiergun.wontgoshoppingpl.days.ListOfLimitedCommerceDays;
import eu.adamgiergun.wontgoshoppingpl.db.DatabaseHandler;
import eu.adamgiergun.wontgoshoppingpl.days.LimitedCommerceDay;
import eu.adamgiergun.wontgoshoppingpl.days.LimitedCommerceDayType;
import eu.adamgiergun.wontgoshoppingpl.R;

public class MainViewModel extends ViewModel {
    private boolean actionAddList;
    private MutableLiveData<Calendar> till;
    private MutableLiveData<SimpleReminder> reminder;
    private SQLiteDatabase database;
    private LimitedCommerceDay lastTouchedDay;
    private LimitedCommerceDayType.TypesList typesToBeAddedToCalendar;
    private LimitedCommerceDayType.TypesList typesToBeDisplayed;
    private AppPreferences appPreferences;
    private InterstitialAd interstitialAd;
    private boolean recreatedOnDemand;

    void initialize(Context context) {
        setLocalizedConst(context);
        actionAddList = false;
        database = new DatabaseHandler(context).getWritableDatabase();
        recreatedOnDemand = false;
        appPreferences = new AppPreferences(context);
        typesToBeDisplayed = appPreferences.getLimitedCommerceDayTypes(context);
        typesToBeAddedToCalendar = typesToBeDisplayed;
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
        AppPreferences appPreferences = new AppPreferences(context);
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

    void setLastTouchedDay(LimitedCommerceDay day) {
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
        if (actionAddList) {
            return ListOfLimitedCommerceDays.addToCalendar(
                    context,
                    typesToBeAddedToCalendar,
                    reminder.getValue(),
                    till.getValue());
        } else {
            lastTouchedDay.setReminder(reminder.getValue());
            return lastTouchedDay.addToGoogleCalendar(context);
        }
    }

    LimitedCommerceDayType.TypesList getTypesToBeAddedToCalendar() {
        return typesToBeAddedToCalendar;
    }

    void setTypesToBeAddedToCalendar(LimitedCommerceDayType.TypesList typesToBeAddedToCalendar) {
        this.typesToBeAddedToCalendar = typesToBeAddedToCalendar;
    }

    void setRecreatedOnDemand() {
        this.recreatedOnDemand = true;
    }

    boolean isRecreatedOnDemand() {
        if (recreatedOnDemand) {
            recreatedOnDemand = false;
            return true;
        } else {
            return false;
        }
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

    boolean isLookRich(Context context) {
        return appPreferences.isLookRich(context);
    }

    boolean isCalendarEnabledAndChosen(Context context) {
        return  appPreferences.isUseGoogleCalendarSet(context) && appPreferences.isCalendarSet(context);
    }

    boolean checkIfTypesChanged(Context context) {
        LimitedCommerceDayType.TypesList oldTypesToBeDisplayed = typesToBeDisplayed;
        typesToBeDisplayed = appPreferences.getLimitedCommerceDayTypes(context);
        return !(isFirstRun(context) ||
                typesToBeDisplayed.equals(oldTypesToBeDisplayed));
    }

    void setLookRich(Context context, boolean isLookRich) {
        appPreferences.setLookRich(context, isLookRich);
    }

    void setNotFirstRun(Context context) {
        appPreferences.setFirstRun(context, false);
    }
    //endregion
}
