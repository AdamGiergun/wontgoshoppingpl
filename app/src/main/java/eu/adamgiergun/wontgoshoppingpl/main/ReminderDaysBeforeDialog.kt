package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R

internal class ReminderDaysBeforeDialog : MyDialogFragment() {
    private lateinit var picker: NumberPicker

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        picker = getPicker(savedInstanceState)
        return getStyledBuilder().run {
            setTitle(R.string.days_before)
            setView(picker)
            setPositiveButton(android.R.string.ok) { _, _ -> mainViewModel.setReminderDaysBefore(picker.value) }
            setNegativeButton(android.R.string.cancel, null)
            create()
        }
    }

    private fun getPicker(savedInstanceState: Bundle?): NumberPicker {
        return if (savedInstanceState == null)
            MyNumberPicker.newInstance(requireContext(), mainViewModel.reminder.value!!.daysBefore)
        else
            MyNumberPicker.newInstance(requireContext(), savedInstanceState.getInt(VALUE))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(VALUE, picker.value)
    }

    private object MyNumberPicker {
        fun newInstance(context: Context, value: Int): NumberPicker {
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