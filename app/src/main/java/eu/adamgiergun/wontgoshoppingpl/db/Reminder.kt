package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events_reminders")
data class Reminder(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id")  val id: Int,
        @ColumnInfo(name = COLUMN_EVENT_ID_NAME) val eventId: Int,
        val hour: Int,
        val minute: Int,
        @ColumnInfo(name = "days_before") val daysBefore: Int
) {
    companion object {
        const val COLUMN_EVENT_ID_NAME = "event_id"
    }
}