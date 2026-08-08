package eu.adamgiergun.wontgoshoppingpl.settings

import android.app.Dialog
import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

@Suppress("unused")
class IntegerPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        (preference as IntegerDialogPreference).let { integerDialogPreference ->
            (savedInstanceState == null).let {
                val value = if (it) integerDialogPreference.value else 0
                val minValue = if (it) integerDialogPreference.minValue else 0
                val maxValue = if (it) integerDialogPreference.maxValue else 9
                return IntegerPickerDialog(
                        requireContext(),
                        integerDialogPreference,
                        value,
                        minValue,
                        maxValue).apply {
                    // Okno dialogu i tak ma szerokość minimalną z motywu, więc bez
                    // tytułu zostawał szeroki pas pustki nad jednym wąskim
                    // pokrętłem. PreferenceDialogFragmentCompat ustawia tytuł sam,
                    // ale tu onCreateDialog jest nadpisane w całości.
                    setTitle(integerDialogPreference.dialogTitle ?: integerDialogPreference.title)
                }
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {}
}