package eu.adamgiergun.wontgoshoppingpl.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.main.compose.DaysScreen
import eu.adamgiergun.wontgoshoppingpl.main.compose.WgsTheme
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.ReminderDaysBeforeDialog
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.ReminderTimeDialog
import eu.adamgiergun.wontgoshoppingpl.main.dialogs.TillDateDialog
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel

/**
 * Gospodarz ekranu listy — jedyna wyspa Compose w aplikacji.
 *
 * Fragment zostaje, bo trzyma go graf nawigacji i bo dialogi wyboru daty
 * i godziny są nadal `DialogFragment`-ami pokazywanymi przez `FragmentManager`.
 * Cała treść ekranu przeniosła się do [DaysScreen].
 *
 * Co odpadło razem z warstwą widoków: ręczne układanie węża wraz z trzema
 * obejściami czasowymi, `DaysRecyclerViewAdapter`, `ColumnSnapHelper`,
 * `ItemTouchHelper`, `notifyDataSetChanged` przy zmianie gęstości i pole
 * pamiętające poprzednią gęstość.
 */
internal class DaysFragment : Fragment() {

    private val viewModel
        get() = ViewModelProvider(requireActivity())[DaysFragmentViewModel::class.java]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val model = viewModel
        model.logListDensity()

        return ComposeView(requireContext()).apply {
            // Bez tego kompozycja żyje dłużej niż widok fragmentu i po powrocie
            // z ustawień działałaby na nieaktualnym cyklu życia.
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WgsTheme {
                    DaysScreen(viewModel = model, scope = rememberCoroutineScope())
                }
            }
        }
    }

    // Dopiero tutaj, a nie w onCreateView: viewLifecycleOwner powstaje po zwróceniu
    // widoku i sięgnięcie po niego wcześniej kończy się wyjątkiem.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        podepnijDialogi(viewModel)
    }

    /**
     * Selektory Material są fragmentami, które przeżywają obrót ekranu bez swoich
     * nasłuchów — podpinamy je z powrotem przy odtworzeniu widoku. To jedyny
     * fragment starej mechaniki, który tu został; zniknie razem z przeniesieniem
     * dialogów na komponenty Compose.
     */
    private fun podepnijDialogi(model: DaysFragmentViewModel) {
        val dialogManager = requireActivity().supportFragmentManager
        ReminderTimeDialog.reconnectIfShown(dialogManager, model)
        TillDateDialog.reconnectIfShown(dialogManager, model)
        model.idOfDialogToShow.observe(viewLifecycleOwner) { id ->
            when (id) {
                R.id.numberOfDaysBeforeView -> ReminderDaysBeforeDialog().show(dialogManager, "")
                R.id.timeView -> ReminderTimeDialog.show(dialogManager, model)
                R.id.tillDateView -> TillDateDialog.show(dialogManager, model)
            }
        }
    }
}
