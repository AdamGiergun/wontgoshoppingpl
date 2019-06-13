package eu.adamgiergun.wontgoshoppingpl.main;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate;
import eu.adamgiergun.wontgoshoppingpl.R;

public class TillDateDialog extends MyDialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    protected static NoticeDialogFragment newInstance() {
        return new TillDateDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, day;
        if (savedInstanceState == null) {
            SimpleDate date = getViewModel().getTill();
            year = date.getYear();
            month = date.getMonth() - 1;
            day = date.getDay();
        } else {
            year = savedInstanceState.getInt(YEAR);
            month = savedInstanceState.getInt(MONTH);
            day = savedInstanceState.getInt(DAY);
        }

        DatePickerDialog datePickerDialog = getViewModel().isLookRich() ?
                new DatePickerDialog(
                        getContext(),
                        R.style.MyAlertDialogStyle,
                        this,
                        year,
                        month,
                        day) :
                new DatePickerDialog(
                        getContext(),
                        this,
                        year,
                        month,
                        day);
        datePickerDialog.getDatePicker().setMaxDate(getViewModel().getLastDateInMillis());
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        SimpleDate date = SimpleDate.newInstance(year, month + 1, day);
        getViewModel().setTill(date);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        DatePickerDialog dialog = (DatePickerDialog) getDialog();
        if (dialog != null) {
            DatePicker picker = dialog.getDatePicker();
            outState.putInt(YEAR, picker.getYear());
            outState.putInt(MONTH, picker.getMonth());
            outState.putInt(DAY, picker.getDayOfMonth());
        }
    }
}
