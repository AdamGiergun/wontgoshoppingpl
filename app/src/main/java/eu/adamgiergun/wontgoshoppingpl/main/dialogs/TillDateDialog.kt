package eu.adamgiergun.wontgoshoppingpl.main.dialogs

import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import eu.adamgiergun.wontgoshoppingpl.calendar.SimpleDate
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel
import java.util.Calendar
import java.util.TimeZone

/**
 * Wybór daty, do której sięga dodawana lista dni.
 *
 * Zastąpiło platformowy `DatePickerDialog` w dwóch wariantach stylu — patrz
 * [ReminderTimeDialog], powody są te same.
 *
 * [MaterialDatePicker] operuje na milisekundach w UTC, a nie w strefie
 * lokalnej. Data zamieniona przez zwykły `Calendar.getInstance()` potrafi
 * wylądować o dzień obok, więc konwersja idzie w obie strony przez [utcMillis]
 * i [toSimpleDate].
 */
internal object TillDateDialog {

    private const val TAG = "till_date_picker"

    fun show(fragmentManager: FragmentManager, viewModel: DaysFragmentViewModel) {
        val wybrana = viewModel.tillDate.value?.let { utcMillis(it.year, it.month, it.day) }
            ?: MaterialDatePicker.todayInUtcMilliseconds()
        val ostatnia = lastAllowedUtcMillis(viewModel.lastDateInMillis)

        MaterialDatePicker.Builder.datePicker()
            .setSelection(wybrana)
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    // Zakres domyślny to rok 1900–2100; bez zawężenia użytkownik
                    // przewijałby setki pustych miesięcy.
                    .setStart(minOf(wybrana, MaterialDatePicker.todayInUtcMilliseconds()))
                    .setEnd(ostatnia)
                    .setOpenAt(wybrana)
                    // setEnd ogranicza tylko widoczne miesiące — dni po ostatniej
                    // dostępnej dacie trzeba wyłączyć osobno, tak jak robiło to
                    // dawne datePicker.maxDate.
                    .setValidator(DateValidatorPointBackward.before(ostatnia))
                    .build()
            )
            .build()
            .also { picker ->
                connect(picker, viewModel)
                picker.show(fragmentManager, TAG)
            }
    }

    fun reconnectIfShown(fragmentManager: FragmentManager, viewModel: DaysFragmentViewModel) {
        @Suppress("UNCHECKED_CAST")
        (fragmentManager.findFragmentByTag(TAG) as? MaterialDatePicker<Long>)?.let { connect(it, viewModel) }
    }

    private fun connect(picker: MaterialDatePicker<Long>, viewModel: DaysFragmentViewModel) {
        picker.addOnPositiveButtonClickListener { selection ->
            // setTillDate oczekuje miesiąca liczonego od 1, tak jak SimpleDate.
            toSimpleDate(selection).let { viewModel.setTillDate(it.year, it.month, it.day) }
        }
        picker.addOnDismissListener { viewModel.setActionNone() }
    }

    /** Ostatni dzień z bazy podany w czasie lokalnym, przeliczony na północ UTC. */
    private fun lastAllowedUtcMillis(localMillis: Long): Long =
        Calendar.getInstance().apply { timeInMillis = localMillis }.let { lokalny ->
            utcMillis(
                lokalny[Calendar.YEAR],
                lokalny[Calendar.MONTH] + 1,
                lokalny[Calendar.DAY_OF_MONTH]
            )
        }

    private fun utcMillis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month - 1, day)
        }.timeInMillis

    private fun toSimpleDate(utc: Long): SimpleDate =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utc }.let {
            SimpleDate(it[Calendar.YEAR], it[Calendar.MONTH] + 1, it[Calendar.DAY_OF_MONTH])
        }
}
