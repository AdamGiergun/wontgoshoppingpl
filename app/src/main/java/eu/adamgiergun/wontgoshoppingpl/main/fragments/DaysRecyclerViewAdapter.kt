package eu.adamgiergun.wontgoshoppingpl.main.fragments

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.databinding.DayListItemBinding
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.Event
import eu.adamgiergun.wontgoshoppingpl.main.utils.setAddedToCalendar
import eu.adamgiergun.wontgoshoppingpl.main.utils.setLook
import eu.adamgiergun.wontgoshoppingpl.main.utils.setTimeDescription
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel

@SuppressLint("NotifyDataSetChanged")
internal class DaysRecyclerViewAdapter(private val viewModel: DaysFragmentViewModel, lifecycleOwner: LifecycleOwner)
    : ListAdapter<Event, DaysRecyclerViewAdapter.DayViewHolder>(EventDiffCallback()) {

    init {
        submitList(viewModel.events.value)
        viewModel.isDbChanged.observe(lifecycleOwner) {
            if (it) {
                if (viewModel.lastActionIsAddList) {
                    viewModel.updateDisplayedDataSetIfNeeded()
                    notifyDataSetChanged()
                } else {
                    notifyItemChanged(viewModel.lastTouchedPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        return DayViewHolder.from(parent)
    }

    override fun onBindViewHolder(dayViewHolder: DayViewHolder, position: Int) {
        val event = getItem(position)
        dayViewHolder.bind(Day(event, dayViewHolder.itemView.context), viewModel)
    }

    internal class DayViewHolder private constructor(private val binding: DayListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private var day: Day? = null
        private var viewModel: DaysFragmentViewModel? = null

        fun bind(day: Day, viewModel: DaysFragmentViewModel) {
            this.day = day
            this.viewModel = viewModel
            val position = bindingAdapterPosition
            binding.run {
                cardView.setLook(day, viewModel.isLookRich, viewModel.density)
                cardView.setOnClickListener { viewModel.setLastTouchedItem(day, position, false) }
                date.text = day.dateAndWeekdayString
                date.setAddedToCalendar(day.isAddedToCalendar)
                time.setTimeDescription(day.allDay, day.time)
                name.text = day.name
            }
        }

        suspend fun deleteFromGoogleCalendar() {
            day?.let { day ->
                day.deleteFromGoogleCalendar(binding.cardView.context)
                if (!day.isAddedToCalendar) {
                    viewModel?.setLastTouchedItem(null, bindingAdapterPosition, true)
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

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {

        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.hashCode() == newItem.hashCode() && oldItem.calendarEventId == newItem.calendarEventId
        }
    }
}