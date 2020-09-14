package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate

@Entity(tableName = "events")
data class Event(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "type_id") val typeId: Int,
        val item: String,
        @ColumnInfo(name = "calendar_id") val calendarId: String,
        @ColumnInfo(name = "calendar_event_id") val calendarEventId: Long
) : Comparable<Event> {

    override fun compareTo(other: Event): Int {
        return hashCode() - other.hashCode()
    }

    override fun hashCode(): Int {
        val pieces = item.split(",")
        return if (pieces[0].startsWith("2019")) {
            SimpleDate.newInstance(pieces[0], SimpleDate.ORDER_YDM.toInt()).hashCode()
        } else {
            SimpleDate.newInstance(pieces[0], SimpleDate.ORDER_YMD.toInt()).hashCode()
        }
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other.hashCode()
    }
}