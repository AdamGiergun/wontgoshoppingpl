package eu.adamgiergun.wontgoshoppingpl.main.dialogs

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.ReminderAndDateSettingsDialogViewBinding

internal class ReminderSettingsAndDateTillDialog : MainDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val binding = bind()
        return getStyledBuilder().run {
            setTitle(getString(R.string.title_reminder))
            setView(binding.root)
            setPositiveButton(android.R.string.ok) {
                    _, _ -> mViewModel.addToCalendar(true, requireActivity())
            }
            setNegativeButton(android.R.string.cancel, showInfoCancelled)
            mViewModel.initializeReminder()
            create()
        }
    }

    private fun bind(): ReminderAndDateSettingsDialogViewBinding {
        val inflater = requireActivity().layoutInflater
        return ReminderAndDateSettingsDialogViewBinding.inflate(inflater).apply {
            val showDialogOnClick = View.OnClickListener { mViewModel.setIdOfDialogToShow(it.id) }
            numberOfDaysBeforeView.setOnClickListener(showDialogOnClick)
            timeView.setOnClickListener(showDialogOnClick)
            tillDateView.setOnClickListener(showDialogOnClick)
            mViewModel.reminderDaysBefore.observe(this@ReminderSettingsAndDateTillDialog) {
                numberOfDaysBeforeView.text = it.toString()
            }
            mViewModel.reminderTime.observe(this@ReminderSettingsAndDateTillDialog) {
                timeView.text = it.toString()
            }
            mViewModel.tillDate.observe(this@ReminderSettingsAndDateTillDialog) {
                tillDateView.text = it.toString()
            }
        }
    }
}