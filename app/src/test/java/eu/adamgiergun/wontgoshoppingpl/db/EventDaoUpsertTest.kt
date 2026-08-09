package eu.adamgiergun.wontgoshoppingpl.db

import androidx.room.Room
import eu.adamgiergun.wontgoshoppingpl.day.Day
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * `upsertAll` to jedyne miejsce, w którym dane zdalne z Remote Config spotykają
 * się z danymi użytkownika.
 *
 * Sedno jego istnienia jest przekorne: DAO **nie** kasuje i nie wstawia od nowa,
 * choć byłoby to prostsze. [Reminder] trzyma klucz obcy do [Event.id], więc
 * podmiana wiersza zerwałaby przypomnienia użytkownika i powiązania z
 * wydarzeniami w Kalendarzu Google. Testy pilnują właśnie tego: że identyfikator
 * przeżywa aktualizację i że nie rusza się nic, co się nie zmieniło.
 *
 * Baza w pamięci pod Robolectrikiem — logika siedzi w SQL-u, więc atrapa DAO
 * sprawdzałaby wyłącznie sama siebie.
 */
@RunWith(RobolectricTestRunner::class)
internal class EventDaoUpsertTest {

    private lateinit var baza: AppDatabase
    private lateinit var dao: EventDao

    @Before
    fun otworzBaze() {
        baza = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = baza.eventDao()
    }

    @After
    fun zamknijBaze() = baza.close()

    private fun wpis(item: String, typ: Day.Type) = Event(null, typ.code, item, null, null)

    private fun wszystkieWpisy(): List<Event> = runBlocking {
        dao.getAllEventsOfTypeNotBefore(listOf("0", "1", "2"), "0000_00_00").orEmpty()
    }

    private fun idWpisu(data: String): Int? = runBlocking { dao.findByDate(data)?.id }

    private fun policz(sql: String): Int =
        baza.openHelper.readableDatabase.query(sql).use { it.moveToFirst(); it.getInt(0) }

    // ---------- wstawianie ----------

    @Test
    fun `nieznana data zostaje dopisana i policzona`() = runBlocking {
        val zmienione = dao.upsertAll(
            listOf(wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY))
        )

        assertThat(zmienione, `is`(1))
        assertThat(wszystkieWpisy().size, `is`(1))
    }

    @Test
    fun `pusta lista nie zmienia niczego`() = runBlocking {
        dao.upsertAll(listOf(wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY)))

        assertThat(dao.upsertAll(emptyList()), `is`(0))
        assertThat(wszystkieWpisy().size, `is`(1))
    }

    // ---------- brak zmian ----------

    @Test
    fun `powtorzony import identycznych danych nie zmienia ani jednego wpisu`() = runBlocking {
        val dane = listOf(
            wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY),
            wpis("2026_08_16,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY),
        )
        dao.upsertAll(dane)

        // Remote Config odpytywany jest raz na dobę, więc ten sam payload
        // przychodzi wielokrotnie. Drugi przebieg musi być bezczynny — inaczej
        // każde uruchomienie aplikacji przepisywałoby całą bazę.
        assertThat(dao.upsertAll(dane), `is`(0))
        assertThat(wszystkieWpisy().size, `is`(2))
    }

    // ---------- aktualizacja w miejscu ----------

    @Test
    fun `zmiana typu dnia aktualizuje wpis zachowujac jego identyfikator`() = runBlocking {
        dao.upsertAll(listOf(wpis("2026_12_20,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY)))
        val idPrzed = idWpisu("2026_12_20")

        // Scenariusz z życia: nowelizacja przepisów zamienia niedzielę
        // niehandlową w handlową. Ten sam dzień, inny typ.
        val zmienione = dao.upsertAll(
            listOf(wpis("2026_12_20,00:00,commerce_sunday", Day.Type.COMMERCE_SUNDAY))
        )

        assertThat(zmienione, `is`(1))
        assertThat(wszystkieWpisy().size, `is`(1))
        assertThat(idWpisu("2026_12_20"), `is`(idPrzed))
        assertThat(wszystkieWpisy()[0].typeId, `is`(Day.Type.COMMERCE_SUNDAY.code))
        assertThat(wszystkieWpisy()[0].item, `is`("2026_12_20,00:00,commerce_sunday"))
    }

    @Test
    fun `zmiana samego klucza nazwy tez jest aktualizacja`() = runBlocking {
        dao.upsertAll(listOf(wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY)))

        val zmienione = dao.upsertAll(
            listOf(wpis("2026_08_15,00:00,assumption_of_mary", Day.Type.HOLIDAY))
        )

        assertThat(zmienione, `is`(1))
        assertThat(wszystkieWpisy()[0].item, `is`("2026_08_15,00:00,assumption_of_mary"))
    }

    @Test
    fun `stary wiersz z nazwa zamiast klucza jest aktualizowany, a nie dublowany`() = runBlocking {
        // Baza sprzed wersji 6 trzyma w trzecim polu gotowy tekst. Import musi
        // rozpoznać go po samej dacie i podmienić na klucz.
        dao.insert(wpis("2026_08_15,00:00,Wniebowzięcie Najświętszej Maryi Panny", Day.Type.HOLIDAY))
        val idPrzed = idWpisu("2026_08_15")

        dao.upsertAll(listOf(wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY)))

        assertThat(wszystkieWpisy().size, `is`(1))
        assertThat(idWpisu("2026_08_15"), `is`(idPrzed))
        assertThat(wszystkieWpisy()[0].item, `is`("2026_08_15,00:00,assumption"))
    }

    // ---------- przypomnienia użytkownika ----------

    @Test
    fun `przypomnienie uzytkownika przezywa aktualizacje dnia`() = runBlocking {
        dao.upsertAll(listOf(wpis("2026_12_20,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY)))
        val idDnia = idWpisu("2026_12_20")!!
        // ReminderDao nie ma dziś ani jednej metody, więc wiersz idzie wprost
        // do SQLite. Klucz obcy jest przez Room egzekwowany, więc samo wstawienie
        // sprawdza już, że dzień o tym identyfikatorze istnieje.
        baza.openHelper.writableDatabase.execSQL(
            "INSERT INTO ${Reminder.TABLE_NAME} " +
                    "(${Reminder.COLUMN_EVENT_ID}, hour, minute, ${Reminder.COLUMN_DAYS_BEFORE}) " +
                    "VALUES ($idDnia, 10, 0, 1)"
        )

        dao.upsertAll(listOf(wpis("2026_12_20,00:00,commerce_sunday", Day.Type.COMMERCE_SUNDAY)))

        // Bez tego sprawdzenia test przechodziłby pusto: gdyby wiersz
        // przypomnienia nigdy nie powstał, osieroconych też by nie było.
        assertThat(policz("SELECT COUNT(*) FROM ${Reminder.TABLE_NAME}"), `is`(1))

        // To jest powód, dla którego upsertAll nie kasuje i nie wstawia od nowa.
        // Klucz obcy ma onDelete = NO_ACTION, więc skasowanie dnia nie usunęłoby
        // przypomnienia, tylko zostawiło je wiszące w powietrzu.
        val osierocone = policz(
            "SELECT COUNT(*) FROM ${Reminder.TABLE_NAME} r " +
                    "LEFT JOIN ${Event.TABLE_NAME} e ON r.${Reminder.COLUMN_EVENT_ID} = e.${Event.COLUMN_ID} " +
                    "WHERE e.${Event.COLUMN_ID} IS NULL"
        )

        assertThat(osierocone, `is`(0))
    }

    // ---------- dopasowanie po dacie ----------

    @Test
    fun `dopasowanie idzie po pelnej dacie, a nie po jej poczatku`() = runBlocking {
        // Bez przecinka w warunku LIKE data „2026_01_1" złapałaby „2026_01_11"
        // i import podmieniłby niewłaściwy dzień.
        dao.upsertAll(listOf(wpis("2026_01_11,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY)))

        val zmienione = dao.upsertAll(
            listOf(wpis("2026_01_1,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY))
        )

        assertThat(zmienione, `is`(1))
        assertThat(wszystkieWpisy().size, `is`(2))
    }

    @Test
    fun `import mieszany wstawia nowe i aktualizuje istniejace za jednym razem`() = runBlocking {
        dao.upsertAll(
            listOf(
                wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY),
                wpis("2026_12_20,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY),
            )
        )

        val zmienione = dao.upsertAll(
            listOf(
                wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY),          // bez zmian
                wpis("2026_12_20,00:00,commerce_sunday", Day.Type.COMMERCE_SUNDAY), // zmiana typu
                wpis("2027_01_01,00:00,new_year", Day.Type.HOLIDAY),            // nowy
            )
        )

        assertThat(zmienione, `is`(2))
        assertThat(wszystkieWpisy().size, `is`(3))
    }

    // ---------- zapytania odczytu ----------

    @Test
    fun `getAllEventsOfTypeNotBefore filtruje po dacie i po typie`() = runBlocking {
        dao.upsertAll(
            listOf(
                wpis("2026_01_04,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY),
                wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY),
                wpis("2026_12_20,00:00,commerce_sunday", Day.Type.COMMERCE_SUNDAY),
            )
        )

        val tylkoSwieta = dao.getAllEventsOfTypeNotBefore(
            listOf(Day.Type.HOLIDAY.code.toString()), "2026_01_01"
        ).orEmpty()
        assertThat(tylkoSwieta.size, `is`(1))

        val odSierpnia = dao.getAllEventsOfTypeNotBefore(
            listOf("0", "1", "2"), "2026_08_01"
        ).orEmpty()
        assertThat(odSierpnia.map { it.item.substringBefore(",") }, `is`(listOf("2026_08_15", "2026_12_20")))
    }

    @Test
    fun `getFirstEventNotBefore i getLastEvent siegaja po skrajne wpisy`() = runBlocking {
        dao.upsertAll(
            listOf(
                wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY),
                wpis("2026_01_04,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY),
                wpis("2027_01_01,00:00,new_year", Day.Type.HOLIDAY),
            )
        )

        assertThat(dao.getFirstEventNotBefore("2026_02_01")?.item?.substringBefore(","), `is`("2026_08_15"))
        assertThat(dao.getLastEvent()?.item?.substringBefore(","), `is`("2027_01_01"))
        assertThat(dao.getFirstEventNotBefore("2030_01_01") == null, `is`(true))
    }
}
