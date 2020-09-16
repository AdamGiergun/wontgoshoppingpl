package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.*

@Dao
interface EventDao {
    @Query("SELECT * FROM " + Event.TABLE_NAME)
    fun getAll(): List<Event>?

    @Query("SELECT * FROM " + Event.TABLE_NAME + " WHERE " + Event.COLUMN_TYPE_ID + " IN (:listOfTypeIds)")
    fun getAllByTypes(listOfTypeIds: String): List<Event>?

    @Query("SELECT * FROM " + Event.TABLE_NAME + " ORDER BY item LIMIT 1")
    suspend fun getFirst(): Event?

    @Insert
    fun insertAll(vararg events: Event)

    @Update
    fun update(event: Event)

    @Transaction
    @Query("SELECT * FROM " + Event.TABLE_NAME)
    fun getEventsWithReminders(): List<EventWithReminders>?
}