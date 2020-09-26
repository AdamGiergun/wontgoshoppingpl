package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import eu.adamgiergun.mykotlinlibrary.BasePreferenceFragmentCompat
import eu.adamgiergun.mykotlinlibrary.IntegerDialogPreference
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.CalendarsListPreference
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.TimePreference
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.TimePreferenceDialogFragmentCompat
import eu.adamgiergun.mykotlinlibrary.IntegerPreferenceDialogFragmentCompat
import eu.adamgiergun.wontgoshoppingpl.R

class NotificationPreferenceFragment : BasePreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_notification, rootKey)
        setSummaries()
    }

    private fun setSummaries() {
        val daysBefore = findPreference<IntegerDialogPreference>(getString(R.string.default_reminder_days_before_preference_key))
        if (daysBefore != null) {
            daysBefore.summary = daysBefore.toString()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let {
            if (key == getString(R.string.default_calendar_preference_key)) {
                val preference = findPreference<Preference>(key)
                if (preference is CalendarsListPreference) {
                    val calendarId: CharSequence = it.getString(key, "0")!!
                    val summary = preference.getCalendarName(calendarId)
                    preference.setSummary(summary)
                }
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        preference?.let {
            val dialogFragment = when (preference) {
                is IntegerDialogPreference -> IntegerPreferenceDialogFragmentCompat()
                is TimePreference -> TimePreferenceDialogFragmentCompat()
                else -> null
            }
            dialogFragment?.let {
                it.arguments = Bundle(1).apply { putString("key", preference.key) }
                it.setTargetFragment(this, 0)
                it.show(parentFragmentManager, preference.javaClass.name)
                return
            }
        }
        super.onDisplayPreferenceDialog(preference)
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