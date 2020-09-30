package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.databinding.DayListItemBinding
import eu.adamgiergun.wontgoshoppingpl.day.Day

internal class DaysRecyclerViewAdapter(private val viewModel: MainViewModel)
    : ListAdapter<Day, DaysRecyclerViewAdapter.DayViewHolder>(DayDiffCallback()) {

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
        }

        suspend fun deleteFromGoogleCalendar(appContext: Context) {
            binding.day?.deleteFromGoogleCalendar(appContext)
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