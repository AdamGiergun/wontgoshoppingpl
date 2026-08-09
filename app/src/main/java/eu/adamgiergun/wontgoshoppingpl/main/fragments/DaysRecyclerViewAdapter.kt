package eu.adamgiergun.wontgoshoppingpl.main.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.databinding.DayListItemBinding
import eu.adamgiergun.wontgoshoppingpl.day.Day
import eu.adamgiergun.wontgoshoppingpl.db.Event
import eu.adamgiergun.wontgoshoppingpl.main.utils.setAddedToCalendar
import eu.adamgiergun.wontgoshoppingpl.main.utils.applyDayLook
import eu.adamgiergun.wontgoshoppingpl.main.utils.applyDayTextSize
import eu.adamgiergun.wontgoshoppingpl.main.utils.setTimeDescription
import eu.adamgiergun.wontgoshoppingpl.main.viewModel.DaysFragmentViewModel

internal class DaysRecyclerViewAdapter(private val viewModel: DaysFragmentViewModel, lifecycleOwner: LifecycleOwner)
    : ListAdapter<Event, DaysRecyclerViewAdapter.DayViewHolder>(EventDiffCallback()) {

    /**
     * Przy przewijaniu w pionie karta zajmuje całą szerokość listy. Przy wężu
     * przewijanym w bok szerokość jest osią przewijania, więc MATCH_PARENT dałby
     * jedną kolumnę na ekran — fragment podaje tu wyliczoną szerokość kolumny.
     */
    var szerokoscKolumny: Int = ViewGroup.LayoutParams.MATCH_PARENT

    init {
        // Bez submitList w tym miejscu — listę dostarcza fragment, obserwując events.
        viewModel.isDbChanged.observe(lifecycleOwner) {
            if (it) {
                if (viewModel.lastActionIsAddList) {
                    // Odświeżenie danych kończy się emisją events, a ta wywołuje
                    // submitList. notifyDataSetChanged byłoby tu nie tylko zbędne,
                    // ale i szkodliwe — rozjeżdża się z asynchronicznym DiffUtil.
                    viewModel.updateDisplayedDataSetIfNeeded()
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
        dayViewHolder.bind(Day(event, dayViewHolder.itemView.context), viewModel, szerokoscKolumny)
    }

    internal class DayViewHolder private constructor(private val binding: DayListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private var day: Day? = null
        private var viewModel: DaysFragmentViewModel? = null

        fun bind(day: Day, viewModel: DaysFragmentViewModel, szerokoscKolumny: Int) {
            this.day = day
            this.viewModel = viewModel
            val position = bindingAdapterPosition
            binding.run {
                // Przy wężu wysokość komórki narzuca menedżer układu, więc duże
                // wypełnienie pionowe nie podnosi wiersza, tylko wypycha z niego
                // tekst. Gęstość steruje wtedy liczbą wierszy, nie wypełnieniem.
                val waz = szerokoscKolumny != ViewGroup.LayoutParams.MATCH_PARENT
                cardView.updateLayoutParams {
                    width = szerokoscKolumny
                    // Bez MATCH_PARENT karta zostaje przy swojej wysokości treści,
                    // a komórka jest wyższa — między wierszami robią się przepaści.
                    height = if (waz) ViewGroup.LayoutParams.MATCH_PARENT
                    else ViewGroup.LayoutParams.WRAP_CONTENT
                }
                val zwarty = viewModel.isListCompact || waz
                val kolorTresci = cardView.applyDayLook(day, zwarty)
                cardView.setOnClickListener { viewModel.setLastTouchedItem(day, position, false) }
                date.text = day.dateAndWeekdayString
                date.setTextColor(kolorTresci)
                date.applyDayTextSize(zwarty, R.dimen.day_text_date, R.dimen.day_text_date_compact)
                date.setAddedToCalendar(day.isAddedToCalendar, kolorTresci)
                time.setTimeDescription(day.allDay, day.time)
                time.setTextColor(kolorTresci)
                time.applyDayTextSize(zwarty, R.dimen.day_text_time, R.dimen.day_text_time_compact)
                name.text = day.name
                name.setTextColor(kolorTresci)
                name.applyDayTextSize(zwarty, R.dimen.day_text_name, R.dimen.day_text_name_compact)
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