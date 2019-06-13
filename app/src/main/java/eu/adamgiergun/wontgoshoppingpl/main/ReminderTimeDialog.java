package eu.adamgiergun.wontgoshoppingpl.main;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;
import eu.adamgiergun.wontgoshoppingpl.R;

public class ReminderTimeDialog extends MyDialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    protected static NoticeDialogFragment newInstance() {
        return new ReminderTimeDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = 0, minute = 0;
        if (savedInstanceState == null) {
            SimpleTime time = getViewModel().getReminder().getTime();
            hour = time.getHour();
            minute = time.getMinute();
        }
        return getViewModel().isLookRich() ?
                new TimePickerDialog(
                        getContext(),
                        R.style.MyAlertDialogStyle,
                        this,
                        hour,
                        minute,
                        true) :
                new TimePickerDialog(
                        getContext(),
                        this,
                        hour,
                        minute,
                        true);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        SimpleTime time = new SimpleTime(hour, minute);
        getViewModel().setReminderTime(time);
    }
}
