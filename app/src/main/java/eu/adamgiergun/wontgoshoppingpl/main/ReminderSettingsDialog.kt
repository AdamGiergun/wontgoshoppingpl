package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatTextView
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R

internal class ReminderSettingsDialog : MyDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val builder = getStyledBuilder()
        val viewModel = mainViewModel
        val title = getString(R.string.title_reminder_of) + " " + viewModel.dayDescriptionForDialog
        builder.setTitle(title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))
                .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this))
        return builder.create()
    }

    private val dialogView: View
        get() {
            val inflater = requireActivity().layoutInflater
            val viewModel = mainViewModel
            val viewLayoutId = R.layout.reminder_settings_dialog_view
            val view = inflater.inflate(viewLayoutId, null)
            initializeReminderViews(view, viewModel)
            return view
        }

    private fun initializeReminderViews(view: View, mainViewModel: MainViewModel) {
        val daysView = setupView(
                getDaysView(view), mainViewModel.getReminder()!!.daysBefore.toString(),
                ReminderDaysBeforeDialog.newInstance())
        val timeView = setupView(
                getTimeView(view),
                mainViewModel.getReminder()!!.time.toString(),
                ReminderTimeDialog.newInstance())
        mainViewModel.reminderObservable!!.observe(this, { reminder ->
            daysView.text = reminder!!.daysBefore.toString()
            timeView.text = reminder.time.toString()
        })
    }

    private fun setupView(view: AppCompatTextView, text: String, dialogFragment: NoticeDialogFragment): AppCompatTextView {
        view.text = text
        view.setOnClickListener(
                getViewListener(dialogFragment))
        return view
    }

    private fun getDaysView(view: View): AppCompatTextView {
        return view.findViewById(R.id.numberOfDaysBeforeView)
    }

    private fun getTimeView(view: View): AppCompatTextView {
        return view.findViewById(R.id.timeView)
    }

    companion object {
        internal fun newInstance(): NoticeDialogFragment {
            return ReminderSettingsDialog()
        }
    }
}