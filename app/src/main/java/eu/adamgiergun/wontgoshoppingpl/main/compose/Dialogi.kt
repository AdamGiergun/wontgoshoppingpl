package eu.adamgiergun.wontgoshoppingpl.main.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.calendar.SimpleDate
import eu.adamgiergun.wontgoshoppingpl.main.activity.MainActivity
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel
import java.util.Calendar
import java.util.TimeZone

/**
 * Wszystkie dialogi ekranu listy.
 *
 * Zastępują sześć `DialogFragment`-ów i dwa layouty XML. Powodem nie jest sama
 * spójność stosu, tylko trzy konkretne rzeczy, które w warstwie fragmentów
 * wymagały obejść:
 *
 * 1. `MaterialTimePicker` i `MaterialDatePicker` są **klasami finalnymi i same są
 *    fragmentami**, więc nie dało się po nich dziedziczyć — istniały jako obiekty
 *    z fabrykami zamiast klas.
 * 2. Po obrocie ekranu menedżer fragmentów odtwarzał selektor, ale nasłuchy
 *    zarejestrowane na poprzedniej instancji przepadały. Trzeba je było podpinać
 *    ponownie (`reconnectIfShown`), inaczej potwierdzenie godziny nie robiło nic.
 *    Tutaj stan selektora jest zwykłym stanem kompozycji i przeżywa obrót sam.
 * 3. Przycisk OK w wyborze typów dni włączało się i wyłączało imperatywnie,
 *    sięgając do `alertDialog.getButton(BUTTON_POSITIVE)` z nasłuchu zaznaczenia.
 *
 * Widoczność steruje `MainActivity.Actions` — tak jak dotąd. Aktywność przestała
 * te dialogi pokazywać, bo pokazanie i zamknięcie muszą być po tej samej stronie:
 * gdyby aktywność pokazywała, a Compose zamykał, akcja wracałaby do NONE zanim
 * kompozycja zdążyłaby ją zobaczyć.
 */
@Composable
internal fun Dialogi(viewModel: DaysFragmentViewModel) {
    val akcja by viewModel.nextAction.observeAsState(MainActivity.Actions.NONE)
    val zagniezdzony by viewModel.nestedDialog.observeAsState(DaysFragmentViewModel.NestedDialog.NONE)
    val activity = LocalContext.current.znajdzAktywnosc()

    when (akcja) {
        MainActivity.Actions.ASK_FOR_DAY_TYPES_ADDED_TO_CALENDAR ->
            DialogTypowDni(viewModel)

        MainActivity.Actions.ASK_FOR_REMINDER_SETTINGS ->
            DialogPrzypomnienia(
                viewModel = viewModel,
                tytul = stringResource(R.string.title_reminder_of) + " " +
                        viewModel.dayDescriptionForDialog,
                zDataDo = false,
                zDomyslnym = true,
                onOk = { activity?.let { viewModel.addToCalendar(false, it) } },
            )

        MainActivity.Actions.ASK_FOR_REMINDER_SETTINGS_AND_DATE_TILL ->
            DialogPrzypomnienia(
                viewModel = viewModel,
                tytul = stringResource(R.string.title_reminder),
                zDataDo = true,
                zDomyslnym = false,
                onOk = { activity?.let { viewModel.addToCalendar(true, it) } },
            )

        else -> Unit
    }

    // Selektory zagnieżdżone: otwierane z wnętrza dialogu przypomnienia, więc
    // sterowane osobnym stanem, nie akcją aktywności.
    when (zagniezdzony) {
        DaysFragmentViewModel.NestedDialog.DAYS_BEFORE -> DialogDniPrzed(viewModel)
        DaysFragmentViewModel.NestedDialog.TIME -> DialogGodziny(viewModel)
        DaysFragmentViewModel.NestedDialog.TILL_DATE -> DialogDaty(viewModel)
        DaysFragmentViewModel.NestedDialog.NONE -> Unit
    }
}

/** Wybór typów dni dodawanych do kalendarza. Kolejność jak w dawnym `items`. */
@Composable
private fun DialogTypowDni(viewModel: DaysFragmentViewModel) {
    val zaznaczone = remember {
        mutableStateListOf(*viewModel.getDayTypesToBeAddedToCalendarAsArray().toTypedArray())
    }
    val etykiety = listOf(
        stringResource(R.string.days_of_limited_commerce),
        stringResource(R.string.holidays),
        stringResource(R.string.commerce_sundays),
    )

    AlertDialog(
        onDismissRequest = { viewModel.setActionNone() },
        title = { Text(stringResource(R.string.title_choose_events)) },
        text = {
            Column {
                etykiety.forEachIndexed { index, etykieta ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = zaznaczone[index],
                                onValueChange = { nowa ->
                                    zaznaczone[index] = nowa
                                    viewModel.updateDayTypesToBeAddedToCalendar(index, nowa)
                                },
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = zaznaczone[index], onCheckedChange = null)
                        Text(text = etykieta, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                // Dawniej robił to imperatywny dostęp do przycisku dialogu
                // z nasłuchu zaznaczenia. Tutaj to zwykły odczyt stanu.
                enabled = zaznaczone.any { it },
                // Podmiana akcji zamyka ten dialog i otwiera następny w jednym
                // ruchu — nie trzeba osobno zerować.
                onClick = { viewModel.askForReminderSettingsAndDateTill() },
            ) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            AnulujZKomunikatem(viewModel)
        },
    )
}

/**
 * Ustawienia przypomnienia. Jedna kompozycja obsługuje oba dawne dialogi —
 * różniły się wyłącznie tym, czy mają pole „do daty" ([zDataDo]) czy pole
 * „zapisz jako domyślne" ([zDomyslnym]).
 */
@Composable
private fun DialogPrzypomnienia(
    viewModel: DaysFragmentViewModel,
    tytul: String,
    zDataDo: Boolean,
    zDomyslnym: Boolean,
    onOk: () -> Unit,
) {
    val dniPrzed by viewModel.reminderDaysBefore.observeAsState()
    val godzina by viewModel.reminderTime.observeAsState()
    val dataDo by viewModel.tillDate.observeAsState()
    var jakoDomyslne by remember { mutableStateOf(false) }

    // Dawniej wołane w builderze dialogu, tuż przed `create()`. Przywraca wartości
    // domyślne z preferencji przy każdym otwarciu.
    LaunchedEffect(Unit) { viewModel.initializeReminder() }

    AlertDialog(
        onDismissRequest = { viewModel.setActionNone() },
        title = { Text(tytul) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.remind_me))
                    PoleDoKliknięcia(
                        tekst = dniPrzed?.toString().orEmpty(),
                        onClick = { viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.DAYS_BEFORE) },
                    )
                    Text(stringResource(R.string.days))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.before_comma_at))
                    PoleDoKliknięcia(
                        tekst = godzina?.toString().orEmpty(),
                        onClick = { viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.TIME) },
                    )
                }
                if (zDataDo) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.till))
                        PoleDoKliknięcia(
                            tekst = dataDo?.toString().orEmpty(),
                            onClick = { viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.TILL_DATE) },
                        )
                        Text(stringResource(R.string.dot))
                    }
                }
                if (zDomyslnym) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = jakoDomyslne,
                                onValueChange = { jakoDomyslne = it },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = jakoDomyslne, onCheckedChange = null)
                        Text(
                            text = stringResource(R.string.set_as_default),
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (jakoDomyslne) viewModel.saveCurrentReminderAsDefault()
                viewModel.setActionNone()
                onOk()
            }) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = { AnulujZKomunikatem(viewModel) },
    )
}

/**
 * Liczba dni przed wydarzeniem.
 *
 * Dawniej platformowy `NumberPicker` — walec, którego Compose nie ma i który przy
 * dziesięciu wartościach był i tak przesadą. Zakres 0-9 mieści się w rzędzie
 * żetonów, gdzie widać wszystkie opcje naraz i wybór to jedno dotknięcie zamiast
 * przewijania.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogDniPrzed(viewModel: DaysFragmentViewModel) {
    val biezaca = viewModel.reminderDaysBefore.value ?: 1
    var wybrana by remember { mutableStateOf(biezaca) }

    AlertDialog(
        onDismissRequest = { viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.NONE) },
        title = { Text(stringResource(R.string.days_before)) },
        text = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (0..9).forEach { n ->
                    FilterChip(
                        selected = n == wybrana,
                        onClick = { wybrana = n },
                        label = { Text(n.toString()) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.setReminderDaysBefore(wybrana)
                viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.NONE)
            }) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.NONE) }) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogGodziny(viewModel: DaysFragmentViewModel) {
    val biezaca = viewModel.reminderTime.value
    val stan = rememberTimePickerState(
        initialHour = biezaca?.hour ?: DOMYSLNA_GODZINA,
        initialMinute = biezaca?.minute ?: 0,
        // Zegar 24-godzinny jak dotąd — reszta aplikacji pokazuje godziny w tym
        // formacie niezależnie od ustawień systemu.
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = { viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.NONE) },
        text = { TimePicker(state = stan) },
        confirmButton = {
            TextButton(onClick = {
                viewModel.setReminderTime(stan.hour, stan.minute)
                viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.NONE)
            }) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.NONE) }) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}

/**
 * Data, do której sięga dodawana lista dni.
 *
 * Selektor operuje na milisekundach w **UTC**, a nie w strefie lokalnej — data
 * przeliczona zwykłym `Calendar.getInstance()` potrafi wylądować o dzień obok.
 * Konwersja idzie więc w obie strony przez jawny kalendarz w UTC. To ta sama
 * pułapka co w `MaterialDatePicker` i nie znika razem z przejściem na Compose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogDaty(viewModel: DaysFragmentViewModel) {
    val ostatnia = remember { ostatniDozwolonyUtc(viewModel.lastDateInMillis) }
    val wybranaPoczatkowo = remember {
        viewModel.tillDate.value?.let { utcMillis(it.year, it.month, it.day) }
    }
    val stan = rememberDatePickerState(
        initialSelectedDateMillis = wybranaPoczatkowo,
        selectableDates = remember(ostatnia) {
            object : SelectableDates {
                // Odpowiednik dawnego DateValidatorPointBackward: dni po ostatniej
                // dacie z bazy są niewybieralne, bo nie ma dla nich danych.
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= ostatnia
            }
        },
    )

    DatePickerDialog(
        onDismissRequest = { viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.NONE) },
        confirmButton = {
            TextButton(
                enabled = stan.selectedDateMillis != null,
                onClick = {
                    stan.selectedDateMillis?.let { millis ->
                        naSimpleDate(millis).let { viewModel.setTillDate(it.year, it.month, it.day) }
                    }
                    viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.NONE)
                },
            ) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showNestedDialog(DaysFragmentViewModel.NestedDialog.NONE) }) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        colors = DatePickerDefaults.colors(),
    ) {
        DatePicker(state = stan)
    }
}

/** Pole wyglądające na edytowalne, otwierające zagnieżdżony selektor. */
@Composable
private fun PoleDoKliknięcia(tekst: String, onClick: () -> Unit) {
    Text(
        text = tekst,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

/** Anulowanie z komunikatem — dawny `showInfoCancelled` z `MainDialogFragment`. */
@Composable
private fun AnulujZKomunikatem(viewModel: DaysFragmentViewModel) {
    val context = LocalContext.current
    TextButton(onClick = {
        viewModel.setActionNone()
        Toast.makeText(context, R.string.info_cancelled, Toast.LENGTH_SHORT).show()
    }) { Text(stringResource(android.R.string.cancel)) }
}

private const val DOMYSLNA_GODZINA = 11

private fun Context.znajdzAktywnosc(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.znajdzAktywnosc()
    else -> null
}

private fun ostatniDozwolonyUtc(lokalneMillis: Long): Long =
    Calendar.getInstance().apply { timeInMillis = lokalneMillis }.let { lokalny ->
        utcMillis(
            lokalny[Calendar.YEAR],
            lokalny[Calendar.MONTH] + 1,
            lokalny[Calendar.DAY_OF_MONTH],
        )
    }

private fun utcMillis(rok: Int, miesiac: Int, dzien: Int): Long =
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(rok, miesiac - 1, dzien)
    }.timeInMillis

private fun naSimpleDate(utc: Long): SimpleDate =
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utc }.let {
        SimpleDate(it[Calendar.YEAR], it[Calendar.MONTH] + 1, it[Calendar.DAY_OF_MONTH])
    }
