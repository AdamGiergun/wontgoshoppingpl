package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.*
import androidx.room.ForeignKey.NO_ACTION
import eu.adamgiergun.wontgoshoppingpl.db.Reminder.Companion.COLUMN_EVENT_ID
import eu.adamgiergun.wontgoshoppingpl.db.Reminder.Companion.TABLE_NAME

@Entity(
        tableName = TABLE_NAME,
        foreignKeys = [ForeignKey(
                entity = Event::class,
                onDelete = NO_ACTION,
                onUpdate = NO_ACTION,
                childColumns = [COLUMN_EVENT_ID],
                parentColumns = [Event.COLUMN_ID])]//,
        //indices = [Index("event_id")]
)
internal data class Reminder(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Int?,
        @ColumnInfo(name = COLUMN_EVENT_ID) val eventId: Int,
        val hour: Int,
        val minute: Int,
        @ColumnInfo(name = COLUMN_DAYS_BEFORE) val daysBefore: Int
) {
    companion object {
        const val TABLE_NAME = "events_reminders"
        const val COLUMN_EVENT_ID = "event_id"
        const val COLUMN_ID = "_id"
        const val COLUMN_DAYS_BEFORE = "days_before"
    }
}