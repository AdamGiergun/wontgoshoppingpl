package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.main.MainViewModel;

public abstract class ThisAppDialogFragment extends NoticeDialogFragment {

    protected AlertDialog.Builder getStyledBuilder() {
        Context context = getContext();
        AppPreferences appPreferences = AppPreferences.getInstance(context);
        AlertDialog.Builder builder;
        if (appPreferences.isLookRich(context)) {
            builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        return builder;
    }

    public MainViewModel getMainViewModel() {
        return ViewModelProviders.of(getActivity()).get(MainViewModel.class);
    }
}
