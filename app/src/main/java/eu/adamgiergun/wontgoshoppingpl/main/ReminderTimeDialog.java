package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;

public class ReminderTimeDialog extends MainDialogFragment {
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";

    private MyTimePicker picker;

    private ReminderTimeDialog() {}

    protected static NoticeDialogFragment newInstance() {
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

    private static class MyTimePicker extends TimePicker {

        private MyTimePicker(Context context) {
            super(context);
        }

        private static MyTimePicker newInstance(Context context, SimpleTime time) {
            MyTimePicker picker = new MyTimePicker(context);
            picker.initialize(time);
            return picker;
        }

        private void initialize(SimpleTime time) {
            setIs24HourView(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setHour(time.getHour());
                setMinute(time.getMinute());
            } else {
                setCurrentHour(time.getHour());
                setCurrentMinute(time.getMinute());
            }
        }

        private SimpleTime getTime() {
            SimpleTime time;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                time = new SimpleTime(getHour(), getMinute());
            } else {
                time = new SimpleTime(getCurrentHour(), getCurrentMinute());
            }
            return time;
        }
    }
}
