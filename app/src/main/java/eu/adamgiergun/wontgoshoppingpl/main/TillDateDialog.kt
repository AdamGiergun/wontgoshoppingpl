package eu.adamgiergun.wontgoshoppingpl.main

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R

internal class TillDateDialog : MyDialogFragment(), OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val year: Int
        val month: Int
        val day: Int
        if (savedInstanceState == null) {
            val date = mainViewModel!!.getTill()
            year = date!!.year
            month = date.month - 1
            day = date.day
        } else {
            year = savedInstanceState.getInt(YEAR)
            month = savedInstanceState.getInt(MONTH)
            day = savedInstanceState.getInt(DAY)
        }
        val datePickerDialog = if (mainViewModel!!.isLookRich) DatePickerDialog(
                requireContext(),
                R.style.MyAlertDialogStyle,
                this,
                year,
                month,
                day) else DatePickerDialog(
                requireContext(),
                this,
                year,
                month,
                day)
        datePickerDialog.datePicker.maxDate = mainViewModel!!.lastDateInMillis
        return datePickerDialog
    }

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, day: Int) {
        mainViewModel!!.setTill(year, month + 1, day)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val dialog = dialog as DatePickerDialog?
        if (dialog != null) {
            val picker = dialog.datePicker
            outState.putInt(YEAR, picker.year)
            outState.putInt(MONTH, picker.month)
            outState.putInt(DAY, picker.dayOfMonth)
        }
    }

    companion object {
        private const val YEAR = "year"
        private const val MONTH = "month"
        private const val DAY = "day"
        internal fun newInstance(): NoticeDialogFragment {
            return TillDateDialog()
        }
    }
}