package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.*

@Dao
interface EventDao {
    @Query("SELECT * FROM " + Event.TABLE_NAME)
    fun getAll(): List<Event>?

    @Query("SELECT * FROM " + Event.TABLE_NAME + " WHERE " + Event.COLUMN_TYPE_ID + " IN (:listOfTypeIds)")
    fun getAllByTypes(listOfTypeIds: String): List<Event>?

    /**
     * gets @param [date] formatted as yyyy_mm_dd
     */
    @Query("SELECT * FROM " + Event.TABLE_NAME + " WHERE " + Event.COLUMN_ITEM + ">=:date ORDER BY item")
    suspend fun getAllEventsNotBefore(date: String): List<Event>?

    /**
     * gets @param [date] formatted as yyyy_mm_dd
     */
    //@Query("SELECT * FROM " + Event.TABLE_NAME + " WHERE " + Event.COLUMN_ITEM + ">=:date AND " + Event.COLUMN_TYPE_ID + " IN (:listOfTypeIds) ORDER BY item")
    @Query("SELECT * FROM events WHERE item>=:date AND type_id IN (:listOfTypeIds) ORDER BY item")
    suspend fun getAllEventsOfTypeNotBefore(listOfTypeIds: List<String>, date: String): List<Event>?

    /**
     * gets @param [date] formatted as yyyy_mm_dd
     */
    @Query("SELECT * FROM " + Event.TABLE_NAME + " WHERE " + Event.COLUMN_ITEM + ">=:date ORDER BY item LIMIT 1")
    suspend fun getFirstEventNotBefore(date: String): Event?

    @Insert
    fun insertAll(vararg events: Event)

    @Update
    fun update(event: Event)

    @Transaction
    @Query("SELECT * FROM " + Event.TABLE_NAME)
    fun getEventsWithReminders(): List<EventWithReminders>?
}