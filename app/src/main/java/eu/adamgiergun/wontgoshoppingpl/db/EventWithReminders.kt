package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.Embedded
import androidx.room.Relation

data class EventWithReminders(
        @Embedded val event: Event,
        @Relation(
                parentColumn = "id",
                entityColumn = "eventId"
        )
        val reminders: List<Reminder>
)