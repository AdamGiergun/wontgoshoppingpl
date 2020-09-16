package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.NO_ACTION
import androidx.room.PrimaryKey

@Entity(
        tableName = "events_reminders",
        foreignKeys = [ForeignKey(
                entity = Event::class,
                onDelete = NO_ACTION,
                onUpdate = NO_ACTION,
                childColumns = ["event_id"],
                parentColumns = ["_id"])]
)
data class Reminder(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id")  val id: Int?,
        @ColumnInfo(name = COLUMN_EVENT_ID_NAME) val eventId: Int,
        val hour: Int,
        val minute: Int,
        @ColumnInfo(name = "days_before") val daysBefore: Int
) {
    companion object {
        const val COLUMN_EVENT_ID_NAME = "event_id"
    }
}