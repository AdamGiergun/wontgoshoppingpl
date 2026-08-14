package eu.adamgiergun.wontgoshoppingpl.main.compose

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as siatkaItemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.Event
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Ekran listy dni.
 *
 * Zastępuje `DaysFragment.ulozListe` / `ulozWeza`, `DaysRecyclerViewAdapter`
 * i `ColumnSnapHelper`. Zniknęły przy tym trzy obejścia, które w warstwie widoków
 * były konieczne i każde miało własny komentarz ostrzegawczy:
 *
 * 1. `post { }` po podmianie menedżera układu — `requestLayout` wywołany w trakcie
 *    trwającego przebiegu układu był odkładany i już nie wracał, przez co lista
 *    zostawała bez menedżera i nie rysowała ani jednego wiersza.
 * 2. `addOnLayoutChangeListener` zamiast `doOnLayout`, bo baner wczytuje się
 *    później i skraca listę już po pierwszym układzie.
 * 3. `notifyDataSetChanged` przy zmianie gęstości, bo dane się nie zmieniały,
 *    więc `DiffUtil` nie miał czego porównać.
 *
 * Wszystkie trzy wynikały z tego samego: wysokość dostępna dla listy była znana
 * dopiero po układzie, a układ trzeba było wywołać ponownie z callbacku.
 * [BoxWithConstraints] podaje tę wysokość **przed** złożeniem zawartości, więc
 * liczba wierszy jest zwykłym wyrażeniem, a nie skutkiem ubocznym.
 */
@Composable
internal fun DaysScreen(
    viewModel: DaysFragmentViewModel,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val events by viewModel.events.observeAsState(emptyList())

    // Obserwator, który wcześniej siedział w `init` adaptera. Bez niego lista nie
    // odświeżyłaby się po zapisie do kalendarza. Odczyt z bazy jest tu konieczny,
    // a nie tylko wygodny: `Event.equals` i `hashCode` opierają się WYŁĄCZNIE na
    // dacie, więc podmiana `calendarEventId` w istniejącym obiekcie jest dla
    // Compose niewidoczna — dopiero nowe obiekty z zapytania dają nową tożsamość.
    val zmianaWBazie by viewModel.isDbChanged.observeAsState(false)
    LaunchedEffect(zmianaWBazie) {
        if (zmianaWBazie) viewModel.updateDisplayedDataSetIfNeeded()
    }

    // Gęstość listy to preferencja czytana z SharedPreferences, więc nie zawiadamia
    // o zmianie sama z siebie. Odczyt przy wznowieniu zastępuje `onResume` plus
    // `odswiezPoZmianieGestosci` plus pole śledzące poprzednią wartość: tutaj
    // wystarczy, że stan się zmieni, a przerysuje się to, co go czyta.
    var zwarty by remember { mutableStateOf(viewModel.isListCompact) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.updateDisplayedDataSetIfNeeded()
        zwarty = viewModel.isListCompact
    }

    val kolumny = integerResource(R.integer.list_span_count)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        // Ograniczenie szerokości treści: na telefonie nigdy nie działa, na tablecie
        // powstrzymuje wiersze przed rozciągnięciem na 2560 px, gdzie data i nazwa
        // dnia oddalają się na tyle, że przestają się czytać jako jeden wiersz.
        //
        // Czytane z zasobu, a nie wpisane w kodzie, bo ma wariant kwalifikowany:
        // od 1260 dp limit rośnie do 1560 dp, inaczej trzy kolumny węża ściskają
        // się do 220 dp. Ta wartość jest w dp, więc dimensionResource jest tu
        // właściwe — inaczej niż przy rozmiarach tekstu, które są w sp.
        Box(
            modifier = Modifier
                .widthIn(max = dimensionResource(R.dimen.content_max_width))
                .fillMaxSize()
        ) {
            if (kolumny <= 1) {
                ListaPionowa(events, viewModel, zwarty, scope)
            } else {
                Waz(events, viewModel, zwarty, kolumny, scope)
            }
        }
    }
}

@Composable
private fun ListaPionowa(
    events: List<Event>,
    viewModel: DaysFragmentViewModel,
    zwarty: Boolean,
    scope: CoroutineScope,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(events, key = { _, event -> event.hashCode() }) { index, event ->
            val day = pamietajDzien(event)
            // Kierunek gestu musi być prostopadły do kierunku przewijania, inaczej
            // przewijanie przechwyci gest i usunięcie stanie się niemożliwe.
            SwipeToDelete(
                poziomo = true,
                onDelete = { scope.launch { viewModel.deleteFromCalendar(day) } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                DayRow(
                    day = day,
                    zwarty = zwarty,
                    wypelniaWysokosc = false,
                    onClick = { viewModel.setLastTouchedItem(day, index, false) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Wąż: dni idą w dół pierwszej kolumny, potem w dół drugiej, a przewijanie
 * odbywa się w bok.
 *
 * W warstwie widoków robił to `GridLayoutManager` w orientacji poziomej, gdzie
 * `spanCount` znaczy liczbę WIERSZY, nie kolumn — źródło trwałego zamieszania.
 * [LazyHorizontalGrid] nazywa to wprost: `rows`.
 */
@Composable
private fun Waz(
    events: List<Event>,
    viewModel: DaysFragmentViewModel,
    zwarty: Boolean,
    kolumny: Int,
    scope: CoroutineScope,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Minimalna wysokość jest w dp, więc nie wie nic o systemowym rozmiarze
        // czcionki. Tekst wiersza jest w sp i przy powiększonej czcionce rośnie,
        // a wysokość komórki narzuca układ — bez tej korekty wierszy wychodzi
        // za dużo i tekst zostaje przycięty zamiast dostać miejsce.
        val skalaCzcionki = LocalDensity.current.fontScale.coerceAtLeast(1f)
        // Wyższe niż w warstwie widoków (56/68 dp) i to jest poprawka, nie kaprys:
        // kolumna węża ma około 220 dp, więc wiersz układa się piętrowo, a data
        // z dniem tygodnia to już dwie linie (`dateAndWeekdayString` ma twardy \n).
        // Z nazwą dnia daje to trzy linie. Przy 56 dp nazwa była ucięta w połowie liter.
        val minimalnaWysokosc = (if (zwarty) 76.dp else 92.dp) * skalaCzcionki
        val wiersze = (maxHeight / minimalnaWysokosc).toInt().coerceAtLeast(1)
        // Odstępy leżą poza szerokością karty, więc bez ich odjęcia druga kolumna
        // wystaje poza ekran.
        val szerokoscKolumny = (maxWidth / kolumny) - 16.dp

        val stan = rememberLazyGridState()
        LazyHorizontalGrid(
            rows = GridCells.Fixed(wiersze),
            state = stan,
            // Zatrzask kolumny do krawędzi. Bez niego przewijanie w bok zatrzymuje
            // się na uciętych kolumnach po obu stronach ekranu — w warstwie widoków
            // wymagało to własnego ColumnSnapHelper, siedemdziesięciu dziewięciu linii.
            flingBehavior = rememberSnapFlingBehavior(stan),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            siatkaItemsIndexed(events, key = { _, event -> event.hashCode() }) { index, event ->
                val day = pamietajDzien(event)
                SwipeToDelete(
                    poziomo = false,
                    onDelete = { scope.launch { viewModel.deleteFromCalendar(day) } },
                    modifier = Modifier
                        .width(szerokoscKolumny)
                        .fillMaxHeight(),
                ) {
                    DayRow(
                        day = day,
                        // Przy wężu wysokość komórki narzuca układ, więc duże
                        // wypełnienie pionowe nie podnosi wiersza, tylko wypycha
                        // z niego tekst. Gęstość steruje wtedy liczbą wierszy.
                        zwarty = true,
                        wypelniaWysokosc = true,
                        onClick = { viewModel.setLastTouchedItem(day, index, false) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

/**
 * [Day] powstaje z [Event] przez rozbiór ciągu i odczyt zasobów, więc nie ma po co
 * budować go przy każdym złożeniu.
 *
 * Klucz **nie może** być samym `event`: `Event.equals` porównuje wyłącznie datę,
 * więc dodanie dnia do kalendarza — zmieniające `calendarEventId` — byłoby dla
 * `remember` zmianą niewidoczną i znacznik „w kalendarzu" nigdy by się nie pojawił.
 */
@Composable
private fun pamietajDzien(event: Event): Day {
    val context = LocalContext.current
    return remember(event.item, event.calendarEventId, event.typeId) { Day(event, context) }
}
