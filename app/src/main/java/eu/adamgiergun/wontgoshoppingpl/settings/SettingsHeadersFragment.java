package eu.adamgiergun.wontgoshoppingpl.settings;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import eu.adamgiergun.wontgoshoppingpl.R;

public class SettingsHeadersFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_headers);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String title = preference.getTitle().toString();
        highlightLeftTreeOption(title.equals(getString(R.string.pref_header_look)));
        return super.onPreferenceTreeClick(preference);
    }

    private void highlightLeftTreeOption(boolean isLook) {
        SettingsViewModel viewModel = getViewModel();
        RecyclerView listView = getListView();
        if (viewModel != null && viewModel.isScreenWideEnough()) {
            if (isLook) {
                listView.getChildAt(0).setBackgroundColor(Color.LTGRAY);
                listView.getChildAt(1).setBackgroundColor(Color.TRANSPARENT);
            } else {
                listView.getChildAt(1).setBackgroundColor(Color.LTGRAY);
                listView.getChildAt(0).setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private SettingsViewModel getViewModel() {
        FragmentActivity activity = getActivity();
        return (activity==null) ? null : ViewModelProviders.of(activity).get(SettingsViewModel.class);
    }
}
