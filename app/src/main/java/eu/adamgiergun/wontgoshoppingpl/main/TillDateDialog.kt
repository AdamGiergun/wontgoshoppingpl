package eu.adamgiergun.wontgoshoppingpl.main

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import eu.adamgiergun.wontgoshoppingpl.R

internal class TillDateDialog : MyDialogFragment(), OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var year = 2100
        var month = 0
        var day = 1

        if (savedInstanceState == null) {
            mainViewModel.tillDate.value?.let { date ->
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

        return if (mainViewModel.isLookRich) {
            DatePickerDialog(
                    requireContext(),
                    R.style.MyAlertDialogStyle,
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
            datePicker.maxDate = mainViewModel.lastDateInMillis
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mainViewModel.setTillDate(year, month + 1, dayOfMonth)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val dialog = dialog as DatePickerDialog?
        dialog?.datePicker?.let { picker ->
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