package eu.adamgiergun.wontgoshoppingpl.main

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.widget.TimePicker
import eu.adamgiergun.wontgoshoppingpl.R

internal class ReminderTimeDialog : MainDialogFragment(), OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var hour = 11
        var minute = 0
        if (savedInstanceState == null) {
            mainViewModel.reminderTime.value?.let { time ->
                hour = time.hour
                minute = time.minute
            }
        }

        return if (mainViewModel.isLookRich)
            TimePickerDialog(
                context,
                R.style.MyAlertDialogStyle,
                this,
                hour,
                minute,
                true)
        else
            TimePickerDialog(
                context,
                this,
                hour,
                minute,
                true)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        mainViewModel.setReminderTime(hourOfDay, minute)
    }
}