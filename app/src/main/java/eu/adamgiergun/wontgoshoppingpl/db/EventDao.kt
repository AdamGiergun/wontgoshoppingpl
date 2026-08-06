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

    /**
     * Szuka wpisu po samej dacie, bez godziny i klucza nazwy — to one mogą się
     * zmienić przy nowelizacji przepisów, a dzień pozostaje ten sam.
     */
    @Query("SELECT * FROM events WHERE item LIKE :date || ',%' LIMIT 1")
    suspend fun findByDate(date: String): Event?

    @Insert
    suspend fun insert(event: Event)

    @Query("UPDATE events SET type_id = :typeId, item = :item WHERE _id = :id")
    suspend fun updateTypeAndItem(id: Int, typeId: Int, item: String)

    /**
     * Nakłada nowe dane na bazę, nie ruszając wpisów, które się nie zmieniły.
     *
     * Celowo bez kasowania i wstawiania od nowa: [Reminder] trzyma klucz obcy do
     * [Event.id], więc podmiana wiersza zerwałaby przypomnienia i powiązania
     * z wydarzeniami w kalendarzu Google. Wpisy istniejące są aktualizowane
     * w miejscu, z zachowaniem identyfikatora.
     *
     * @return liczba dopisanych i zmienionych wpisów
     */
    @Transaction
    suspend fun upsertAll(events: List<Event>): Int {
        var affected = 0
        for (event in events) {
            val date = event.item.substringBefore(',')
            val existing = findByDate(date)
            when {
                existing == null -> {
                    insert(event)
                    affected++
                }
                existing.item != event.item || existing.typeId != event.typeId -> {
                    updateTypeAndItem(existing.id!!, event.typeId, event.item)
                    affected++
                }
            }
        }
        return affected
    }

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