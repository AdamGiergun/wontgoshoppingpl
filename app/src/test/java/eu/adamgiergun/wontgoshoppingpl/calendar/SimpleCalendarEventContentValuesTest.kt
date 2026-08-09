package eu.adamgiergun.wontgoshoppingpl.calendar

import android.provider.CalendarContract
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
 * To, co naprawdę trafia do dostawcy kalendarza.
 *
 * Sedno rozróżnienia: dzień bez handlu jest **datą**, a nie momentem. Dostawca
 * zapisuje datę jako północ UTC i wymaga wtedy `EVENT_TIMEZONE` równego `"UTC"`
 * oraz czasu trwania będącego wielokrotnością doby — dopiero ten komplet
 * sprawia, że wydarzenie wypada na tym samym dniu kalendarza u każdego,
 * niezależnie od strefy jego telefonu.
 *
 * Wydarzenie z godziną to co innego: „sklepy zamykają się o 13:00 w Wielką
 * Sobotę" jest momentem w polskiej strefie i tak ma zostać zapisane.
 *
 * Testy istnieją, bo poprzednia wersja pisała `EVENT_TIMEZONE = "Europe/Warsaw"`
 * także dla wydarzeń całodobowych, przy `DTSTART` w północy UTC — dwa sąsiednie
 * pola mówiły co innego. Wcześniejsze testy sprawdzały tylko kalendarze startu
 * i końca, więc tego nie widziały.
 */
@RunWith(RobolectricTestRunner::class)
internal class SimpleCalendarEventContentValuesTest {

    private lateinit var domyslnaStrefa: TimeZone

    @Before
    fun ustawStrefe() {
        domyslnaStrefa = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
    }

    @After
    fun przywrocStrefe() = TimeZone.setDefault(domyslnaStrefa)

    private fun wydarzenie(
        data: SimpleDate,
        godzina: String,
        calyDzien: Boolean,
        koniec: Pair<SimpleDate, String>? = null,
    ) = SimpleCalendarEvent.Builder("1", data, SimpleTime(godzina), "Europe/Warsaw", calyDzien)
        .setTitle("Niedziela niehandlowa")
        .also { builder -> koniec?.let { builder.setEndCalendar(it.first, SimpleTime(it.second)) } }
        .build()
        .contentValues

    private fun wUtc(millis: Long) = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        .apply { timeInMillis = millis }

    // ---------- wydarzenie całodobowe ----------

    @Test
    fun `calodobowe deklaruje strefe UTC`() {
        val wartosci = wydarzenie(SimpleDate(2026, 8, 16), "00:00", calyDzien = true)

        assertThat(wartosci.getAsInteger(CalendarContract.Events.ALL_DAY), `is`(1))
        assertThat(wartosci.getAsString(CalendarContract.Events.EVENT_TIMEZONE), `is`("UTC"))
        assertThat(wartosci.getAsString(CalendarContract.Events.EVENT_END_TIMEZONE), `is`("UTC"))
    }

    @Test
    fun `calodobowe zaczyna sie o polnocy UTC dokladnie w swoim dniu`() {
        val wartosci = wydarzenie(SimpleDate(2026, 8, 16), "00:00", calyDzien = true)

        val start = wUtc(wartosci.getAsLong(CalendarContract.Events.DTSTART))
        assertThat(start[Calendar.YEAR], `is`(2026))
        assertThat(start[Calendar.MONTH] + 1, `is`(8))
        assertThat(start[Calendar.DAY_OF_MONTH], `is`(16))
        assertThat(start[Calendar.HOUR_OF_DAY], `is`(0))
        assertThat(start[Calendar.MINUTE], `is`(0))
    }

    @Test
    fun `calodobowe jednodniowe trwa dokladnie dobe`() {
        // Kontrakt wymaga wielokrotności doby. Budowniczy dostaje 23:59
        // ostatniego dnia — czytelne dla człowieka, ale to doba bez minuty.
        val wartosci = wydarzenie(
            SimpleDate(2026, 8, 16), "00:00", calyDzien = true,
            koniec = SimpleDate(2026, 8, 16) to "23:59",
        )

        val trwanie = wartosci.getAsLong(CalendarContract.Events.DTEND) -
                wartosci.getAsLong(CalendarContract.Events.DTSTART)

        assertThat(trwanie, `is`(24L * 60 * 60 * 1000))
    }

    @Test
    fun `calodobowe wielodniowe trwa wielokrotnosc doby`() {
        // Tak Day buduje wydarzenie dla ciągu dni następujących po sobie:
        // 25, 26 i 27 grudnia to trzy doby.
        val wartosci = wydarzenie(
            SimpleDate(2026, 12, 25), "00:00", calyDzien = true,
            koniec = SimpleDate(2026, 12, 27) to "23:59",
        )

        val trwanie = wartosci.getAsLong(CalendarContract.Events.DTEND) -
                wartosci.getAsLong(CalendarContract.Events.DTSTART)

        assertThat(trwanie, `is`(3 * 24L * 60 * 60 * 1000))
        val koniec = wUtc(wartosci.getAsLong(CalendarContract.Events.DTEND))
        assertThat(koniec[Calendar.DAY_OF_MONTH], `is`(28))
        assertThat(koniec[Calendar.HOUR_OF_DAY], `is`(0))
    }

    @Test
    fun `calodobowe nie zalezy od strefy urzadzenia`() {
        val wNowymJorku = wydarzenie(SimpleDate(2026, 8, 16), "00:00", calyDzien = true)
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"))
        val wAuckland = wydarzenie(SimpleDate(2026, 8, 16), "00:00", calyDzien = true)

        assertThat(
            wAuckland.getAsLong(CalendarContract.Events.DTSTART),
            `is`(wNowymJorku.getAsLong(CalendarContract.Events.DTSTART))
        )
    }

    // ---------- wydarzenie z godziną ----------

    @Test
    fun `wydarzenie z godzina deklaruje strefe polska`() {
        val wartosci = wydarzenie(SimpleDate(2026, 4, 4), "13:00", calyDzien = false)

        assertThat(wartosci.getAsString(CalendarContract.Events.EVENT_TIMEZONE), `is`("Europe/Warsaw"))
        assertThat(wartosci.getAsString(CalendarContract.Events.EVENT_END_TIMEZONE), `is`("Europe/Warsaw"))
        assertThat(wartosci.containsKey(CalendarContract.Events.ALL_DAY), `is`(false))
    }

    @Test
    fun `wydarzenie z godzina wypada o tej godzinie czasu polskiego`() {
        // Wielka Sobota 2026 — jedyny w danych dzień z godziną. Kwiecień, czyli
        // czas letni: 13:00 w Warszawie to 11:00 UTC.
        val wartosci = wydarzenie(SimpleDate(2026, 4, 4), "13:00", calyDzien = false)

        val start = wUtc(wartosci.getAsLong(CalendarContract.Events.DTSTART))
        assertThat(start[Calendar.HOUR_OF_DAY], `is`(11))
        assertThat(start[Calendar.DAY_OF_MONTH], `is`(4))
    }

    @Test
    fun `zmiana czasu jest liczona dla dnia wydarzenia, a nie dla dzis`() {
        // Ta sama godzina zimą to inne przesunięcie: 13:00 w Warszawie
        // w styczniu to 12:00 UTC, a nie 11:00.
        val zima = wydarzenie(SimpleDate(2026, 1, 10), "13:00", calyDzien = false)

        val start = wUtc(zima.getAsLong(CalendarContract.Events.DTSTART))
        assertThat(start[Calendar.HOUR_OF_DAY], `is`(12))
    }

    @Test
    fun `bez podanego konca wydarzenie z godzina trwa godzine`() {
        val wartosci = wydarzenie(SimpleDate(2026, 4, 4), "13:00", calyDzien = false)

        val trwanie = wartosci.getAsLong(CalendarContract.Events.DTEND) -
                wartosci.getAsLong(CalendarContract.Events.DTSTART)

        assertThat(trwanie, `is`(60L * 60 * 1000))
    }
}
