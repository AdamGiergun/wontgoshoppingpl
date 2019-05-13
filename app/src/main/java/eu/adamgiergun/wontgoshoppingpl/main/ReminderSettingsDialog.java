package eu.adamgiergun.wontgoshoppingpl.main;

import android.annotation.SuppressLint;
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

public class ReminderSettingsDialog extends MainDialogFragment {

    public static NoticeDialogFragment newInstance() {
        return new ReminderSettingsDialog();
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getStyledBuilder(getContext());
        MainViewModel viewModel = getViewModel();
        String title = viewModel.isActionAddList() ? getString(R.string.title_reminder) : getString(R.string.title_reminder_of) + " " + viewModel.getDayDescriptionForDialog();
        builder
                .setTitle(title)
                .setView(getDialogView())
                .setPositiveButton(android.R.string.ok, getPositiveButtonListener(this))
                .setNegativeButton(android.R.string.cancel, getNegativeButtonListener(this));
        return builder.create();
    }

    @SuppressLint("InflateParams")
    private View getDialogView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        MainViewModel viewModel = getViewModel();
        int viewLayoutId = viewModel.isActionAddList() ? R.layout.reminder_and_date_settings_dialog_view : R.layout.reminder_settings_dialog_view;
        View view = inflater.inflate(viewLayoutId, null);

        initializeReminderViews(view, viewModel);
        if (viewModel.isActionAddList()) {
            initializeTillView(view, viewModel);
        }

        return view;
    }

    private void initializeTillView(View view, MainViewModel viewModel) {
        AppCompatTextView tillView = getTillView(view);
        tillView.setText(viewModel.getTill().toString());
        tillView.setOnClickListener(getViewListener(TillDateDialog.newInstance()));
    }

    private AppCompatTextView getTillView(View view) {
        return view.findViewById(R.id.tillDateView);
    }

    private void initializeReminderViews(final View view, final MainViewModel viewModel) {
        final AppCompatTextView daysView = getDaysView(view);
        daysView.setText(String.valueOf(viewModel.getReminder().getDaysBefore()));
        daysView.setOnClickListener(getViewListener(ReminderDaysBeforeDialog.newInstance()));

        final AppCompatTextView timeView = getTimeView(view);
        timeView.setText(viewModel.getReminder().getTime().toString());
        timeView.setOnClickListener(getViewListener(ReminderTimeDialog.newInstance()));

        Observer<SimpleReminder> reminderObserver = new Observer<SimpleReminder>() {
            @Override
            public void onChanged(SimpleReminder reminder) {
                daysView.setText(
                        String.valueOf(reminder.getDaysBefore()
                        ));
                timeView.setText(
                        reminder.getTime().toString());
            }
        };

        viewModel.getReminderObservable().observe(this, reminderObserver);

        if (viewModel.isActionAddList()) {
            Observer<SimpleDate> tillObserver = new Observer<SimpleDate>() {
                @Override
                public void onChanged(SimpleDate till) {
                    getTillView(view).setText(till.toString());
                }
            };

            viewModel.getTillObservable().observe(this, tillObserver);
        }
    }

    private AppCompatTextView getTimeView(View view) {
        return view.findViewById(R.id.timeView);
    }

    private AppCompatTextView getDaysView(View view) {
        return view.findViewById(R.id.numberOfDaysBeforeView);
    }
}
