package eu.adamgiergun.wontgoshoppingpl.main;

import android.app.TimePickerDialog;
import android.widget.TimePicker;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;
import eu.adamgiergun.wontgoshoppingpl.R;

class NewReminderTimeDialog {

    static TimePickerDialog newInstance(final FragmentActivity fragmentActivity) {
        final ViewModel viewModel = getViewModel(fragmentActivity);
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay,
                                  int minute) {

                viewModel.setReminderTime(new SimpleTime(hourOfDay, minute));
            }
        };
        SimpleTime time = viewModel.getReminder().getTime();
        return viewModel.isLookRich() ?
                new TimePickerDialog(
                        fragmentActivity,
                        R.style.MyAlertDialogStyle,
                        listener,
                        time.getHour(),
                        time.getMinute(),
                        true) :
                new TimePickerDialog(
                        fragmentActivity,
                        listener,
                        time.getHour(),
                        time.getMinute(),
                        true);
    }

    static private ViewModel getViewModel(FragmentActivity fragmentActivity) {
        return ViewModelProviders.of(fragmentActivity).get(ViewModel.class);
    }
}
