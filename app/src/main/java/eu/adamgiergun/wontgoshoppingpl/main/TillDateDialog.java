package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate;

public class TillDateDialog extends MyDialogFragment {
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private MyDatePicker picker;

    protected static NoticeDialogFragment newInstance() {
        return new TillDateDialog();
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
                        updateTill(picker.getDate());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private MyDatePicker getPicker(Bundle savedInstanceState) {
        ViewModel viewModel = getViewModel();
        MyDatePicker myDatePicker =
                MyDatePicker.newInstance(
                        getContext(),
                        viewModel.getTill(),
                        viewModel.getLastDateInMillis());
        if (savedInstanceState != null) {
            int year = savedInstanceState.getInt(YEAR);
            int month = savedInstanceState.getInt(MONTH);
            int day = savedInstanceState.getInt(DAY);
            myDatePicker.updateDate(year, month, day);
        }
        return myDatePicker;
    }

    private void updateTill(SimpleDate date) {
        getViewModel().setTill(date);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(YEAR, picker.getYear());
        outState.putInt(MONTH, picker.getMonth());
        outState.putInt(DAY, picker.getDayOfMonth());
    }

    private static class MyDatePicker extends DatePicker {

        private MyDatePicker(Context context) {
            super(context);
        }

        private static MyDatePicker newInstance(Context context, SimpleDate date, long maxDate) {
            MyDatePicker picker = new MyDatePicker(context);
            picker.setMaxDate(maxDate);
            picker.updateDate(date.getYear(), date.getMonth() - 1, date.getDay());
            return picker;
        }

        private SimpleDate getDate() {
            return SimpleDate.newInstance(getYear(), getMonth()+1, getDayOfMonth());
        }
    }
}
