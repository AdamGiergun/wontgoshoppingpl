package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import eu.adamgiergun.mykotlinlibrary.calendarmanagement.SimpleDate

// Indeks na `item` obsługuje zarówno zapytania zakresowe (`item >= :date`), jak
// i wyszukiwanie po dacie przy odświeżaniu danych. Celowo nieunikalny: baza
// użytkownika pochodzi z wielu starych wersji i nie ma gwarancji, że nie ma
// w niej duplikatów, a CREATE UNIQUE INDEX wywróciłby wtedy migrację.
@Entity(tableName = Event.TABLE_NAME, indices = [Index(value = [Event.COLUMN_ITEM])])
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

    // Tożsamość wpisu wyznacza sama data — tak samo jak hashCode i compareTo,
    // bo w jednym dniu jest najwyżej jedno wydarzenie. Sprawdzenie typu jest
    // konieczne: bez niego `other.hashCode()` rozwiązywało się do rozszerzenia
    // `Any?.hashCode()` i kolizja z obiektem dowolnej klasy dawała `true`.
    override fun equals(other: Any?): Boolean {
        return other is Event && hashCode() == other.hashCode()
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