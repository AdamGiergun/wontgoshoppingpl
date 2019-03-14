package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import eu.adamgiergun.mylibrary.NoticeDialogFragment;

abstract class ThisAppDialogFragment extends NoticeDialogFragment {

    AlertDialog.Builder getStyledBuilder() {
        Context context = getContext();
        AppPreferences appPreferences = new AppPreferences(context);
        AlertDialog.Builder builder;
        if (appPreferences.isLookRich(getContext())) {
            builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        return builder;
    }
}
