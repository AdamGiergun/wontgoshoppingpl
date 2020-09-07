package eu.adamgiergun.wontgoshoppingpl.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.wontgoshoppingpl.R;

public class WidgetInfoDialog extends MyDialogFragment {

    public static NoticeDialogFragment newInstance() {
        return new WidgetInfoDialog();
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getStyledBuilder();
        builder.setView(getDialogView());
        return builder.create();
    }

    private View getDialogView() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.info_about_app_widget, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getViewModel().setAppWidgetInfoDisplayed();
                if (getDialog() != null)
                    getDialog().dismiss();
            }
        });
        return view;
    }
}
