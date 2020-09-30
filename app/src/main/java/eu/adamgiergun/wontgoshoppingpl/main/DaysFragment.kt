package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.databinding.FragmentDayListBinding
import kotlinx.coroutines.launch

internal class DaysFragment : Fragment() {

    private val dayTouchHelperCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    lifecycleScope.launch {
                        (viewHolder as DaysRecyclerViewAdapter.DayViewHolder).let { dayViewHolder ->
                            context?.let { context ->
                                dayViewHolder.deleteFromGoogleCalendar(context)
                                DataBindingUtil.getBinding<FragmentDayListBinding>(this@DaysFragment.requireView())?.let { binding ->
                                    binding.list.adapter?.notifyItemChanged(dayViewHolder.adapterPosition)
                                }
                            }
                        }
                    }
                }
            }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentDayListBinding.inflate(inflater).let { binding ->
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java).let { mainViewModel ->
                mainViewModel.density = requireContext().resources.displayMetrics.density
                DaysRecyclerViewAdapter(mainViewModel).let { recyclerViewAdapter ->
                    binding.list.adapter = recyclerViewAdapter
                    mainViewModel.days.observe(viewLifecycleOwner, { recyclerViewAdapter.submitList(it) })
                }
                mainViewModel.logLook()
            }
            ItemTouchHelper(dayTouchHelperCallback).attachToRecyclerView(binding.list)
            binding.root
        }
    }
}