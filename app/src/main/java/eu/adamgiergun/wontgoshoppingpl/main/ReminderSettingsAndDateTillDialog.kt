package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.ReminderAndDateSettingsDialogViewBinding

internal class ReminderSettingsAndDateTillDialog : MyDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val builder = getStyledBuilder()
        val title = getString(R.string.title_reminder)
        builder.setTitle(title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))
                .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this))
        return builder.create()
    }

    private val dialogView: View
        get() {
            val inflater = requireActivity().layoutInflater
            val binding = ReminderAndDateSettingsDialogViewBinding.inflate(inflater)
            binding.lifecycleOwner = this
            binding.viewModel = mainViewModel
            binding.numberOfDaysBeforeView.setOnClickListener(
                    getViewListener(ReminderDaysBeforeDialog.newInstance()))
            binding.timeView.setOnClickListener(
                    getViewListener(ReminderTimeDialog.newInstance()))
            binding.tillDateView.setOnClickListener(
                    getViewListener(TillDateDialog.newInstance()))
            return binding.root
        }


    companion object {
        internal fun newInstance(): NoticeDialogFragment {
            return ReminderSettingsAndDateTillDialog()
        }
    }
}