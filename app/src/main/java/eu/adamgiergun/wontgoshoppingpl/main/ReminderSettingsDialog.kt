package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.ReminderSettingsDialogViewBinding

internal class ReminderSettingsDialog : MyDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val binding = bind()
        val title = getString(R.string.title_reminder_of) + " " + mainViewModel.dayDescriptionForDialog
        return getStyledBuilder().run {
            setTitle(title)
            setView(binding.root)
            setPositiveButton(android.R.string.ok, getPositiveButtonListener(this@ReminderSettingsDialog))
            setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this@ReminderSettingsDialog))
            create()
        }
    }

    private fun bind(): ReminderSettingsDialogViewBinding {
        val inflater = requireActivity().layoutInflater
        return ReminderSettingsDialogViewBinding.inflate(inflater).apply {
            lifecycleOwner = this@ReminderSettingsDialog
            viewModel = mainViewModel
            numberOfDaysBeforeView.setOnClickListener(
                    getViewListener(ReminderDaysBeforeDialog.newInstance()))
            timeView.setOnClickListener(
                    getViewListener(ReminderTimeDialog.newInstance()))
        }
    }

    companion object {
        internal fun newInstance(): NoticeDialogFragment {
            return ReminderSettingsDialog()
        }
    }
}