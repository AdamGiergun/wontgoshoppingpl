package eu.adamgiergun.wontgoshoppingpl.main;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import eu.adamgiergun.wontgoshoppingpl.common.AppDialogFragment;

class MainDialogFragment extends AppDialogFragment {

    MainViewModel getViewModel() {
        FragmentActivity activity = getActivity();
        return (activity==null) ? null : ViewModelProviders.of(activity).get(MainViewModel.class);
    }
}
