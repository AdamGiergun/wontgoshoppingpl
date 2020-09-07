package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day

class DaysFragment : Fragment() {
    private var listener: OnItemClickListener? = null
    private var daysRecyclerView: RecyclerView? = null
    private val dayTouchHelperCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val dayViewHolder = viewHolder as DaysRecyclerViewAdapter.ViewHolder
            dayViewHolder.deleteFromGoogleCalendar(context!!.applicationContext)
            setRecyclerViewAdapter()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_day_list, container, false)
        if (view is RecyclerView) {
            daysRecyclerView = view
            daysRecyclerView!!.layoutManager = LinearLayoutManager(context)
            setRecyclerViewAdapter()
            attachItemTouchHelper(daysRecyclerView)
        }
        return view
    }

    fun setRecyclerViewAdapter() {
        val daysRecyclerViewAdapter = DaysRecyclerViewAdapter.newInstance(requireContext().applicationContext, listener)
        daysRecyclerView!!.adapter = daysRecyclerViewAdapter
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
        fun onItemClick(day: Day?)
    }

    companion object {
        fun newInstance(): DaysFragment {
            return DaysFragment()
        }
    }
}