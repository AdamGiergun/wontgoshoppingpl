package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.Embedded
import androidx.room.Relation

data class EventWithReminders(
        @Embedded val event: Event,

        @Relation(
                parentColumn = Event.COLUMN_ID_NAME,
                entityColumn = Reminder.COLUMN_EVENT_ID_NAME
        )
        val reminders: List<Reminder>
)