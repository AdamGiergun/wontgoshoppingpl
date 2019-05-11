package eu.adamgiergun.wontgoshoppingpl.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import eu.adamgiergun.wontgoshoppingpl.R;

public class SettingsHeadersFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_headers);
    }

//    private void highlightMenuOption(RecyclerView listView) {
//        SettingsViewModel viewModel = getViewModel();
//        if (viewModel != null && !viewModel.isScreenNotWideEnough()){
//            if(viewModel.isLastFragmentLook()) {
//                listView.getChildAt(0).setBackgroundColor(Color.LTGRAY);
//                listView.getChildAt(1).setBackgroundColor(Color.TRANSPARENT);
//            } else {
//                listView.getChildAt(1).setBackgroundColor(Color.LTGRAY);
//                listView.getChildAt(0).setBackgroundColor(Color.TRANSPARENT);
//            }
//        }
//    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String title = preference.getTitle().toString();
        SettingsViewModel viewModel = getViewModel();
        if (viewModel != null && !viewModel.isScreenNotWideEnough()) {
            if (title.equals(getString(R.string.pref_header_look))) {
                getListView().getChildAt(0).setBackgroundColor(Color.LTGRAY);
                getListView().getChildAt(1).setBackgroundColor(Color.TRANSPARENT);
            } else {
                getListView().getChildAt(1).setBackgroundColor(Color.LTGRAY);
                getListView().getChildAt(0).setBackgroundColor(Color.TRANSPARENT);
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    private SettingsViewModel getViewModel() {
        FragmentActivity activity = getActivity();
        return (activity==null) ? null : ViewModelProviders.of(activity).get(SettingsViewModel.class);
    }
}
