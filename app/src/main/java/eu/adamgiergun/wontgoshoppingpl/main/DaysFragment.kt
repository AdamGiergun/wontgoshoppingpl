package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
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
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.FragmentDayListBinding
import eu.adamgiergun.wontgoshoppingpl.day.Day
import kotlinx.coroutines.launch

class DaysFragment : Fragment() {
    private lateinit var recyclerViewAdapter: DaysRecyclerViewAdapter
    private var listener: OnItemClickListener? = null

    private val dayTouchHelperCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            lifecycleScope.launch {
                val dayViewHolder = viewHolder as DaysRecyclerViewAdapter.ViewHolder
                dayViewHolder.deleteFromGoogleCalendar(context!!.applicationContext)
                recyclerViewAdapter.notifyItemChanged(viewHolder.adapterPosition)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentDayListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_day_list, container, false)
        binding.lifecycleOwner = this
        recyclerViewAdapter = DaysRecyclerViewAdapter.newInstance(requireContext().applicationContext, listener)
        binding.list.adapter = recyclerViewAdapter
        attachItemTouchHelper(binding.list)

        val mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        //binding.viewModel = mainViewModel
        mainViewModel.days.observe(viewLifecycleOwner, {
            it?.let {
                recyclerViewAdapter.submitList(it)
            }
        })
        return binding.root
    }

    private fun attachItemTouchHelper(recyclerView: RecyclerView?) {
        val dayTouchHelper = ItemTouchHelper(dayTouchHelperCallback)
        dayTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is OnItemClickListener) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement onItemClickListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnItemClickListener {
        fun onItemClick(day: Day)
    }

    companion object {
        fun newInstance(): DaysFragment {
            return DaysFragment()
        }
    }
}