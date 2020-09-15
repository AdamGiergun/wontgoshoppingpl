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

    @Transaction
    @Query("SELECT * FROM events")
    fun getEventsWithReminders(): List<EventWithReminders>
}