package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleDate

@Entity(tableName = Event.TABLE_NAME)
internal data class Event(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Int?,
        @ColumnInfo(name = COLUMN_TYPE_ID) val typeId: Int,
        val item: String,
        @ColumnInfo(name = COLUMN_CALENDAR_ID) var calendarId: String?,
        @ColumnInfo(name = COLUMN_CALENDAR_EVENT_ID) var calendarEventId: Long?
) : Comparable<Event> {

    override fun compareTo(other: Event): Int {
        return hashCode() - other.hashCode()
    }

    override fun hashCode(): Int {
        val pieces = item.split(",".toRegex()).toTypedArray()
        val date = SimpleDate.newInstance(pieces[0], SimpleDate.ORDER_YMD.toInt())
        return date.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other.hashCode()
    }

    companion object {
        const val TABLE_NAME = "events"
        const val COLUMN_ID = "_id"
        const val COLUMN_ITEM = "item"
        const val COLUMN_TYPE_ID = "type_id"
        const val COLUMN_CALENDAR_ID = "calendar_id"
        const val COLUMN_CALENDAR_EVENT_ID = "calendar_event_id"
    }
}