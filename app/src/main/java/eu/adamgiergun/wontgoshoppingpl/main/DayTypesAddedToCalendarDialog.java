package eu.adamgiergun.wontgoshoppingpl.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.day.Day;

import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.COMMERCE_SUNDAY;
import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.HOLIDAY;
import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.NON_COMMERCE_DAY;

public class DayTypesAddedToCalendarDialog extends MyDialogFragment {

    protected static NoticeDialogFragment newInstance() {
        return new DayTypesAddedToCalendarDialog();
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        final boolean[] checkedItems = getCheckedItems();
        AlertDialog dialog = getAlertDialog(checkedItems);
        dialog.show();
        enableDialogButtonPositive(dialog, checkedItems);
        return dialog;
    }

    private boolean[] getCheckedItems() {
        boolean[] checkedItems = new boolean[3];
        Day.Type.Set dayTypesToBeAddedToCalendar = getViewModel().getDayTypesToBeAddedToCalendar();
        checkedItems[NON_COMMERCE_DAY.getCode()] = dayTypesToBeAddedToCalendar.contains(NON_COMMERCE_DAY);
        checkedItems[HOLIDAY.getCode()] = dayTypesToBeAddedToCalendar.contains(HOLIDAY);
        checkedItems[COMMERCE_SUNDAY.getCode()] = dayTypesToBeAddedToCalendar.contains(COMMERCE_SUNDAY);
        return checkedItems;
    }

    private AlertDialog getAlertDialog(final boolean[] checkedItems) {
        CharSequence[] items = getItems();
        DialogInterface.OnMultiChoiceClickListener listener = getOnMultiChoiceClickListener(checkedItems);

        AlertDialog.Builder builder = getStyledBuilder(getContext());
        builder
                .setTitle(R.string.title_choose_events)
                .setMultiChoiceItems(items, checkedItems, listener)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getViewModel().setDayTypesToBeAddedToCalendar(getDayTypesSet(checkedItems));
                        DayTypesAddedToCalendarDialog.this.noticeDialogListener.onDialogPositiveClick(DayTypesAddedToCalendarDialog.this);
                    }
                })
                .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))

                .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this));
        return builder.create();
    }

    private CharSequence[] getItems() {
        return new CharSequence[] {
                getString(R.string.days_of_limited_commerce),
                getString(R.string.holidays),
                getString(R.string.commerce_sundays)
        };
    }

    private DialogInterface.OnMultiChoiceClickListener getOnMultiChoiceClickListener(final boolean[] checkedItems) {
        return new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                getViewModel().setDayTypesToBeAddedToCalendar(getDayTypesSet(checkedItems));
                enableDialogButtonPositive(getDialog(), checkedItems);
            }
        };
    }

    private void enableDialogButtonPositive(Dialog dialog, boolean[] checkedItems) {
        if (dialog instanceof AlertDialog) {
            Button button = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setEnabled(isAnyValueInBooleanArrayTrue(checkedItems));
        }
    }

    private boolean isAnyValueInBooleanArrayTrue(boolean[] array) {
        for (boolean value : array)
            if (value) return true;
        return false;
    }

    private Day.Type.Set getDayTypesSet(boolean[] checkedItems) {
        return getViewModel().newDayTypesSet(
                checkedItems[NON_COMMERCE_DAY.getCode()],
                checkedItems[HOLIDAY.getCode()],
                checkedItems[COMMERCE_SUNDAY.getCode()]);
    }
}
