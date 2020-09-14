package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events_reminders")
class Reminder(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "eid") val eventId: Int,
        val hour: Int,
        val minute: Int,
        @ColumnInfo(name = "days_before") val daysBefore: Int
)