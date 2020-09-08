package eu.adamgiergun.wontgoshoppingpl.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import eu.adamgiergun.mylibrary.BasePreferenceFragmentCompat;
import eu.adamgiergun.wontgoshoppingpl.R;

public class LookPreferenceFragment extends BasePreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_look, rootKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.app_look_preference_key))) {
            SettingsViewModel settingsViewModel = getViewModel();
            if (settingsViewModel !=null)
                settingsViewModel.setLastFragmentIdHeaders();
            Intent intent = new Intent(getActivity(), eu.adamgiergun.wontgoshoppingpl.main.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private SettingsViewModel getViewModel() {
        FragmentActivity activity = getActivity();
        return (activity==null) ? null : new ViewModelProvider(activity).get(SettingsViewModel.class);
    }
}
