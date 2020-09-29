package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.*

@Dao
internal interface EventDao {

//    @Query("SELECT * FROM events")
//    fun getAllEvents(): LiveData<List<Event>>

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

    @Query("SELECT * FROM events ORDER BY item DESC LIMIT 1")
    suspend fun getLastEvent(): Event?

    @Insert
    fun insertAll(vararg events: Event)

    @Update
    suspend fun update(event: Event)

    suspend fun initialize() {
        var hash: Int
        do {
            hash = getLastEvent().hashCode()
        } while (hash == 0)
    }

//    @Transaction
//    @Query("SELECT * FROM events")
//    suspend fun getEventsWithReminders(): List<EventWithReminders>?
}