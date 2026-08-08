package eu.adamgiergun.wontgoshoppingpl.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.FragmentDayListBinding
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.ReminderDaysBeforeDialog
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.ReminderTimeDialog
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.TillDateDialog
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel
import kotlinx.coroutines.launch

internal class DaysFragment : Fragment() {

    private val viewModel
        get() = ViewModelProvider(requireActivity())[DaysFragmentViewModel::class.java]

    private var adapter: DaysRecyclerViewAdapter? = null
    private var gestoscPrzyOstatnimRysowaniu: Boolean? = null

    /** Liczba kolumn z zasobów: 1 w pionie, 2 powyżej 840dp szerokości. */
    private val liczbaKolumn
        get() = resources.getInteger(R.integer.list_span_count)

    /**
     * Usuwanie z kalendarza gestem. Kierunek musi być prostopadły do kierunku
     * przewijania, inaczej przewijanie przechwyci gest i usunięcie stanie się
     * niemożliwe — dlatego przy wężu (przewijanie w bok) gest idzie w pionie.
     */
    private fun dayTouchHelperCallback(kierunki: Int): ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, kierunki) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                lifecycleScope.launch { (viewHolder as DaysRecyclerViewAdapter.DayViewHolder).deleteFromGoogleCalendar() }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentDayListBinding.inflate(inflater).let { binding ->
            viewModel.apply {
                logListDensity()

                // Adapter powstaje raz na cykl życia widoku, a kolejne dane wchodzą
                // przez submitList. Wcześniej był tworzony przy KAŻDEJ emisji events,
                // a że jego init rejestruje obserwatora isDbChanged, każda emisja
                // zostawiała kolejnego, nieusuwalnego obserwatora. Przy okazji
                // DiffUtil nigdy nie działał: świeży adapter porównywał z pustą listą,
                // więc każde odświeżenie przebudowywało całą listę.
                val adapter = DaysRecyclerViewAdapter(this, viewLifecycleOwner)
                this@DaysFragment.adapter = adapter
                gestoscPrzyOstatnimRysowaniu = isListCompact
                binding.list.adapter = adapter
                events.observe(viewLifecycleOwner) { adapter.submitList(it) }
                val dialogManager = requireActivity().supportFragmentManager
                // Selektory Material są fragmentami, które przeżywają obrót ekranu
                // bez swoich nasłuchów — podpinamy je z powrotem przy odtworzeniu widoku.
                ReminderTimeDialog.reconnectIfShown(dialogManager, this)
                TillDateDialog.reconnectIfShown(dialogManager, this)
                idOfDialogToShow.observe(viewLifecycleOwner) { id ->
                    when (id) {
                        R.id.numberOfDaysBeforeView ->
                            ReminderDaysBeforeDialog().show(dialogManager, "")
                        R.id.timeView -> ReminderTimeDialog.show(dialogManager, this)
                        R.id.tillDateView -> TillDateDialog.show(dialogManager, this)
                    }
                }

            }
            ulozListe(binding.list)
            binding.root
        }
    }

    /**
     * Jedna kolumna to zwykła lista przewijana w pionie.
     *
     * Dwie kolumny to wąż: dni idą w dół pierwszej kolumny, potem w dół drugiej,
     * a przewijanie odbywa się w bok. Robi to GridLayoutManager w orientacji
     * poziomej, w której `spanCount` oznacza liczbę WIERSZY, nie kolumn.
     *
     * Liczby wierszy nie da się wziąć z zasobów, bo zależy od tego, ile miejsca
     * zostaje po pasku tytułu i banerze — a wysokość banera znana jest dopiero po
     * jego wczytaniu. Stąd pomiar na gotowym widoku.
     */
    private fun ulozListe(lista: RecyclerView) {
        val kolumny = liczbaKolumn
        if (kolumny <= 1) {
            lista.layoutManager = LinearLayoutManager(requireContext())
            adapter?.szerokoscKolumny = ViewGroup.LayoutParams.MATCH_PARENT
            ItemTouchHelper(dayTouchHelperCallback(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT))
                .attachToRecyclerView(lista)
            return
        }
        ItemTouchHelper(dayTouchHelperCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN))
            .attachToRecyclerView(lista)
        // Nie doOnLayout: baner wczytuje się później i skraca listę, a wtedy liczba
        // wierszy policzona przy pierwszym układzie jest już nieaktualna i wiersze
        // ściskają się poniżej wysokości, jaką miały dostać.
        //
        // post jest konieczny: podmiana menedżera układu woła requestLayout, a ten
        // wywołany w trakcie trwającego przebiegu układu jest odkładany i już nie
        // wraca — lista zostaje bez menedżera i nie rysuje ani jednego wiersza.
        lista.addOnLayoutChangeListener { widok, _, _, _, _, _, _, _, _ ->
            widok.post { ulozWeza(widok as RecyclerView, kolumny) }
        }
    }

    @Suppress("NotifyDataSetChanged")
    private fun ulozWeza(lista: RecyclerView, kolumny: Int) {
        val dostepnaSzerokosc = lista.width - lista.paddingStart - lista.paddingEnd
        val dostepnaWysokosc = lista.height - lista.paddingTop - lista.paddingBottom
        if (dostepnaWysokosc <= 0 || dostepnaSzerokosc <= 0) return

        val minimalnaWysokoscWiersza = resources.getDimensionPixelSize(
            if (viewModel.isListCompact) R.dimen.day_row_min_height_compact
            else R.dimen.day_row_min_height
        )
        val wiersze = (dostepnaWysokosc / minimalnaWysokoscWiersza).coerceAtLeast(1)
        // Marginesy karty leżą poza jej szerokością, więc bez ich odjęcia dwie
        // kolumny nie mieszczą się w liście i druga wystaje poza ekran.
        val marginesy = 2 * resources.getDimensionPixelSize(R.dimen.spacing_s)
        val szerokosc = (dostepnaSzerokosc / kolumny - marginesy).coerceAtLeast(1)

        val menedzer = lista.layoutManager as? GridLayoutManager
        if (menedzer?.spanCount == wiersze && adapter?.szerokoscKolumny == szerokosc) return

        adapter?.szerokoscKolumny = szerokosc
        if (menedzer?.spanCount == wiersze) {
            adapter?.notifyDataSetChanged()
        } else {
            lista.layoutManager =
                GridLayoutManager(requireContext(), wiersze, GridLayoutManager.HORIZONTAL, false)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateDisplayedDataSetIfNeeded()
        odswiezPoZmianieGestosci()
    }

    /**
     * Gęstość jest odczytywana przy wiązaniu wiersza, więc po powrocie z ustawień
     * trzeba przerysować wszystkie widoczne wiersze. Zmiana dotyczy każdego z nich,
     * a nie żadnej pozycji z osobna — DiffUtil nie ma tu czego porównywać, bo dane
     * się nie zmieniły. Przy wężu zmienia się dodatkowo liczba wierszy na ekran.
     */
    @Suppress("NotifyDataSetChanged")
    private fun odswiezPoZmianieGestosci() {
        val gestosc = viewModel.isListCompact
        if (gestoscPrzyOstatnimRysowaniu != null && gestoscPrzyOstatnimRysowaniu != gestosc) {
            adapter?.notifyDataSetChanged()
            view?.findViewById<RecyclerView>(R.id.list)?.let { lista ->
                if (liczbaKolumn > 1) ulozWeza(lista, liczbaKolumn)
            }
        }
        gestoscPrzyOstatnimRysowaniu = gestosc
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }
}
