package eu.adamgiergun.wontgoshoppingpl;

import android.content.*;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.preference.*;

import eu.adamgiergun.mylibrary.*;
import eu.adamgiergun.mylibrary.calendarmanagement.*;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        AppPreferences appPreferences = new AppPreferences(this);
        if (appPreferences.isLookRich(this)) {
            theme.applyStyle(R.style.AppThemeCustomizationsRich, true);
        } else {
            theme.applyStyle(R.style.AppThemeCustomizationsMini, true);
        }
        return theme;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.settings_activity);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    public static class SettingsFragment extends BasePreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
            setSummaries();
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (key.equals(getString(R.string.default_calendar_preference_key))) {
                Preference connectionPref = findPreference(key);
                if (connectionPref instanceof CalendarsListPreferenceNew) {
                    CharSequence calendarId = sharedPreferences.getString(key, "0");
                    //getString(R.string.calendar_not_set));
                    CharSequence summary = ((CalendarsListPreferenceNew) connectionPref).getCalendarName(calendarId);
                    connectionPref.setSummary(summary);
                }
            } else if (key.equals(getString(R.string.app_look_preference_key))) {
                getActivity().recreate();
            }
        }

        private void setSummaries() {
            IntegerDialogPreferenceNew daysBefore = (IntegerDialogPreferenceNew) findPreference(getString(R.string.default_reminder_days_before_preference_key));
            daysBefore.setSummary(daysBefore.toString());
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            DialogFragment dialogFragment = null;
            if (preference instanceof IntegerDialogPreferenceNew) {
                dialogFragment = new IntegerPreferenceDialogFragmentCompat();
            } else if (preference instanceof TimePreferenceNew) {
                dialogFragment = new TimePreferenceDialogFragmentCompat();
            }

            if (dialogFragment != null) {
                setDialogFragmentArguments(dialogFragment, preference.getKey());
                dialogFragment.setTargetFragment(this, 0);
                if (this.getFragmentManager() != null) {
                    dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
                }
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        private void setDialogFragmentArguments(DialogFragment dialogFragment, String key) {
            Bundle bundle = new Bundle(1);
            bundle.putString("key", key);
            dialogFragment.setArguments(bundle);
        }
    }
}
