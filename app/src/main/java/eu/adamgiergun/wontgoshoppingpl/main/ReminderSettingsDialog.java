package eu.adamgiergun.wontgoshoppingpl.main;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.Observer;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;
import eu.adamgiergun.wontgoshoppingpl.R;

public class ReminderSettingsDialog extends MyDialogFragment {

    public static NoticeDialogFragment newInstance() {
        return new ReminderSettingsDialog();
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getStyledBuilder(getContext());
        ViewModel viewModel = getViewModel();
        String title = viewModel.isActionAddList() ? getString(R.string.title_reminder) : getString(R.string.title_reminder_of) + " " + viewModel.getDayDescriptionForDialog();
        builder
                .setTitle(title)
                .setView(getDialogView())
                .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))
                .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this));
        return builder.create();
    }

    private View getDialogView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewModel viewModel = getViewModel();
        int viewLayoutId = viewModel.isActionAddList() ? R.layout.reminder_and_date_settings_dialog_view : R.layout.reminder_settings_dialog_view;
        View view = inflater.inflate(viewLayoutId, null);

        initializeReminderViews(view, viewModel);
        if (viewModel.isActionAddList()) {
            initializeTillView(view, viewModel);
        }

        return view;
    }

    private void initializeReminderViews(final View view, final ViewModel viewModel) {
        final AppCompatTextView daysView = setupView(
                getDaysView(view),
                String.valueOf(viewModel.getReminder().getDaysBefore()),
                ReminderDaysBeforeDialog.newInstance());
        final AppCompatTextView timeView = setupView(
                getTimeView(view),
                viewModel.getReminder().getTime().toString(),
                ReminderTimeDialog.newInstance());

        viewModel.getReminderObservable().observe(this, new Observer<SimpleReminder>() {
            @Override
            public void onChanged(SimpleReminder reminder) {
                daysView.setText(
                        String.valueOf(reminder.getDaysBefore()));
                timeView.setText(
                        reminder.getTime().toString());
            }
        });
    }

    private AppCompatTextView setupView(AppCompatTextView view, String text, NoticeDialogFragment dialogFragment) {
        view.setText(text);
        view.setOnClickListener(
                getViewListener(dialogFragment));
        return view;
    }

    private void initializeTillView(final View view, ViewModel viewModel) {
        final AppCompatTextView tillView = setupView(
                getTillView(view),
                viewModel.getTill().toString(),
                TillDateDialog.newInstance());

        viewModel.getTillObservable().observe(this, new Observer<SimpleDate>() {
            @Override
            public void onChanged(SimpleDate till) {
                tillView.setText(till.toString());
            }
        });
    }

    private AppCompatTextView getDaysView(View view) {
        return view.findViewById(R.id.numberOfDaysBeforeView);
    }

    private AppCompatTextView getTimeView(View view) {
        return view.findViewById(R.id.timeView);
    }

    private AppCompatTextView getTillView(View view) {
        return view.findViewById(R.id.tillDateView);
    }
}
