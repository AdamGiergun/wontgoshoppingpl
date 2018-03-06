package eu.adamgiergun.niekupiszpl;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import eu.adamgiergun.mylibrary.AppCompatPreferenceActivity;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }

    /**
     * Fragment for settings.
     */
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
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
}
