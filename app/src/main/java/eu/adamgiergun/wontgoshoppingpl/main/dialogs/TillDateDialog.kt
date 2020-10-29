package eu.adamgiergun.wontgoshoppingpl.main.dialogs

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import eu.adamgiergun.wontgoshoppingpl.R

internal class TillDateDialog : MainDialogFragment(), OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var year = 2100
        var month = 0
        var day = 1

        if (savedInstanceState == null) {
            mViewModel.tillDate.value?.let { date ->
                year = date.year
                month = date.month - 1
                day = date.day
            }
        } else {
            savedInstanceState.run {
                year = getInt(YEAR)
                month = getInt(MONTH)
                day = getInt(DAY)
            }
        }

        return if (mViewModel.isLookRich) {
            DatePickerDialog(
                    requireContext(),
                    R.style.RichAlertDialogStyle,
                    this,
                    year,
                    month,
                    day)
        } else {
            DatePickerDialog(
                    requireContext(),
                    this,
                    year,
                    month,
                    day)
        }.apply {
            datePicker.maxDate = mViewModel.lastDateInMillis
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mViewModel.setTillDate(year, month + 1, dayOfMonth)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val datePickerDialog = dialog as DatePickerDialog?
        datePickerDialog?.datePicker?.let { picker ->
            outState.putInt(YEAR, picker.year)
            outState.putInt(MONTH, picker.month)
            outState.putInt(DAY, picker.dayOfMonth)
        }
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val YEAR = "year"
        private const val MONTH = "month"
        private const val DAY = "day"
    }
}