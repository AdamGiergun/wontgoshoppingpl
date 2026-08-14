package eu.adamgiergun.wontgoshoppingpl.main.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Usuwanie wpisu z kalendarza gestem odrzucenia.
 *
 * Napisane ręcznie, i to jest jedyne miejsce w tej migracji, gdzie Compose **nie**
 * dał gotowca. `SwipeToDismissBox` z Material 3 obsługuje wyłącznie gest poziomy,
 * a przy wężu lista przewija się w bok — gest usuwania musi być prostopadły do
 * kierunku przewijania, inaczej przewijanie go przechwyci i usunięcie stanie się
 * niemożliwe. Stąd parametr [poziomo] i własna obsługa zamiast komponentu
 * bibliotecznego.
 *
 * Próg to jedna trzecia rozmiaru wzdłuż osi gestu. Poniżej progu element wraca
 * na miejsce z animacją, powyżej — odjeżdża do końca i dopiero wtedy woła
 * [onDelete], więc kasowanie widać, zamiast żeby wiersz znikał w połowie ruchu.
 */
@Composable
internal fun SwipeToDelete(
    poziomo: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val przesuniecie = remember { Animatable(0f) }
    val zakres = rememberCoroutineScope()
    var rozmiar by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .onSizeChanged { rozmiar = if (poziomo) it.width else it.height }
            .offset {
                val p = przesuniecie.value.roundToInt()
                if (poziomo) IntOffset(p, 0) else IntOffset(0, p)
            }
            .alpha(
                if (rozmiar == 0) 1f
                else (1f - przesuniecie.value.absoluteValue / rozmiar).coerceIn(0.2f, 1f)
            )
            .draggable(
                orientation = if (poziomo) Orientation.Horizontal else Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    zakres.launch { przesuniecie.snapTo(przesuniecie.value + delta) }
                },
                onDragStopped = {
                    val prog = rozmiar / 3f
                    if (rozmiar > 0 && przesuniecie.value.absoluteValue > prog) {
                        val cel = if (przesuniecie.value > 0) rozmiar.toFloat() else -rozmiar.toFloat()
                        przesuniecie.animateTo(cel)
                        onDelete()
                        // Element zostaje na liście, dopóki baza nie zgłosi zmiany —
                        // przywrócenie pozycji jest więc konieczne, inaczej po
                        // nieudanym usunięciu wiersz zostałby niewidoczny.
                        przesuniecie.snapTo(0f)
                    } else {
                        przesuniecie.animateTo(0f)
                    }
                },
            ),
    ) {
        content()
    }
}
