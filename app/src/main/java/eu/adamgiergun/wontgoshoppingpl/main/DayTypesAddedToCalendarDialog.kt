package eu.adamgiergun.wontgoshoppingpl.main

import android.content.DialogInterface.OnMultiChoiceClickListener
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.wontgoshoppingpl.R

internal class DayTypesAddedToCalendarDialog : MainDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val checkedItems = mainViewModel.getDayTypesToBeAddedToCalendarAsArray()
        return getAlertDialog(checkedItems).apply {
            show()
            setIsEnabledDialogButtonPositive(this, checkedItems)
        }
    }

    private fun getAlertDialog(checkedItems: BooleanArray): AlertDialog {
        return getStyledBuilder().run {
            setTitle(R.string.title_choose_events)
            setMultiChoiceItems(items, checkedItems, getOnMultiChoiceClickListener(checkedItems))
            setPositiveButton(android.R.string.ok) { _, _ -> mainViewModel.askForReminderSettingsAndDateTill() }
            setNegativeButton(android.R.string.cancel, showInfoCancelled)
            create()
        }
    }

    private val items
        get() = arrayOf(
                getString(R.string.days_of_limited_commerce),
                getString(R.string.holidays),
                getString(R.string.commerce_sundays)
        )

    private fun getOnMultiChoiceClickListener(checkedItems: BooleanArray): OnMultiChoiceClickListener {
        return OnMultiChoiceClickListener { _, position: Int, checked: Boolean ->
            mainViewModel.updateDayTypesToBeAddedToCalendar(position, checked)
            setIsEnabledDialogButtonPositive(dialog as AlertDialog, checkedItems)
        }
    }

    private fun setIsEnabledDialogButtonPositive(alertDialog: AlertDialog, checkedItems: BooleanArray) {
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
            isEnabled = isAnyValueTrue(checkedItems)
        }
    }

    private fun isAnyValueTrue(array: BooleanArray): Boolean {
        array.forEach { if (it) return true }
        return false
    }
}