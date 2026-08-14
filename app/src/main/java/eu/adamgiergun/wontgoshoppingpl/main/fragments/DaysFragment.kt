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
import eu.adamgiergun.wontgoshoppingpl.main.compose.DaysScreen
import eu.adamgiergun.wontgoshoppingpl.main.compose.WgsTheme
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel

/**
 * Gospodarz ekranu listy — jedyna wyspa Compose w aplikacji.
 *
 * Fragment zostaje wyłącznie dlatego, że trzyma go graf nawigacji. Cała treść
 * ekranu wraz z dialogami przeniosła się do [DaysScreen].
 *
 * Co odpadło razem z warstwą widoków: ręczne układanie węża wraz z trzema
 * obejściami czasowymi, `DaysRecyclerViewAdapter`, `ColumnSnapHelper`,
 * `ItemTouchHelper`, `notifyDataSetChanged` przy zmianie gęstości, pole
 * pamiętające poprzednią gęstość, sześć klas dialogów i — razem z nimi —
 * podpinanie nasłuchów selektorów po obrocie ekranu.
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
}
