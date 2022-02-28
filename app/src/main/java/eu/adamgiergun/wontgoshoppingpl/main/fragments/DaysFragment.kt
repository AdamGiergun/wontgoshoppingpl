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
                density = requireContext().resources.displayMetrics.density
                logLook()
                events.observe(viewLifecycleOwner) {
                    binding.list.adapter = DaysRecyclerViewAdapter(this, viewLifecycleOwner)
                }
                idOfDialogToShow.observe(viewLifecycleOwner) { id ->
                    when (id) {
                        R.id.numberOfDaysBeforeView -> ReminderDaysBeforeDialog()
                        R.id.timeView -> ReminderTimeDialog()
                        R.id.tillDateView -> TillDateDialog()
                        else -> null
                    }?.show(requireActivity().supportFragmentManager, "")
                }

            }
            ItemTouchHelper(dayTouchHelperCallback).attachToRecyclerView(binding.list)
            binding.root
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateDisplayedDataSetIfNeeded()
    }
}