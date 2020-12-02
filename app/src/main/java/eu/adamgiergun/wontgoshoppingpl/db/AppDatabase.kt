package eu.adamgiergun.wontgoshoppingpl.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day
import java.util.concurrent.Executors

@Database(entities = [Event::class, Reminder::class], version = 5)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val rdc: Callback = object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    addEventsToDb(arrayOf(2020, 2021, 2022, 2023), context)
                }
            }

            val migration3to5: Migration = object : Migration(3, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    update6thDecember2020(database, context)
                }
            }

            val migration4to5: Migration = object : Migration(4, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    update6thDecember2020(database, context)
                }
            }

            val migration2to5: Migration = object : Migration(2, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    addEventsToDb(arrayOf(2021, 2022, 2023), context)
                }
            }

            val migration1to5: Migration = object : Migration(1, 5) {
                override fun migrate(database: SupportSQLiteDatabase) {
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
                            .addMigrations(migration4to5, migration3to5, migration2to5, migration1to5)
                            .fallbackToDestructiveMigration()
                            .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

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
            Executors.newSingleThreadScheduledExecutor().execute {
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