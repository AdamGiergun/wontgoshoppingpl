package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import eu.adamgiergun.mylibrary.NoticeDialogFragment;
import eu.adamgiergun.wontgoshoppingpl.R;

class MyDialogFragment extends NoticeDialogFragment {

    AlertDialog.Builder getStyledBuilder(Context context) {
        return  getViewModel().isLookRich()
                ? new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                : new AlertDialog.Builder(context);
    }

    ViewModel getViewModel() {
        FragmentActivity activity = getActivity();
        return (activity==null) ? null : new ViewModelProvider(activity).get(ViewModel.class);
    }
}
