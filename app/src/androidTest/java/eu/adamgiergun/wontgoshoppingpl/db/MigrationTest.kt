package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migracje bazy.
 *
 * Test instrumentalny, bo `MigrationTestHelper` potrzebuje prawdziwego SQLite —
 * atrapa nie odtworzy ani indeksów, ani kluczy obcych, ani zachowania kursora
 * na pustym wyniku.
 *
 * Stawką jest utrata danych, nie wygoda. Baza jest budowana z
 * `fallbackToDestructiveMigration(dropAllTables = true)`, więc migracja, która
 * **rzuci wyjątkiem**, kończy się skasowaniem wszystkiego, co użytkownik ma:
 * powiązań z Kalendarzem Google i przypomnień. Objaw po stronie użytkownika to
 * nie awaria, tylko cicho pusta lista i przypomnienia, które przestały działać.
 *
 * Stąd nacisk nie na „czy dane się przeniosły", ale na „czy migracja przeszła
 * bez wyjątku także wtedy, gdy baza wygląda inaczej, niż zakładał autor".
 *
 * Wersje 1, 2 i 3 nie mają wyeksportowanych schematów (`app/schemas` zaczyna się
 * od 4), więc ich ścieżek nie da się tu sprawdzić bez ręcznego odtwarzania
 * struktury sprzed lat. Świadome ograniczenie.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val nazwaBazy = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun SupportSQLiteDatabase.wstawDzien(id: Int, item: String, typ: Int, kalendarz: String? = null, idWKalendarzu: Long? = null) {
        execSQL(
            "INSERT INTO ${Event.TABLE_NAME} " +
                    "(${Event.COLUMN_ID}, ${Event.COLUMN_TYPE_ID}, ${Event.COLUMN_ITEM}, " +
                    "${Event.COLUMN_CALENDAR_ID}, ${Event.COLUMN_CALENDAR_EVENT_ID}) " +
                    "VALUES ($id, $typ, '$item', " +
                    "${kalendarz?.let { "'$it'" } ?: "NULL"}, ${idWKalendarzu ?: "NULL"})"
        )
    }

    private fun SupportSQLiteDatabase.wstawPrzypomnienie(idDnia: Int) {
        execSQL(
            "INSERT INTO ${Reminder.TABLE_NAME} " +
                    "(${Reminder.COLUMN_EVENT_ID}, hour, minute, ${Reminder.COLUMN_DAYS_BEFORE}) " +
                    "VALUES ($idDnia, 10, 0, 1)"
        )
    }

    private fun SupportSQLiteDatabase.policz(sql: String): Int =
        query(sql).use { it.moveToFirst(); it.getInt(0) }

    private fun migruj(od: Int, doWersji: Int): SupportSQLiteDatabase =
        helper.runMigrationsAndValidate(
            nazwaBazy, doWersji, true, *AppDatabase.migrations(context)
        )

    // ---------- ścieżka pełna ----------

    @Test
    fun migracja_4_do_6_zachowuje_dane_uzytkownika() {
        helper.createDatabase(nazwaBazy, 4).use { db ->
            db.wstawDzien(1, "2020_12_06,00:00,Niedziela handlowa", 0, "5", 12345L)
            db.wstawDzien(2, "2026_08_16,00:00,non_commerce_sunday", 0, "5", 6789L)
            db.wstawPrzypomnienie(2)
        }

        migruj(4, 6).use { db ->
            // Powiązanie z Kalendarzem Google to jedyna rzecz w tej bazie, której
            // nie da się odtworzyć z zasobów ani z Remote Config.
            db.query(
                "SELECT ${Event.COLUMN_CALENDAR_EVENT_ID} FROM ${Event.TABLE_NAME} " +
                        "WHERE ${Event.COLUMN_ID} = 2"
            ).use { kursor ->
                assertThat(kursor.moveToFirst(), `is`(true))
                assertThat(kursor.getLong(0), `is`(6789L))
            }
            assertThat(db.policz("SELECT COUNT(*) FROM ${Reminder.TABLE_NAME} WHERE ${Reminder.COLUMN_EVENT_ID} = 2"), `is`(1))
        }
    }

    @Test
    fun migracja_5_do_6_zaklada_indeks_na_kolumnie_item() {
        helper.createDatabase(nazwaBazy, 5).use { db ->
            db.wstawDzien(1, "2026_08_16,00:00,non_commerce_sunday", 0)
        }

        migruj(5, 6).use { db ->
            val indeksy = db.policz(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'index' " +
                        "AND name = 'index_${Event.TABLE_NAME}_${Event.COLUMN_ITEM}'"
            )
            assertThat(indeksy, `is`(1))
        }
    }

    @Test
    fun migracja_5_do_6_dosiewa_dane_z_zasobow_nie_ruszajac_istniejacych() {
        helper.createDatabase(nazwaBazy, 5).use { db ->
            // Wiersz sprzed wersji 6: w trzecim polu gotowa nazwa, nie klucz.
            db.wstawDzien(1, "2026_08_16,00:00,Niedziela niehandlowa", 0, "5", 6789L)
        }

        migruj(5, 6).use { db ->
            // Dzień już obecny zostaje nietknięty razem z powiązaniem do kalendarza.
            db.query(
                "SELECT ${Event.COLUMN_ITEM}, ${Event.COLUMN_CALENDAR_EVENT_ID} " +
                        "FROM ${Event.TABLE_NAME} WHERE ${Event.COLUMN_ITEM} LIKE '2026_08_16,%'"
            ).use { kursor ->
                assertThat(kursor.count, `is`(1))
                kursor.moveToFirst()
                assertThat(kursor.getString(0), `is`("2026_08_16,00:00,Niedziela niehandlowa"))
                assertThat(kursor.getLong(1), `is`(6789L))
            }
            // A reszta roku dochodzi z zasobów.
            assertThat(db.policz("SELECT COUNT(*) FROM ${Event.TABLE_NAME}") > 1, `is`(true))
        }
    }

    // ---------- ścieżki, na których wcześniej ginęły dane ----------

    @Test
    fun migracja_4_do_5_nie_wywraca_sie_gdy_brakuje_dnia_z_grudnia_2020() {
        // Regresja. `update6thDecember2020` ignorowało wynik `moveToFirst()`
        // i sięgało po kolumnę pustego kursora, co kończyło się wyjątkiem,
        // a przez fallback — skasowaniem całej bazy użytkownika.
        helper.createDatabase(nazwaBazy, 4).use { db ->
            db.wstawDzien(1, "2026_08_16,00:00,non_commerce_sunday", 0, "5", 6789L)
            db.wstawPrzypomnienie(1)
        }

        migruj(4, 6).use { db ->
            assertThat(db.policz("SELECT COUNT(*) FROM ${Event.TABLE_NAME} WHERE ${Event.COLUMN_ID} = 1"), `is`(1))
            assertThat(db.policz("SELECT COUNT(*) FROM ${Reminder.TABLE_NAME}"), `is`(1))
        }
    }

    @Test
    fun migracja_4_do_5_poprawia_dzien_z_grudnia_2020_gdy_jest_obecny() {
        helper.createDatabase(nazwaBazy, 4).use { db ->
            db.wstawDzien(1, "2020_12_06,00:00,cokolwiek", 0)
        }

        migruj(4, 6).use { db ->
            db.query(
                "SELECT ${Event.COLUMN_ITEM} FROM ${Event.TABLE_NAME} WHERE ${Event.COLUMN_ID} = 1"
            ).use { kursor ->
                kursor.moveToFirst()
                // Wartość bierze się z tablicy zasobów, więc sprawdzamy tylko,
                // że wiersz został podmieniony na wpis z tej tablicy.
                assertThat(kursor.getString(0).startsWith("2020_12_06"), `is`(true))
                assertThat(kursor.getString(0), `is`(not("2020_12_06,00:00,cokolwiek")))
            }
        }
    }

    @Test
    fun pusta_baza_w_wersji_4_migruje_sie_bez_wyjatku() {
        // Skrajny przypadek tej samej klasy: żadnego wiersza, więc każdy kursor
        // w migracjach jest pusty.
        helper.createDatabase(nazwaBazy, 4).close()

        migruj(4, 6).use { db ->
            assertThat(db.policz("SELECT COUNT(*) FROM ${Event.TABLE_NAME}") > 0, `is`(true))
        }
    }

    private fun not(wartosc: String) = org.hamcrest.CoreMatchers.not(wartosc)
}
