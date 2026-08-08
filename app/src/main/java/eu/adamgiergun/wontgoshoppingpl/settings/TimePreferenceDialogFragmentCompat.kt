package eu.adamgiergun.wontgoshoppingpl.settings

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

class TimePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {
    private var hour = 0
    private var minute = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        with(preference as TimePreference) {
            if (savedInstanceState == null) {
                hour = this.time.hour
                minute = this.time.minute
            }
            return TimePickerDialog(
                    context,
                    this,
                    hour,
                    minute,
                    true)
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {}
}