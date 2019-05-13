package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;

public class ReminderTimeDialog extends MainDialogFragment {
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";

    private MyTimePicker picker;

    public static NoticeDialogFragment newInstance() {
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
}
