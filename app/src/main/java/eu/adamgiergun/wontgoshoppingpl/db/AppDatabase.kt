package eu.adamgiergun.wontgoshoppingpl.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Event::class, Reminder::class], version = 6)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val rdc: Callback = object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Świeża instalacja dostaje wyłącznie dane bieżące. Lat historycznych
                    // nie ma po co wstawiać: zapytania filtrują `item >= dziś`, więc i tak
                    // nigdy się nie pokażą.
                    seedFromResources(db, context)
                }
            }

            val migration5to6: Migration = object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS index_${Event.TABLE_NAME}_${Event.COLUMN_ITEM} " +
                                "ON ${Event.TABLE_NAME} (${Event.COLUMN_ITEM})"
                    )
                    seedFromResources(db, context)
                }
            }

            val migration3to5: Migration = object : Migration(3, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    update6thDecember2020(db, context)
                }
            }

            val migration4to5: Migration = object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    update6thDecember2020(db, context)
                }
            }

            val migration2to5: Migration = object : Migration(2, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    addEventsToDb(arrayOf(2021, 2022, 2023), context)
                }
            }

            val migration1to5: Migration = object : Migration(1, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    addEventsToDb(arrayOf(2020, 2021, 2022, 2023), context)
                }
            }

            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "events.db"
                    )
                            .addCallback(rdc)
                            .addMigrations(
                                migration5to6,
                                migration4to5, migration3to5, migration2to5, migration1to5
                            )
                            .fallbackToDestructiveMigration(dropAllTables = true)
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        /**
         * Wstawia dane wbudowane w aplikację. To tylko punkt startowy — właściwym
         * źródłem są dane zdalne, patrz [eu.adamgiergun.wontgoshoppingpl.common.DaysRepository].
         *
         * Świadomie synchronicznie, na bazie przekazanej przez Room, a nie przez DAO
         * w osobnej korutynie jak [addEventsToDb]: tamten wariant kończył się pustą
         * listą przy pierwszym uruchomieniu po aktualizacji, bo ekran zdążył odpytać
         * bazę, zanim wstawianie się wykonało. Tutaj wszystko dzieje się w transakcji
         * migracji, więc po jej zakończeniu dane po prostu są.
         */
        private fun seedFromResources(db: SupportSQLiteDatabase, context: Context) {
            val events = DaysPayload.fromResources(context)
            if (events.isEmpty()) return

            // Dni już obecne w bazie zostają nietknięte: [Reminder] trzyma klucz obcy
            // do [Event.id], więc podmiana wiersza zerwałaby przypomnienia użytkownika
            // i powiązania z wydarzeniami w kalendarzu Google.
            val knownDates = mutableSetOf<String>()
            db.query("SELECT ${Event.COLUMN_ITEM} FROM ${Event.TABLE_NAME}").use { cursor ->
                while (cursor.moveToNext()) {
                    knownDates.add(cursor.getString(0).substringBefore(','))
                }
            }

            for (event in events) {
                if (!knownDates.add(event.item.substringBefore(','))) continue
                db.insert(
                    Event.TABLE_NAME,
                    SQLiteDatabase.CONFLICT_IGNORE,
                    ContentValues().apply {
                        put(Event.COLUMN_TYPE_ID, event.typeId)
                        put(Event.COLUMN_ITEM, event.item)
                    }
                )
            }
        }

        /**
         * Wstawia lata 2020-2023 z tablic w zasobach. Używane wyłącznie przez migracje
         * ze starych wersji bazy — nowe dane idą przez [seedFromResources].
         */
        private fun addEventsToDb(years: Array<Int>, context: Context) {
            var array = emptyArray<Event>()
            for (year in years) {
                getArrays(year).forEach { arrayId ->
                    val eventsArray = context.resources.getStringArray(arrayId).map {
                        Event(null, getEventTypeId(arrayId), it, null, null)
                    }
                    array = array.plus(eventsArray)
                }
            }
            array.sort()
            // Celowo asynchronicznie: wołane z Callback.onCreate i z migracji, czyli w trakcie
            // budowania instancji — wstawianie musi poczekać, aż `build()` zwróci sterowanie.
            CoroutineScope(Dispatchers.IO).launch {
                getInstance(context).eventDao().insertAll(*array)
            }
        }

        private fun update6thDecember2020(db: SupportSQLiteDatabase, context: Context) {
            db.query("SELECT * FROM events WHERE item LIKE '2020_12_06%' ORDER BY item LIMIT 1").use { cursor ->
                cursor.moveToFirst()
                val oldEvent = Event(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getLong(4)
                )

                val arrayId = R.array.commerce_sundays_2020
                val newEvent = context.resources.getStringArray(arrayId).filter {
                    it.startsWith("2020_12_06")
                }.map {
                    Event(oldEvent.id, getEventTypeId(arrayId), it, null, null)
                }.first()

                val query = "UPDATE ${Event.TABLE_NAME} " +
                        "SET ${Event.COLUMN_TYPE_ID} = ${newEvent.typeId}, ${Event.COLUMN_ITEM} = '${newEvent.item}' " +
                        "WHERE ${Event.COLUMN_ID} = ${newEvent.id} "
                db.execSQL(query)
            }
        }

        private fun getArrays(year: Int): Array<Int> {
            return when (year) {
                2020 ->
                    arrayOf(R.array.non_commerce_days_2020, R.array.commerce_sundays_2020, R.array.holidays_2020)
                2021 ->
                    arrayOf(R.array.non_commerce_days_2021, R.array.commerce_sundays_2021, R.array.holidays_2021)
                2022 ->
                    arrayOf(R.array.non_commerce_days_2022, R.array.commerce_sundays_2022, R.array.holidays_2022)
                2023 ->
                    arrayOf(R.array.non_commerce_days_2023, R.array.commerce_sundays_2023, R.array.holidays_2023)
                else ->
                    emptyArray()
            }
        }

        private fun getEventTypeId(idOfArray: Int): Int {
            return when (idOfArray) {
                R.array.holidays_2020, R.array.holidays_2021, R.array.holidays_2022, R.array.holidays_2023 ->
                    Day.Type.HOLIDAY.code
                R.array.non_commerce_days_2020, R.array.non_commerce_days_2021, R.array.non_commerce_days_2022, R.array.non_commerce_days_2023 ->
                    Day.Type.NON_COMMERCE_DAY.code
                R.array.commerce_sundays_2020, R.array.commerce_sundays_2021, R.array.commerce_sundays_2022, R.array.commerce_sundays_2023 ->
                    Day.Type.COMMERCE_SUNDAY.code
                else ->
                    -1
            }
        }
    }
}