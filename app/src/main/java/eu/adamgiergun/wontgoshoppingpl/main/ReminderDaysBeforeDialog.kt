package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R

internal class ReminderDaysBeforeDialog : MyDialogFragment() {
    private var picker: NumberPicker? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        picker = getPicker(savedInstanceState)
        val builder = getStyledBuilder()
        builder
                .setTitle(R.string.days_before)
                .setView(picker)
                .setPositiveButton(android.R.string.ok) { dialog, id -> updateReminder(picker!!.value) }
                .setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }

    private fun getPicker(savedInstanceState: Bundle?): NumberPicker {
        return if (savedInstanceState == null) MyNumberPicker.newInstance(context, mainViewModel!!.reminderDaysBefore) else MyNumberPicker.newInstance(context, savedInstanceState.getInt(VALUE))
    }

    private fun updateReminder(daysBefore: Int) {
        mainViewModel!!.reminderDaysBefore = daysBefore
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(VALUE, picker!!.value)
    }

    private object MyNumberPicker {
        fun newInstance(context: Context?, value: Int): NumberPicker {
            val picker = NumberPicker(context)
            picker.isSaveFromParentEnabled = false
            picker.isSaveEnabled = false
            picker.minValue = 0
            picker.maxValue = 9
            picker.value = value
            return picker
        }
    }

    companion object {
        private const val VALUE = "value"
        internal fun newInstance(): NoticeDialogFragment {
            return ReminderDaysBeforeDialog()
        }
    }
}