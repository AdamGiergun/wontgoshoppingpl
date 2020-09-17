package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.*

@Dao
interface EventDao {

    /**
     * gets @param [date] formatted as yyyy_mm_dd
     */
    @Query("SELECT * FROM events WHERE item>=:date AND type_id IN (:listOfTypeIds) ORDER BY item")
    suspend fun getAllEventsOfTypeNotBefore(listOfTypeIds: List<String>, date: String): List<Event>?

    /**
     * gets @param [date] formatted as yyyy_mm_dd
     */
    @Query("SELECT * FROM events WHERE item>=:date ORDER BY item LIMIT 1")
    suspend fun getFirstEventNotBefore(date: String): Event?

    @Insert
    fun insertAll(vararg events: Event)

    @Update
    fun update(event: Event)

    @Transaction
    @Query("SELECT * FROM events")
    fun getEventsWithReminders(): List<EventWithReminders>?
}