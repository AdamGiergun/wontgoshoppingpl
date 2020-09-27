package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.Embedded
import androidx.room.Relation

internal data class EventWithReminders(
        @Embedded val event: Event,
        @Relation(
                parentColumn = Event.COLUMN_ID,
                entityColumn = Reminder.COLUMN_EVENT_ID
        )
        val reminders: List<Reminder>
)