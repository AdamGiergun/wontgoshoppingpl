package eu.adamgiergun.wontgoshoppingpl.main

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.widget.TimePicker
import eu.adamgiergun.wontgoshoppingpl.R

internal class ReminderTimeDialog : MyDialogFragment(), OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var hour = 0
        var minute = 0
        if (savedInstanceState == null) {
            mainViewModel.reminder.value?.time?.run {
                hour = this.hour
                minute = this.minute
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

    override fun onTimeSet(timePicker: TimePicker, hour: Int, minute: Int) {
        mainViewModel.setReminderTime(hour, minute)
    }
}