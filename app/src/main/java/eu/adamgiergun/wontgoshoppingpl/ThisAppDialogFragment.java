package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;

abstract class ThisAppDialogFragment extends NoticeDialogFragment {

    AlertDialog.Builder getStyledBuilder() {
        Context context = getContext();
        AppPreferences appPreferences = new AppPreferences(context);
        AlertDialog.Builder builder;
        if (appPreferences.isLookRich(context)) {
            builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        return builder;
    }

    MainViewModel getViewModel() {
        return ViewModelProviders.of(getActivity()).get(MainViewModel.class);
    }
}
