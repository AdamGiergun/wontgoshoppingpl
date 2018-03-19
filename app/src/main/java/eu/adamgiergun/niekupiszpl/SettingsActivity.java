package eu.adamgiergun.niekupiszpl;

import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import eu.adamgiergun.mylibrary.*;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setSummaries();
        }

        private void setSummaries() {
            IntegerDialogPreference daysBefore = (IntegerDialogPreference) findPreference(getString(R.string.default_reminder_days_before_preference_key));
            daysBefore.setSummary(daysBefore.toString());
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            ifSuperReturnsFalseNavigateUpFromSameTask(super.onMenuItemSelected(featureId, item));
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void ifSuperReturnsFalseNavigateUpFromSameTask(boolean superResult) {
        if (!superResult)
            NavUtils.navigateUpFromSameTask(this);
    }

    static boolean useGoogleCalendar(Context context) {
        String key = context.getString(R.string.use_google_calendar_preference_key);
        return getBooleanValueOfPreference(context,key, false);
    }

    static boolean showHolidays(Context context) {
        String key = "include_holidays";
        return getBooleanValueOfPreference(context,key, true);
    }

    private static boolean getBooleanValueOfPreference(Context context, String key, boolean defaultValue) {
        String preferencesName = context.getString(R.string.default_preferences_name);
        SharedPreferences prefs = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, defaultValue);
    }

    protected static Integer getIntegerValueOfPreference(Context context, String key, int defaultValue) {
        String preferencesName = context.getString(R.string.default_preferences_name);
        SharedPreferences prefs = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        return prefs.getInt(key, defaultValue);
    }

    protected static String getStringValueOfPreference(Context context, String key, String defaultValue) {
        String preferencesName = context.getString(R.string.default_preferences_name);
        SharedPreferences prefs = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
    }
}
