package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.databinding.DayListItemBinding
import eu.adamgiergun.wontgoshoppingpl.day.Day

class DaysRecyclerViewAdapter private constructor(private val listener: DaysFragment.OnItemClickListener?) : ListAdapter<Day, DaysRecyclerViewAdapter.DayViewHolder>(DayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        return DayViewHolder.from(parent)
    }

    override fun onBindViewHolder(holderDay: DayViewHolder, position: Int) {
        val day = getItem(position)
        holderDay.bind(day!!, listener)
    }

    class DayViewHolder(binding: DayListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dayView = binding.date //: TextView = cardView.findViewById(R.id.date)
        private val timeView = binding.time //: TextView = cardView.findViewById(R.id.time)
        private val nameView = binding.name // TextView = cardView.findViewById(R.id.name)
        private val cardView = binding.cardView

        private lateinit var day: Day

        fun bind(day: Day, listener: DaysFragment.OnItemClickListener?) {
            setup(day)
            cardView.setOnClickListener { listener?.onItemClick(day) }
        }

        private fun setup(day: Day) {
            this.day = day
            dayView.text = day.dateAndWeekdayString
            timeView.text = day.timeDescription
            nameView.text = day.name
            setupCardView()
        }

        private fun setupCardView() {
            if (isLookRich) {
                cardView.setBackgroundResource(day.backgroundResource)
            } else {
                cardView.setCardBackgroundColor(color)
                cardView.radius = 8 * density
                cardView.cardElevation = 4 * density
            }
        }

        private val color: Int
            get() {
                val colorRes = day.backgroundColorResource
                val resources = cardView.context.resources
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    resources.getColor(colorRes, null)
                } else {
                    @Suppress("DEPRECATION")
                    resources.getColor(colorRes)
                }
            }

        override fun toString(): String {
            return super.toString() + " '" + timeView.text + "'"
        }

        suspend fun deleteFromGoogleCalendar(appContext: Context?) {
            day.deleteFromGoogleCalendar(appContext!!)
        }

        companion object {
            fun from(parent: ViewGroup): DayViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = DayListItemBinding.inflate(layoutInflater, parent, false)
                return DayViewHolder(binding)
            }
        }
    }

    companion object {
        private var isLookRich = false
        private var density = 0f
        fun newInstance(appContext: Context, listener: DaysFragment.OnItemClickListener?): DaysRecyclerViewAdapter {
            val daysRecyclerViewAdapter = DaysRecyclerViewAdapter(listener)
            isLookRich = AppPreferences.isLookRich(appContext)
            density = appContext.resources.displayMetrics.density
            return daysRecyclerViewAdapter
        }
    }
}

class DayDiffCallback : DiffUtil.ItemCallback<Day>() {

    override fun areItemsTheSame(oldItem: Day, newItem: Day): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }

    override fun areContentsTheSame(oldItem: Day, newItem: Day): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }
}