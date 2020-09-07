package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatTextView
import eu.adamgiergun.mylibrary.NoticeDialogFragment
import eu.adamgiergun.wontgoshoppingpl.R

class ReminderSettingsDialog : MyDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): AppCompatDialog {
        val builder = getStyledBuilder()
        val viewModel = viewModel!!
        val title = if (viewModel.isActionAddList) getString(R.string.title_reminder) else getString(R.string.title_reminder_of) + " " + viewModel.dayDescriptionForDialog
        builder
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))
                .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this))
        return builder.create()
    }

    private val dialogView: View
        get() {
            val inflater = requireActivity().layoutInflater
            val viewModel = viewModel!!
            val viewLayoutId = if (viewModel.isActionAddList) R.layout.reminder_and_date_settings_dialog_view else R.layout.reminder_settings_dialog_view
            val view = inflater.inflate(viewLayoutId, null)
            initializeReminderViews(view, viewModel)
            if (viewModel.isActionAddList) {
                initializeTillView(view, viewModel)
            }
            return view
        }

    private fun initializeReminderViews(view: View, viewModel: ViewModel) {
        val daysView = setupView(
                getDaysView(view), viewModel.reminder.daysBefore.toString(),
                ReminderDaysBeforeDialog.newInstance())
        val timeView = setupView(
                getTimeView(view),
                viewModel.reminder.time.toString(),
                ReminderTimeDialog.newInstance())
        viewModel.reminderObservable.observe(this, { reminder ->
            daysView.text = reminder.daysBefore.toString()
            timeView.text = reminder.time.toString()
        })
    }

    private fun setupView(view: AppCompatTextView, text: String, dialogFragment: NoticeDialogFragment): AppCompatTextView {
        view.text = text
        view.setOnClickListener(
                getViewListener(dialogFragment))
        return view
    }

    private fun initializeTillView(view: View, viewModel: ViewModel) {
        val tillView = setupView(
                getTillView(view),
                viewModel.till.toString(),
                TillDateDialog.newInstance())
        viewModel.tillObservable.observe(this, { till -> tillView.text = till.toString() })
    }

    private fun getDaysView(view: View): AppCompatTextView {
        return view.findViewById(R.id.numberOfDaysBeforeView)
    }

    private fun getTimeView(view: View): AppCompatTextView {
        return view.findViewById(R.id.timeView)
    }

    private fun getTillView(view: View): AppCompatTextView {
        return view.findViewById(R.id.tillDateView)
    }

    companion object {
        fun newInstance(): NoticeDialogFragment {
            return ReminderSettingsDialog()
        }
    }
}