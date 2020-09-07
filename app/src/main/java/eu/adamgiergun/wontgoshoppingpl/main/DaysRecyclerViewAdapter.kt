package eu.adamgiergun.wontgoshoppingpl.main

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences
import eu.adamgiergun.wontgoshoppingpl.day.Day

class DaysRecyclerViewAdapter private constructor(private val days: Day.List, private val listener: DaysFragment.OnItemClickListener?) : RecyclerView.Adapter<DaysRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_day, parent, false) as CardView
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day, listener)
    }

    override fun getItemCount(): Int {
        return days.size
    }

    class ViewHolder(private val cardView: CardView) : RecyclerView.ViewHolder(cardView) {
        private val dayView: TextView = cardView.findViewById(R.id.date)
        private val timeView: TextView = cardView.findViewById(R.id.time)
        private val nameView: TextView = cardView.findViewById(R.id.name)
        private var day: Day? = null
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
                cardView.setBackgroundResource(day!!.backgroundResource)
            } else {
                cardView.setCardBackgroundColor(color)
                cardView.radius = 8 * density
                cardView.cardElevation = 4 * density
            }
        }

        private val color: Int
            get() {
                val colorRes = day!!.backgroundColorResource
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

        fun deleteFromGoogleCalendar(appContext: Context?) {
            day!!.deleteFromGoogleCalendar(appContext)
        }

    }

    companion object {
        private var isLookRich = false
        private var density = 0f
        fun newInstance(appContext: Context, listener: DaysFragment.OnItemClickListener?): DaysRecyclerViewAdapter {
            val daysRecyclerViewAdapter = DaysRecyclerViewAdapter(
                    Day.List.newInstance(appContext),
                    listener)
            isLookRich = AppPreferences.isLookRich(appContext)
            density = appContext.resources.displayMetrics.density
            return daysRecyclerViewAdapter
        }
    }
}