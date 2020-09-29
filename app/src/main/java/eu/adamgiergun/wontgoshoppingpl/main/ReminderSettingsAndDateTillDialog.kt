package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import androidx.lifecycle.lifecycleScope
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.ReminderAndDateSettingsDialogViewBinding
import kotlinx.coroutines.launch

internal class ReminderSettingsAndDateTillDialog : MyDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val binding = bind()
        return getStyledBuilder().run {
            setTitle(getString(R.string.title_reminder))
            setView(binding.root)
            setPositiveButton(android.R.string.ok) { _, _ -> lifecycleScope.launch { mainViewModel.addToCalendar() } }
            setNegativeButton(android.R.string.cancel) { _, _ -> mainViewModel.showInfoCancelled() }
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