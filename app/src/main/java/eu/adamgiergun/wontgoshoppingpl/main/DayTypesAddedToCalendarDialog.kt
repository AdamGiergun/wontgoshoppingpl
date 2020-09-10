package eu.adamgiergun.wontgoshoppingpl.main

import android.app.Dialog
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day

internal class DayTypesAddedToCalendarDialog : MyDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val checkedItems = checkedItems
        val dialog = getAlertDialog(checkedItems)
        dialog.show()
        enableDialogButtonPositive(dialog, checkedItems)
        return dialog
    }

    private val checkedItems: BooleanArray
        get() {
            val checkedItems = BooleanArray(3)
            val dayTypesToBeAddedToCalendar = mainViewModel.dayTypesToBeAddedToCalendar
            checkedItems[Day.Type.NON_COMMERCE_DAY.code] = dayTypesToBeAddedToCalendar!!.contains(Day.Type.NON_COMMERCE_DAY)
            checkedItems[Day.Type.HOLIDAY.code] = dayTypesToBeAddedToCalendar.contains(Day.Type.HOLIDAY)
            checkedItems[Day.Type.COMMERCE_SUNDAY.code] = dayTypesToBeAddedToCalendar.contains(Day.Type.COMMERCE_SUNDAY)
            return checkedItems
        }

    private fun getAlertDialog(checkedItems: BooleanArray): AlertDialog {
        val items = items
        val listener = getOnMultiChoiceClickListener(checkedItems)
        val builder = getStyledBuilder()
        builder
                .setTitle(R.string.title_choose_events)
                .setMultiChoiceItems(items, checkedItems, listener)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    mainViewModel.dayTypesToBeAddedToCalendar = getDayTypesSet(checkedItems)
                    noticeDialogListener.onDialogPositiveClick(this@DayTypesAddedToCalendarDialog)
                }
                .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))
                .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this))
        return builder.create()
    }

    private val items: Array<CharSequence>
        get() = arrayOf(
                getString(R.string.days_of_limited_commerce),
                getString(R.string.holidays),
                getString(R.string.commerce_sundays)
        )

    private fun getOnMultiChoiceClickListener(checkedItems: BooleanArray): OnMultiChoiceClickListener {
        return OnMultiChoiceClickListener { _, _, _ ->
            mainViewModel.dayTypesToBeAddedToCalendar = getDayTypesSet(checkedItems)
            enableDialogButtonPositive(dialog, checkedItems)
        }
    }

    private fun enableDialogButtonPositive(dialog: Dialog?, checkedItems: BooleanArray) {
        if (dialog is AlertDialog) {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.isEnabled = isAnyValueInBooleanArrayTrue(checkedItems)
        }
    }

    private fun isAnyValueInBooleanArrayTrue(array: BooleanArray): Boolean {
        for (value in array) if (value) return true
        return false
    }

    private fun getDayTypesSet(checkedItems: BooleanArray): Day.Type.Set {
        return mainViewModel.newDayTypesSet(
                checkedItems[Day.Type.NON_COMMERCE_DAY.code],
                checkedItems[Day.Type.HOLIDAY.code],
                checkedItems[Day.Type.COMMERCE_SUNDAY.code])
    }

    companion object {
        internal fun newInstance(): NoticeDialogFragment {
            return DayTypesAddedToCalendarDialog()
        }
    }
}