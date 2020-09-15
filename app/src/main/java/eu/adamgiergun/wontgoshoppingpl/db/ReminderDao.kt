package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReminderDao {

    @Query("SELECT * FROM events_reminders WHERE _id IS :eventId")
    fun loadAllByEventId(eventId: Int): List<Reminder>

    @Insert
    fun insertAll(vararg reminders: Reminder)
}