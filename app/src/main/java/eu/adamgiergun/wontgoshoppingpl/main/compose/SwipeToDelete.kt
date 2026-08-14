package eu.adamgiergun.wontgoshoppingpl.main.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Usunięcie dnia z Kalendarza Google gestem odrzucenia.
 *
 * Napisane ręcznie, bo `SwipeToDismissBox` z Material 3 obsługuje wyłącznie gest
 * poziomy, a przy wężu lista przewija się w bok — gest musi być prostopadły do
 * kierunku przewijania, inaczej przewijanie go przechwyci. Stąd parametr [poziomo].
 *
 * **Karta zawsze wraca na miejsce.** To nie jest ustępstwo, tylko odwzorowanie
 * tego, co robi aplikacja: „usunięcie" zrywa powiązanie z kalendarzem, ale dzień
 * zostaje na liście — zmienia mu się tylko znacznik. Pierwsza wersja kazała
 * karcie odjechać poza ekran przed powrotem i to był błąd w dwóch warstwach.
 * Semantycznie sugerowała zniknięcie wpisu, którego nie ma. Technicznie zaś
 * `onDragStopped` biegnie w zakresie należącym do gestu: usunięcie ustawia
 * `isDbChanged`, to przeładowuje listę, a przeładowanie potrafi anulować ten
 * zakres, zanim wykona się powrót — karta zostawała wtedy poza ekranem
 * całkowicie albo w połowie.
 *
 * [resetKey] jest drugim zabezpieczeniem tej samej rzeczy: przy zmianie danych
 * wiersza przesunięcie wraca do zera bez udziału zakresu gestu. Klucz LazyColumn
 * opiera się na dacie, więc po przeładowaniu ten sam slot dostaje ten sam
 * zapamiętany [Animatable] i bez zerowania odziedziczyłby jego przesunięcie.
 */
@Composable
internal fun SwipeToDelete(
    poziomo: Boolean,
    onDelete: () -> Unit,
    resetKey: Any?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val przesuniecie = remember { Animatable(0f) }
    val zakres = rememberCoroutineScope()
    var rozmiar by remember { mutableIntStateOf(0) }

    LaunchedEffect(resetKey) { przesuniecie.snapTo(0f) }

    Box(
        modifier = modifier
            .onSizeChanged { rozmiar = if (poziomo) it.width else it.height }
            .offset {
                val p = przesuniecie.value.roundToInt()
                if (poziomo) IntOffset(p, 0) else IntOffset(0, p)
            }
            .alpha(
                if (rozmiar == 0) 1f
                else (1f - przesuniecie.value.absoluteValue / rozmiar).coerceIn(0.35f, 1f)
            )
            .draggable(
                orientation = if (poziomo) Orientation.Horizontal else Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    zakres.launch { przesuniecie.snapTo(przesuniecie.value + delta) }
                },
                onDragStopped = {
                    val prog = rozmiar / 3f
                    val przekroczony = rozmiar > 0 && przesuniecie.value.absoluteValue > prog
                    // Powrót najpierw, dopiero potem skutek uboczny — gdyby
                    // kolejność była odwrotna, przeładowanie listy mogłoby anulować
                    // animację i zostawić kartę przesuniętą.
                    przesuniecie.animateTo(0f)
                    if (przekroczony) onDelete()
                },
            ),
    ) {
        content()
    }
}
