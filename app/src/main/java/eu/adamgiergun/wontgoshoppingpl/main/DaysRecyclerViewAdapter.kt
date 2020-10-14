package eu.adamgiergun.wontgoshoppingpl.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.databinding.DayListItemBinding
import eu.adamgiergun.wontgoshoppingpl.day.Day

internal class DaysRecyclerViewAdapter(private val viewModel: MainViewModel, lifecycleOwner: LifecycleOwner)
    : ListAdapter<Day, DaysRecyclerViewAdapter.DayViewHolder>(DayDiffCallback()) {

    init {
        viewModel.days.observe(lifecycleOwner, { submitList(it) })
        viewModel.isDbChanged.observe(lifecycleOwner, {
            if (viewModel.isActionAddList) {
                viewModel.resetDayTypes()
                notifyDataSetChanged()
            } else {
                notifyItemChanged(viewModel.lastTouchedPosition)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        return DayViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderDay: DayViewHolder, position: Int) {
        val day = getItem(position)
        holderDay.bind(day, viewModel)
    }

    internal class DayViewHolder private constructor(private val binding: DayListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: Day, viewModel: MainViewModel) {
            binding.day = day
            binding.viewModel = viewModel
            binding.position = adapterPosition
        }

        suspend fun deleteFromGoogleCalendar() {
            binding.day?.let {day ->
                day.deleteFromGoogleCalendar(binding.cardView.context)
                if (!day.isAddedToCalendar) {
                    binding.viewModel?.run {
                        setLastTouchedItem(null, adapterPosition, true)
                    }
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): DayViewHolder {
                return DayViewHolder(
                        DayListItemBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false))
            }
        }
    }

    private class DayDiffCallback : DiffUtil.ItemCallback<Day>() {

        override fun areItemsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem.hashCode() == newItem.hashCode() && oldItem.calendarEventId == newItem.calendarEventId
        }
    }
}