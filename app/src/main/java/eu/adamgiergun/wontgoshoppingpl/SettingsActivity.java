package eu.adamgiergun.wontgoshoppingpl;

import android.content.*;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.*;

import eu.adamgiergun.mylibrary.*;
import eu.adamgiergun.mylibrary.calendarmanagement.*;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getBaseContext(), R.xml.preferences, false);
        setContentView(R.layout.settings_activity);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

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
                if (connectionPref instanceof NewCalendarsListPreference) {
                    CharSequence calendarId = sharedPreferences.getString(key, "0");
                    //getString(R.string.calendar_not_set));
                    CharSequence summary = ((NewCalendarsListPreference) connectionPref).getCalendarName(calendarId);
                    connectionPref.setSummary(summary);
                }
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


    static boolean isUseGoogleCalendarSet(Context context) {
        String key = context.getString(R.string.use_google_calendar_preference_key);
        return getBooleanValueOfPreference(context,key, false);
    }

    static boolean showHolidays(Context context) {
        String key = context.getString(R.string.include_holidays_preference_key);
        return getBooleanValueOfPreference(context,key, true);
    }

    private static boolean getBooleanValueOfPreference(Context context, String key, boolean defaultValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(key, defaultValue);
    }
    
    private static SharedPreferences getSharedPreferences(Context context) {
        String preferencesName = context.getString(R.string.default_preferences_name);
        return context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    }

    static Integer getReminderDaysFromPreferences(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        String key = context.getString(R.string.default_reminder_days_before_preference_key);
        Resources resources = context.getResources();
        int defaultValue = resources.getInteger(R.integer.reminder_days_before_default_value);        
        return prefs.getInt(key, defaultValue);
    }

    static String getReminderTime(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        String key = context.getString(R.string.default_reminder_time_preference_key);
        String defaultValue = context.getString(R.string.default_reminder_time_preference_default_value);
        return prefs.getString(key, defaultValue);
    }
    
}
