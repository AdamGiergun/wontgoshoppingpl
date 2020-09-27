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

@Database(entities = [Event::class, Reminder::class], version = 4)
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

            val migration3to4: Migration = object : Migration(3, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    // Since we didn't alter the table, there's nothing else to do here.
                    // It's done only for marking migration to Room
                }
            }

            val migration2to4: Migration = object : Migration(2, 4) {
                override fun migrate(database: SupportSQLiteDatabase) {
                    addEventsToDb(arrayOf(2021, 2022, 2023), context)
                }
            }

            val migration1to4: Migration = object : Migration(1, 4) {
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
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            // Migration is not part of this lesson. You can learn more about
                            // migration with Room in this blog post:
                            // https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
                            .addCallback(rdc)
                            .addMigrations(migration3to4)
                            .addMigrations(migration2to4)
                            .addMigrations(migration1to4)
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