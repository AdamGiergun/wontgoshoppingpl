package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.wontgoshoppingpl.R

internal class ReminderDaysBeforeDialog : MyDialogFragment() {

    private lateinit var picker : NumberPicker

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
        return if (savedInstanceState == null) {
            val daysBefore = mainViewModel.reminder.value?.daysBefore ?: 1
            MyNumberPicker.newInstance(requireContext(), daysBefore)
        } else
            MyNumberPicker.newInstance(requireContext(), savedInstanceState.getInt(VALUE))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(VALUE, picker.value)
    }

    private object MyNumberPicker {
        fun newInstance(context: Context, newValue: Int): NumberPicker {
            return NumberPicker(context).apply {
                isSaveFromParentEnabled = false
                isSaveEnabled = false
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