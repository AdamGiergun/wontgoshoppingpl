package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.*

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAll(): List<Event>

    @Insert
    fun insertAll(vararg events: Event)

    @Update
    fun update(event: Event)

    @Query("SELECT * FROM events_reminders WHERE id IS :eventId")
    fun loadAllByEventId(eventId: Int): List<Reminder>

    @Insert
    fun insertAll(vararg reminders: Reminder)

    @Transaction
    @Query("SELECT * FROM events")
    fun getEventsWithReminders(): List<EventWithReminders>
}