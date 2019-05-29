package eu.adamgiergun.wontgoshoppingpl.settings;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import eu.adamgiergun.wontgoshoppingpl.R;

public class HeadersPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_headers);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        highlightLeftTreePreference(preference.getOrder());
        return super.onPreferenceTreeClick(preference);
    }

    private void highlightLeftTreePreference(int position) {
        RecyclerView listView = getListView();
        for (int i = 0; i < listView.getChildCount(); i++) {
            listView.getChildAt(i).setBackgroundColor(
                    i == position ? Color.LTGRAY : Color.TRANSPARENT);
        }
    }

    private ViewModel getViewModel() {
        FragmentActivity activity = getActivity();
        return (activity==null) ? null : ViewModelProviders.of(activity).get(ViewModel.class);
    }
}
