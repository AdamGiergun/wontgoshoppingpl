package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.wontgoshoppingpl.R;

public abstract class AppDialogFragment extends NoticeDialogFragment {

    protected AlertDialog.Builder getStyledBuilder(Context context) {
        AppPreferences appPreferences = AppPreferences.getInstance(context.getApplicationContext());
        AlertDialog.Builder builder;
        if (appPreferences.isLookRich(context.getApplicationContext())) {
            builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        return builder;
    }
}
