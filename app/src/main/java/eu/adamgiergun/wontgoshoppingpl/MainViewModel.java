package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Calendar;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;

public class MainViewModel extends ViewModel {
    private MainActivity.Action action;
    private String dayDescription;
    private MutableLiveData<Calendar> till;
    private MutableLiveData<SimpleReminder> reminder;

    void putReminderDialogArgs(MainActivity.Action action, SimpleReminder reminder, String dayDescription) {
        this.action = action;
        this.reminder = new MutableLiveData<>();
        this.reminder.setValue(reminder);
        this.dayDescription = dayDescription;
    }

    void putReminderDialogArgs(MainActivity.Action action, SimpleReminder reminder, Calendar till) {
        this.action = action;
        this.reminder = new MutableLiveData<>();
        this.reminder.setValue(reminder);
        this.till = new MutableLiveData<>();
        this.till.setValue(till);
    }

    MutableLiveData<SimpleReminder> getReminder() {
        return reminder;
    }

    void setReminder(SimpleReminder reminder) {
        this.reminder.setValue(reminder);
    }

    MainActivity.Action getAction() {
        return action;
    }

    String getDayDescription() {
        return dayDescription;
    }

    MutableLiveData<Calendar> getTill() {
        return till;
    }

    void setTill(Calendar till) {
        this.till.setValue(till);
    }

    void setAppWidgetInfoDisplayed(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        appPreferences.setAppWidgetInfoDisplayed(context);
    }
}
