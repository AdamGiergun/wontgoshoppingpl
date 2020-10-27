package eu.adamgiergun.wontgoshoppingpl.main.dialogs

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.ReminderSettingsDialogViewBinding

internal class ReminderSettingsDialog : MainDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val binding = bind()
        val title = getString(R.string.title_reminder_of) + " " + mViewModel.dayDescriptionForDialog
        return getStyledBuilder().run {
            setTitle(title)
            setView(binding.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                if (binding.setAsDefaultCheckbox.isChecked)
                    mViewModel.saveCurrentReminderAsDefault()
                mViewModel.addToCalendar(false)
            }
            setNegativeButton(android.R.string.cancel, showInfoCancelled)
            mViewModel.initializeReminder()
            create()
        }
    }

    private fun bind(): ReminderSettingsDialogViewBinding {
        val inflater = requireActivity().layoutInflater
        return ReminderSettingsDialogViewBinding.inflate(inflater).apply {
            lifecycleOwner = this@ReminderSettingsDialog
            viewModel = mViewModel
        }
    }
}