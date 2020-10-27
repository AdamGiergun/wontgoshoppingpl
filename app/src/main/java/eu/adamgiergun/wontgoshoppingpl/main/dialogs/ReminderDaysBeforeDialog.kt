package eu.adamgiergun.wontgoshoppingpl.main.dialogs

import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.wontgoshoppingpl.R

internal class ReminderDaysBeforeDialog : MainDialogFragment() {

    private lateinit var picker : NumberPicker

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        picker = getPicker(savedInstanceState)
        return getStyledBuilder().run {
            setTitle(R.string.days_before)
            setView(picker)
            setPositiveButton(android.R.string.ok) { _, _ -> mViewModel.setReminderDaysBefore(picker.value) }
            setNegativeButton(android.R.string.cancel, null)
            create()
        }
    }

    private fun getPicker(savedInstanceState: Bundle?): NumberPicker {
        return if (savedInstanceState == null) {
            val reminderDaysBefore = mViewModel.reminderDaysBefore.value
                    ?: context?.resources?.getInteger(R.integer.reminder_days_before_default_value)
                    ?: 1
            MyNumberPicker.newInstance(requireContext(), reminderDaysBefore)
        } else
            MyNumberPicker.newInstance(requireContext(), savedInstanceState.getInt(VALUE))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(VALUE, picker.value)
        super.onSaveInstanceState(outState)
    }

    private object MyNumberPicker {
        fun newInstance(context: Context, newValue: Int): NumberPicker {
            return NumberPicker(context).apply {
                isSaveFromParentEnabled = false
                isSaveEnabled = false
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                minValue = 0
                maxValue = 9
                value = newValue
            }
        }
    }

    companion object {
        private const val VALUE = "value"
    }
}