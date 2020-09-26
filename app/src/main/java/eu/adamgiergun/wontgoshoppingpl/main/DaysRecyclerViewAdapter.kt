package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.databinding.DayListItemBinding
import eu.adamgiergun.wontgoshoppingpl.day.Day

internal class DaysRecyclerViewAdapter(
        private val listener: DaysFragment.OnItemClickListener?,
        private val isLookRich: Boolean,
        private val density: Float
)
    : ListAdapter<Day, DaysRecyclerViewAdapter.DayViewHolder>(DayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        return DayViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderDay: DayViewHolder, position: Int) {
        val day = getItem(position)
        holderDay.bind(day!!, listener, isLookRich, density)
    }

    internal class DayViewHolder private constructor(private val binding: DayListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var day: Day

        fun bind(item: Day, listener: DaysFragment.OnItemClickListener?, isLookRich: Boolean, density: Float) {
            day = item
            binding.day = item
            binding.isLookRich = isLookRich
            binding.density = density
            binding.clickListener = listener
        }

        suspend fun deleteFromGoogleCalendar(appContext: Context) {
            day.deleteFromGoogleCalendar(appContext)
        }

        companion object {
            fun from(parent: ViewGroup): DayViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DayListItemBinding.inflate(layoutInflater, parent, false)
                return DayViewHolder(binding)
            }
        }
    }

    private class DayDiffCallback : DiffUtil.ItemCallback<Day>() {

        override fun areItemsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Day, newItem: Day): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }
}