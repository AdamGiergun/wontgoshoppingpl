package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.ReminderAndDateSettingsDialogViewBinding

internal class ReminderSettingsAndDateTillDialog : MainDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val binding = bind()
        return getStyledBuilder().run {
            setTitle(getString(R.string.title_reminder))
            setView(binding.root)
            setPositiveButton(android.R.string.ok, addToCalendar)
            setNegativeButton(android.R.string.cancel, showInfoCancelled)
            create()
        }
    }

    private fun bind(): ReminderAndDateSettingsDialogViewBinding {
        val inflater = requireActivity().layoutInflater
        return ReminderAndDateSettingsDialogViewBinding.inflate(inflater).apply {
            lifecycleOwner = this@ReminderSettingsAndDateTillDialog
            viewModel = mainViewModel
            numberOfDaysBeforeView.setOnClickListener(
                    getOnClickListener(ReminderDaysBeforeDialog()))
            timeView.setOnClickListener(
                    getOnClickListener(ReminderTimeDialog()))
            tillDateView.setOnClickListener(
                    getOnClickListener(TillDateDialog()))
        }
    }
}