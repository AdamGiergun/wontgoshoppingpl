package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.wontgoshoppingpl.R;

public class ReminderDaysBeforeDialog extends MyDialogFragment {
    private static final String VALUE = "value";

    private NumberPicker picker;

    protected static NoticeDialogFragment newInstance() {
        return new ReminderDaysBeforeDialog();
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        picker = getPicker(savedInstanceState);
        AlertDialog.Builder builder = getStyledBuilder(getContext());
        builder
                .setTitle(R.string.days_before)
                .setView(picker)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateReminder(picker.getValue());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private NumberPicker getPicker(Bundle savedInstanceState) {
        return savedInstanceState == null ?
                MyNumberPicker.newInstance(getContext(), getViewModel().getReminderDaysBefore()) :
                MyNumberPicker.newInstance(getContext(), savedInstanceState.getInt(VALUE));
    }

    private void updateReminder(int daysBefore) {
        getViewModel().setReminderDaysBefore(daysBefore);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(VALUE, picker.getValue());
    }

    private static class MyNumberPicker {

        private MyNumberPicker() {}

        private static NumberPicker newInstance(Context context, int value) {
            NumberPicker picker = new NumberPicker(context);
            picker.setSaveFromParentEnabled(false);
            picker.setSaveEnabled(false);
            picker.setMinValue(0);
            picker.setMaxValue(9);
            picker.setValue(value);
            return picker;
        }
    }
}
