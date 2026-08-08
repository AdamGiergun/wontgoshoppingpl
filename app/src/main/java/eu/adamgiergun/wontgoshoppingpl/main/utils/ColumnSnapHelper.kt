package eu.adamgiergun.wontgoshoppingpl.main.utils

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Dosuwa kolumnę węża do krawędzi listy.
 *
 * Gotowe [LinearSnapHelper] i `PagerSnapHelper` wyrównują ŚRODEK elementu do
 * środka listy. Przy kolumnach o połowie szerokości ekranu daje to zatrzask
 * w połowie kolumny, czyli dokładnie to, czego chcemy uniknąć — stąd własna
 * wersja liczona od początku elementu.
 *
 * Przy wężu jedna kolumna to `spanCount` kolejnych pozycji, więc cel przewinięcia
 * zaokrągla się w dół do wielokrotności `spanCount`. Bez tego rzut palcem
 * potrafi wylądować na drugim elemencie kolumny, a wtedy zatrzask przesuwa listę
 * o pół kolumny.
 */
internal class ColumnSnapHelper : LinearSnapHelper() {

    private var pomocnik: OrientationHelper? = null

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val odleglosci = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            odleglosci[0] = odlegloscOdPoczatku(targetView, pomocnik(layoutManager))
        }
        return odleglosci
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager !is LinearLayoutManager) return null
        val pierwsza = layoutManager.findFirstVisibleItemPosition()
        if (pierwsza == RecyclerView.NO_POSITION) return null
        // Na końcu listy nie dosuwamy — inaczej ostatniej kolumny nie dałoby się
        // pokazać w całości, bo zatrzask ciągnąłby ją z powrotem do krawędzi.
        if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.itemCount - 1) {
            return null
        }

        val kolumna = layoutManager.findViewByPosition(pierwsza) ?: return null
        val pomocnik = pomocnik(layoutManager)
        val widocznaCzesc = pomocnik.getDecoratedEnd(kolumna)
        val szerokosc = pomocnik.getDecoratedMeasurement(kolumna)
        return if (widocznaCzesc >= szerokosc / 2 && widocznaCzesc > 0) {
            kolumna
        } else {
            layoutManager.findViewByPosition(pierwsza + liczbaWierszy(layoutManager))
        }
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val cel = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (cel == RecyclerView.NO_POSITION) return cel
        val wiersze = liczbaWierszy(layoutManager)
        return cel / wiersze * wiersze
    }

    private fun liczbaWierszy(layoutManager: RecyclerView.LayoutManager) =
        (layoutManager as? GridLayoutManager)?.spanCount ?: 1

    private fun odlegloscOdPoczatku(widok: View, pomocnik: OrientationHelper) =
        pomocnik.getDecoratedStart(widok) - pomocnik.startAfterPadding

    private fun pomocnik(layoutManager: RecyclerView.LayoutManager): OrientationHelper =
        pomocnik.takeIf { it?.layoutManager === layoutManager }
            ?: OrientationHelper.createHorizontalHelper(layoutManager).also { pomocnik = it }
}
