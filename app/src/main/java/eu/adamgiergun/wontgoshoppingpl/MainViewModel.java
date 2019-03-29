package eu.adamgiergun.wontgoshoppingpl;

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

//    public SimpleReminder getReminder() {
//        return reminder;
//    }

    String getDayDescription() {
        return dayDescription;
    }

    public MutableLiveData<Calendar> getTill() {
        return till;
    }

    public void setTill(Calendar till) {
        this.till.setValue(till);
    }
}
