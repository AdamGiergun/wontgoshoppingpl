package eu.adamgiergun.wontgoshoppingpl.main

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.widget.TimePicker
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime
import eu.adamgiergun.wontgoshoppingpl.R

class ReminderTimeDialog : MyDialogFragment(), OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var hour = 0
        var minute = 0
        if (savedInstanceState == null) {
            val time = viewModel!!.reminder.time
            hour = time.hour
            minute = time.minute
        }
        return if (viewModel!!.isLookRich) TimePickerDialog(
                context,
                R.style.MyAlertDialogStyle,
                this,
                hour,
                minute,
                true) else TimePickerDialog(
                context,
                this,
                hour,
                minute,
                true)
    }

    override fun onTimeSet(timePicker: TimePicker, hour: Int, minute: Int) {
        val time = SimpleTime(hour, minute)
        viewModel!!.setReminderTime(time)
    }

    companion object {
        fun newInstance(): NoticeDialogFragment {
            return ReminderTimeDialog()
        }
    }
}