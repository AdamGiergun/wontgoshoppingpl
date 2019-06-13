package eu.adamgiergun.wontgoshoppingpl.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;

import eu.adamgiergun.mylibrary.BasePreferenceFragmentCompat;
import eu.adamgiergun.mylibrary.IntegerDialogPreference;
import eu.adamgiergun.mylibrary.IntegerPreferenceDialogFragmentCompat;
import eu.adamgiergun.mylibrary.calendarmanagement.CalendarsListPreference;
import eu.adamgiergun.mylibrary.calendarmanagement.TimePreference;
import eu.adamgiergun.mylibrary.calendarmanagement.TimePreferenceDialogFragmentCompat;
import eu.adamgiergun.wontgoshoppingpl.R;

public class NotificationPreferenceFragment extends BasePreferenceFragmentCompat {

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_notification, rootKey);
        setSummaries();
    }

    private void setSummaries() {
        IntegerDialogPreference daysBefore = findPreference(getString(R.string.default_reminder_days_before_preference_key));
        if (daysBefore != null) {
            daysBefore.setSummary(daysBefore.toString());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.default_calendar_preference_key))) {
            Preference preference = findPreference(key);
            if (preference instanceof CalendarsListPreference) {
                CharSequence calendarId = sharedPreferences.getString(key, "0");
                CharSequence summary = ((CalendarsListPreference) preference).getCalendarName(calendarId);
                preference.setSummary(summary);
            }
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof IntegerDialogPreference) {
            dialogFragment = new IntegerPreferenceDialogFragmentCompat();
        } else if (preference instanceof TimePreference) {
            dialogFragment = new TimePreferenceDialogFragmentCompat();
        }

        if (dialogFragment != null) {
            setDialogFragmentArguments(dialogFragment, preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            if (this.getFragmentManager() != null) {
                dialogFragment.show(this.getFragmentManager(), preference.getClass().getName());
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

    // TODO ChooseCalendarsForCleaning

//        @Override
//        public boolean onPreferenceTreeClick(Preference preference) {
//            if (preference.getKey().equals(getString(R.string.key_delete_events))) {
//                DialogFragment dialogFragment = new ChooseCalendarsForCleaning();
//                if (this.getFragmentManager() != null) {
//                    dialogFragment.show(this.getFragmentManager(), "test");
//                }
//                return true;
//            } else {
//                return super.onPreferenceTreeClick(preference);
//            }
//        }
}
