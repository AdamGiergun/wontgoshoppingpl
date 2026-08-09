package eu.adamgiergun.wontgoshoppingpl.calendar

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar
import java.util.TimeZone

/**
 * Składanie wydarzenia dla Kalendarza Google — ta część, która nie dotyka
 * `ContentResolver`.
 *
 * Sedno jest w strefach czasowych i łatwo się w nim pomylić: [SimpleDate] liczy
 * w UTC (data bez godziny nie ma strefy), a wydarzenie w kalendarzu ma się
 * pokazać o właściwej godzinie **lokalnej**. Konwersję robi `setProperTime`
 * i tylko dla wydarzeń z godziną — całodobowe zostają w UTC, bo inaczej
 * przeskakiwałyby o dzień.
 *
 * Robolectric, bo companion object klasy woła `Uri.parse` już przy ładowaniu.
 */
@RunWith(RobolectricTestRunner::class)
internal class SimpleCalendarEventBuilderTest {

    private lateinit var domyslnaStrefa: TimeZone

    @Before
    fun ustawStrefe() {
        domyslnaStrefa = TimeZone.getDefault()
        // Strefa maszyny celowo inna niż strefa wydarzenia — gdyby kod czytał
        // domyślną zamiast podanej, testy poniżej by tego nie przepuściły.
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
    }

    @After
    fun przywrocStrefe() = TimeZone.setDefault(domyslnaStrefa)

    private fun wydarzenie(
        data: SimpleDate = SimpleDate(2026, 8, 15),
        godzina: String = "10:00",
        strefa: String? = "Europe/Warsaw",
        calyDzien: Boolean = false,
        koniec: Pair<SimpleDate, String>? = null,
    ) = SimpleCalendarEvent.Builder("1", data, SimpleTime(godzina), strefa, calyDzien)
        .setTitle("tytuł")
        .also { builder -> koniec?.let { builder.setEndCalendar(it.first, SimpleTime(it.second)) } }
        .build()

    // ---------- wydarzenie z godziną ----------

    @Test
    fun `wydarzenie z godzina wypada o tej godzinie w swojej strefie`() {
        val start = wydarzenie(godzina = "10:00", strefa = "Europe/Warsaw").getStartCalendar()

        val wWarszawie = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"))
            .apply { timeInMillis = start.timeInMillis }

        assertThat(wWarszawie[Calendar.HOUR_OF_DAY], `is`(10))
        assertThat(wWarszawie[Calendar.MINUTE], `is`(0))
        assertThat(wWarszawie[Calendar.DAY_OF_MONTH], `is`(15))
    }

    @Test
    fun `ta sama godzina w innej strefie to inny moment`() {
        val warszawa = wydarzenie(strefa = "Europe/Warsaw").getStartCalendar().timeInMillis
        val londyn = wydarzenie(strefa = "Europe/London").getStartCalendar().timeInMillis

        // Latem różnica Warszawa–Londyn to godzina.
        assertThat(londyn - warszawa, `is`(60L * 60 * 1000))
    }

    @Test
    fun `brak podanej strefy oznacza strefe urzadzenia`() {
        val bezStrefy = wydarzenie(strefa = null).getStartCalendar()

        val lokalnie = Calendar.getInstance().apply { timeInMillis = bezStrefy.timeInMillis }
        assertThat(lokalnie[Calendar.HOUR_OF_DAY], `is`(10))
    }

    // ---------- wydarzenie całodobowe ----------

    @Test
    fun `wydarzenie calodobowe zostaje w UTC i nie przeskakuje dnia`() {
        // Gdyby całodobowe też przechodziło przez setProperTime, w strefach na
        // zachód od UTC data cofnęłaby się o dzień — dzień bez handlu pokazałby
        // się w kalendarzu dobę wcześniej.
        val start = wydarzenie(godzina = "00:00", calyDzien = true).getStartCalendar()

        val wUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            .apply { timeInMillis = start.timeInMillis }

        assertThat(wUtc[Calendar.YEAR], `is`(2026))
        assertThat(wUtc[Calendar.MONTH] + 1, `is`(8))
        assertThat(wUtc[Calendar.DAY_OF_MONTH], `is`(15))
        assertThat(wUtc[Calendar.HOUR_OF_DAY], `is`(0))
    }

    @Test
    fun `wydarzenie calodobowe nie zalezy od strefy maszyny`() {
        val wNowymJorku = wydarzenie(godzina = "00:00", calyDzien = true).getStartCalendar().timeInMillis
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"))
        val wAuckland = wydarzenie(godzina = "00:00", calyDzien = true).getStartCalendar().timeInMillis

        assertThat(wAuckland, `is`(wNowymJorku))
    }

    // ---------- koniec wydarzenia ----------

    @Test
    fun `bez podanego konca wydarzenie trwa godzine`() {
        val wydarzenie = wydarzenie(godzina = "10:00")

        val roznica = wydarzenie.getEndCalendar().timeInMillis - wydarzenie.getStartCalendar().timeInMillis
        assertThat(roznica, `is`(60L * 60 * 1000))
    }

    @Test
    fun `podany koniec obejmuje caly ciag dni`() {
        // Tak Day buduje wydarzenie dla ciągu dni następujących po sobie:
        // start pierwszego dnia, koniec o 23 59 ostatniego.
        val wydarzenie = wydarzenie(
            data = SimpleDate(2026, 12, 25),
            godzina = "00:00",
            calyDzien = true,
            koniec = SimpleDate(2026, 12, 27) to "23:59",
        )

        val koniec = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            .apply { timeInMillis = wydarzenie.getEndCalendar().timeInMillis }

        assertThat(koniec[Calendar.DAY_OF_MONTH], `is`(27))
        assertThat(koniec[Calendar.HOUR_OF_DAY], `is`(23))
        assertThat(koniec[Calendar.MINUTE], `is`(59))
    }

    @Test
    fun `koniec jest zawsze po poczatku`() {
        val wydarzenie = wydarzenie(
            data = SimpleDate(2026, 12, 25),
            godzina = "00:00",
            calyDzien = true,
            koniec = SimpleDate(2026, 12, 27) to "23:59",
        )

        assertThat(wydarzenie.getEndCalendar().after(wydarzenie.getStartCalendar()), `is`(true))
    }

    @Test
    fun `getStartCalendar zwraca kopie, wiec wolajacy nie zepsuje wydarzenia`() {
        val wydarzenie = wydarzenie()
        val pierwotny = wydarzenie.getStartCalendar().timeInMillis

        wydarzenie.getStartCalendar().add(Calendar.YEAR, 5)

        assertThat(wydarzenie.getStartCalendar().timeInMillis, `is`(pierwotny))
    }
}
