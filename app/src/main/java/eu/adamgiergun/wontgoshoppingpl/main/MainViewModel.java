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
import eu.adamgiergun.wontgoshoppingpl.AppPreferences;
import eu.adamgiergun.wontgoshoppingpl.db.DatabaseHandler;
import eu.adamgiergun.wontgoshoppingpl.LimitedCommerceDay;
import eu.adamgiergun.wontgoshoppingpl.LimitedCommerceDayType;
import eu.adamgiergun.wontgoshoppingpl.R;

public class MainViewModel extends ViewModel {
    private boolean actionAddList;
    private String dayDescription;
    private MutableLiveData<Calendar> till;
    private MutableLiveData<SimpleReminder> reminder;
    private SQLiteDatabase database;
    private LimitedCommerceDay lastTouchedDay;
    private LimitedCommerceDayType.TypesList typesAddedToCalendar;
    private LimitedCommerceDayType.TypesList lastTypesToBeDisplayed;
    private AppPreferences appPreferences;
    private InterstitialAd interstitialAd;
    private boolean recreatedOnDemand;

    void initialize(Context context) {
        actionAddList = false;
        database = new DatabaseHandler(context).getWritableDatabase();
        recreatedOnDemand = false;
        appPreferences = new AppPreferences(context);
        lastTypesToBeDisplayed = appPreferences.getLimitedCommerceDayTypes(context);
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(context.getString(R.string.interstitial_ad_id));
        loadInterstitialAd();
    }

    void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    //TODO rethink parameters
    void putReminderDialogArgs(SimpleReminder reminder, String dayDescription) {
        this.reminder = new MutableLiveData<>();
        this.reminder.setValue(reminder);
        this.dayDescription = dayDescription;
    }

    void putReminderDialogArgs(SimpleReminder reminder, Calendar till) {
        this.reminder = new MutableLiveData<>();
        this.reminder.setValue(reminder);
        this.till = new MutableLiveData<>();
        this.till.setValue(till);
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
        return dayDescription;
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

    AppPreferences getAppPreferences() {
        return appPreferences;
    }

    void closeDatabase() {
        if (database != null)
            database.close();
    }

    LimitedCommerceDayType.TypesList getLastTypesToBeDisplayed() {
        return lastTypesToBeDisplayed;
    }

    void setLastTypesToBeDisplayed(LimitedCommerceDayType.TypesList typesList) {
        lastTypesToBeDisplayed = typesList;
    }

    InterstitialAd getInterstitialAd() {
        return interstitialAd;
    }

    LimitedCommerceDay getLastTouchedDay() {
        return lastTouchedDay;
    }

    void setLastTouchedDay(LimitedCommerceDay lastTouchedDay) {
        this.lastTouchedDay = lastTouchedDay;
    }

    void setActionAddList(boolean actionAddList) {
        this.actionAddList = actionAddList;
    }

    boolean isActionAddList() {
        return actionAddList;
    }

    LimitedCommerceDayType.TypesList getTypesAddedToCalendar() {
        return typesAddedToCalendar;
    }

    void setTypesAddedToCalendar(LimitedCommerceDayType.TypesList typesAddedToCalendar) {
        this.typesAddedToCalendar = typesAddedToCalendar;
    }

    void setRecreatedOnDemand(boolean recreatedOnDemand) {
        this.recreatedOnDemand = recreatedOnDemand;
    }

    boolean isRecreatedOnDemand() {
        return recreatedOnDemand;
    }
}
