package eu.adamgiergun.wontgoshoppingpl.main.dialogs

import androidx.fragment.app.FragmentManager
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel

/**
 * Wybór godziny przypomnienia.
 *
 * Wcześniej była to klasa dziedzicząca po [MainDialogFragment] i zwracająca
 * platformowy `TimePickerDialog` w dwóch wariantach stylu. Platformowy selektor
 * bierze wygląd z motywu systemowego, nie aplikacji, więc po przejściu na
 * Material 3 wyglądałby obco. [MaterialTimePicker] jest klasą finalną i sam
 * jest fragmentem dialogowym, więc nie da się po nim dziedziczyć — stąd obiekt
 * z fabryką zamiast klasy.
 */
internal object ReminderTimeDialog {

    private const val TAG = "reminder_time_picker"

    fun show(fragmentManager: FragmentManager, viewModel: DaysFragmentViewModel) {
        val time = viewModel.reminderTime.value
        MaterialTimePicker.Builder()
            // Zegar 24-godzinny jak dotąd — reszta aplikacji pokazuje godziny
            // w tym formacie niezależnie od ustawień systemu.
            .setTimeFormat(TimeFormat.CLOCK_24H)
            // Tarcza, nie klawiatura: MaterialTimePicker sam przechodzi na wpisywanie
            // z klawiatury, gdy uzna, że jest za mało miejsca.
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setHour(time?.hour ?: DEFAULT_HOUR)
            .setMinute(time?.minute ?: DEFAULT_MINUTE)
            .build()
            .also { picker ->
                connect(picker, viewModel)
                picker.show(fragmentManager, TAG)
            }
    }

    /**
     * Po obrocie ekranu menedżer fragmentów odtwarza selektor, ale nasłuchy
     * zarejestrowane na poprzedniej instancji przepadają. Trzeba je podpiąć
     * ponownie, inaczej potwierdzenie godziny nie zrobi nic.
     */
    fun reconnectIfShown(fragmentManager: FragmentManager, viewModel: DaysFragmentViewModel) {
        (fragmentManager.findFragmentByTag(TAG) as? MaterialTimePicker)?.let { connect(it, viewModel) }
    }

    private fun connect(picker: MaterialTimePicker, viewModel: DaysFragmentViewModel) {
        picker.addOnPositiveButtonClickListener {
            viewModel.setReminderTime(picker.hour, picker.minute)
        }
        picker.addOnDismissListener { viewModel.setActionNone() }
    }

    private const val DEFAULT_HOUR = 11
    private const val DEFAULT_MINUTE = 0
}
