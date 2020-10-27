package eu.adamgiergun.wontgoshoppingpl.main.dialogs

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.ReminderAndDateSettingsDialogViewBinding
import eu.adamgiergun.wontgoshoppingpl.main.fragments.MainDialogFragment

internal class ReminderSettingsAndDateTillDialog : MainDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val binding = bind()
        return getStyledBuilder().run {
            setTitle(getString(R.string.title_reminder))
            setView(binding.root)
            setPositiveButton(android.R.string.ok) { _, _ -> mainViewModel.addToCalendar(true) }
            setNegativeButton(android.R.string.cancel, showInfoCancelled)
            mainViewModel.initializeReminder()
            create()
        }
    }

    private fun bind(): ReminderAndDateSettingsDialogViewBinding {
        val inflater = requireActivity().layoutInflater
        return ReminderAndDateSettingsDialogViewBinding.inflate(inflater).apply {
            lifecycleOwner = this@ReminderSettingsAndDateTillDialog
            viewModel = mainViewModel
        }
    }
}