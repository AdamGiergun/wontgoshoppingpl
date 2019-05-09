package eu.adamgiergun.wontgoshoppingpl.common;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.wontgoshoppingpl.R;

public abstract class AppDialogFragment extends NoticeDialogFragment {

    protected AlertDialog.Builder getStyledBuilder(Context context) {
        return  AppPreferences.isLookRich(context.getApplicationContext())
                ? new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                : new AlertDialog.Builder(context);
    }
}
