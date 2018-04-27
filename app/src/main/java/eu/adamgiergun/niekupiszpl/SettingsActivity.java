package eu.adamgiergun.niekupiszpl;

import android.content.*;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.*;

import eu.adamgiergun.mylibrary.*;
import eu.adamgiergun.mylibrary.calendarmanagement.CalendarsListPreference;

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
                if (connectionPref instanceof CalendarsListPreference) {
                    CharSequence calendarId = sharedPreferences.getString(key, "0");
                    //getString(R.string.calendar_not_set));
                    CharSequence summary = ((CalendarsListPreference) connectionPref).getCalendarName(calendarId);
                    connectionPref.setSummary(summary);
                }
            }
        }

        private void setSummaries() {
            NewIntegerDialogPreference daysBefore = (NewIntegerDialogPreference) findPreference(getString(R.string.default_reminder_days_before_preference_key));
            daysBefore.setSummary(daysBefore.toString());
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference instanceof NewIntegerDialogPreference) {
/*                NewIntegerDialogPreference dialogFragment = NewIntegerDialogPreference.newInstance(preference.getKey(), getContext());
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(getFragmentManager(), null);*/
            } else super.onDisplayPreferenceDialog(preference);
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
