package eu.adamgiergun.wontgoshoppingpl.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
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

    private val dayTouchHelperCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

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
            ItemTouchHelper(dayTouchHelperCallback).attachToRecyclerView(binding.list)
            binding.root
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
     * się nie zmieniły.
     */
    @Suppress("NotifyDataSetChanged")
    private fun odswiezPoZmianieGestosci() {
        val gestosc = viewModel.isListCompact
        if (gestoscPrzyOstatnimRysowaniu != null && gestoscPrzyOstatnimRysowaniu != gestosc) {
            adapter?.notifyDataSetChanged()
        }
        gestoscPrzyOstatnimRysowaniu = gestosc
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }
}