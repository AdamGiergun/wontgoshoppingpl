package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import androidx.lifecycle.lifecycleScope
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.ReminderSettingsDialogViewBinding
import kotlinx.coroutines.launch

internal class ReminderSettingsDialog : MyDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val binding = bind()
        val title = getString(R.string.title_reminder_of) + " " + mainViewModel.dayDescriptionForDialog
        return getStyledBuilder().run {
            setTitle(title)
            setView(binding.root)
            setPositiveButton(android.R.string.ok) { _, _ -> lifecycleScope.launch { mainViewModel.addToCalendar() } }
            setNegativeButton(android.R.string.cancel) { _, _ -> mainViewModel.showInfoCancelled() }
            create()
        }
    }

    private fun bind(): ReminderSettingsDialogViewBinding {
        val inflater = requireActivity().layoutInflater
        return ReminderSettingsDialogViewBinding.inflate(inflater).apply {
            lifecycleOwner = this@ReminderSettingsDialog
            viewModel = mainViewModel
            numberOfDaysBeforeView.setOnClickListener(
                    getViewListener(ReminderDaysBeforeDialog()))
            timeView.setOnClickListener(
                    getViewListener(ReminderTimeDialog()))
        }
    }
}