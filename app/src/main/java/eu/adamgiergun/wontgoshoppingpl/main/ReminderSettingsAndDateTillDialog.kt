package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.ReminderAndDateSettingsDialogViewBinding

internal class ReminderSettingsAndDateTillDialog : MyDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val binding = bind()
        return getStyledBuilder().run {
            setTitle(getString(R.string.title_reminder))
            setView(binding.root)
            setPositiveButton(android.R.string.ok, getPositiveButtonListener(this@ReminderSettingsAndDateTillDialog))
            setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this@ReminderSettingsAndDateTillDialog))
            create()
        }
    }

    private fun bind(): ReminderAndDateSettingsDialogViewBinding {
        val inflater = requireActivity().layoutInflater
        return ReminderAndDateSettingsDialogViewBinding.inflate(inflater).apply {
            lifecycleOwner = this@ReminderSettingsAndDateTillDialog
            viewModel = mainViewModel
            numberOfDaysBeforeView.setOnClickListener(
                    getViewListener(ReminderDaysBeforeDialog()))
            timeView.setOnClickListener(
                    getViewListener(ReminderTimeDialog()))
            tillDateView.setOnClickListener(
                    getViewListener(TillDateDialog()))
        }
    }
}