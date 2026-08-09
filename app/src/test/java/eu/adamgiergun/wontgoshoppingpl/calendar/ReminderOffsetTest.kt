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
 * Przesunięcie przypomnienia, czyli `Reminders.MINUTES`.
 *
 * Pora przypomnienia jest preferencją użytkownika o **jego własnym poranku**,
 * więc godzina liczy się w strefie urządzenia, a nie w strefie wydarzenia.
 * Dla dni całodobowych to rozróżnienie decyduje o wszystkim, bo ich kalendarz
 * startowy stoi w UTC.
 *
 * Wartość zmierzona wcześniej na urządzeniu przy „1 dzień wcześniej, o 10:00":
 * do bazy szło `minutes = 840`, czyli alarm o 10:00 UTC — 12:00 czasu polskiego
 * latem. Poprawnie ma być 960 i 10:00 lokalnie.
 */
@RunWith(RobolectricTestRunner::class)
internal class ReminderOffsetTest {

    private lateinit var domyslnaStrefa: TimeZone

    @Before
    fun zapamietajStrefe() {
        domyslnaStrefa = TimeZone.getDefault()
    }

    @After
    fun przywrocStrefe() = TimeZone.setDefault(domyslnaStrefa)

    private fun minutyPrzed(
        data: SimpleDate,
        godzinaWydarzenia: String,
        calyDzien: Boolean,
        strefaWydarzenia: String,
        dniWczesniej: Int,
        poraPrzypomnienia: String,
    ): Long {
        val budowniczy = SimpleCalendarEvent.Builder(
            "1", data, SimpleTime(godzinaWydarzenia), strefaWydarzenia, calyDzien
        )
        return SimpleCalendarEvent.Reminder(
            budowniczy.startCalendar,
            SimpleReminder(
                SimpleReminder.METHOD_ALERT, SimpleTime(poraPrzypomnienia), dniWczesniej, strefaWydarzenia
            )
        ).numberOfMinutesBeforeEvent
    }

    /** Moment, w którym alarm faktycznie zadzwoni, odczytany w podanej strefie. */
    private fun godzinaAlarmu(
        data: SimpleDate,
        calyDzien: Boolean,
        strefaWydarzenia: String,
        dniWczesniej: Int,
        pora: String,
        odczytWStrefie: String,
    ): Int {
        val budowniczy = SimpleCalendarEvent.Builder(
            "1", data, SimpleTime(if (calyDzien) "00:00" else "13:00"), strefaWydarzenia, calyDzien
        )
        val minuty = minutyPrzed(data, if (calyDzien) "00:00" else "13:00", calyDzien, strefaWydarzenia, dniWczesniej, pora)
        val alarm = budowniczy.startCalendar.timeInMillis - minuty * 60_000
        return Calendar.getInstance(TimeZone.getTimeZone(odczytWStrefie))
            .apply { timeInMillis = alarm }[Calendar.HOUR_OF_DAY]
    }

    // ---------- dzień całodobowy ----------

    @Test
    fun `dla dnia calodobowego alarm wypada o zadanej godzinie czasu lokalnego`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        assertThat(
            godzinaAlarmu(
                SimpleDate(2026, 8, 23), calyDzien = true, strefaWydarzenia = "Europe/Warsaw",
                dniWczesniej = 1, pora = "10:00", odczytWStrefie = "Europe/Warsaw"
            ),
            `is`(10)
        )
    }

    @Test
    fun `przesuniecie dla dnia calodobowego w Polsce latem to 960 minut`() {
        // Zmierzone na urządzeniu przed poprawką: 840, czyli 10:00 UTC.
        // Poprawnie: północ 23.08 UTC minus 10:00 czasu polskiego 22.08 (08:00 UTC)
        // to 16 godzin.
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        assertThat(
            minutyPrzed(
                SimpleDate(2026, 8, 23), "00:00", calyDzien = true,
                strefaWydarzenia = "Europe/Warsaw", dniWczesniej = 1, poraPrzypomnienia = "10:00"
            ),
            `is`(960L)
        )
    }

    @Test
    fun `zima przesuniecie jest o godzine mniejsze, bo strefa to UTC plus jeden`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        assertThat(
            minutyPrzed(
                SimpleDate(2026, 1, 11), "00:00", calyDzien = true,
                strefaWydarzenia = "Europe/Warsaw", dniWczesniej = 1, poraPrzypomnienia = "10:00"
            ),
            `is`(900L)
        )
    }

    @Test
    fun `uzytkownik w Australii dostaje alarm o dziesiatej u siebie`() {
        // Sedno wyboru projektowego: pora przypomnienia idzie za zegarem
        // użytkownika, a nie za strefą wydarzenia.
        TimeZone.setDefault(TimeZone.getTimeZone("Australia/Sydney"))

        assertThat(
            godzinaAlarmu(
                SimpleDate(2026, 8, 23), calyDzien = true, strefaWydarzenia = "Europe/Warsaw",
                dniWczesniej = 1, pora = "10:00", odczytWStrefie = "Australia/Sydney"
            ),
            `is`(10)
        )
    }

    @Test
    fun `zero dni wczesniej oznacza przypomnienie w dniu wydarzenia`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        // Północ UTC 23.08 minus 10:00 czasu polskiego 23.08 (08:00 UTC)
        // to minus 8 godzin — alarm wypada PO starcie wydarzenia całodobowego.
        assertThat(
            minutyPrzed(
                SimpleDate(2026, 8, 23), "00:00", calyDzien = true,
                strefaWydarzenia = "Europe/Warsaw", dniWczesniej = 0, poraPrzypomnienia = "10:00"
            ),
            `is`(-480L)
        )
    }

    // ---------- dzień z godziną ----------

    @Test
    fun `dla wydarzenia z godzina alarm tez wypada o zadanej godzinie lokalnej`() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        assertThat(
            godzinaAlarmu(
                SimpleDate(2026, 4, 4), calyDzien = false, strefaWydarzenia = "Europe/Warsaw",
                dniWczesniej = 1, pora = "10:00", odczytWStrefie = "Europe/Warsaw"
            ),
            `is`(10)
        )
    }

    @Test
    fun `dla wydarzenia z godzina przesuniecie to 27 godzin`() {
        // Wielka Sobota 13:00 czasu polskiego, przypomnienie dzień wcześniej
        // o 10:00 — od 10:00 dnia poprzedniego do 13:00 mija 27 godzin.
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))

        assertThat(
            minutyPrzed(
                SimpleDate(2026, 4, 4), "13:00", calyDzien = false,
                strefaWydarzenia = "Europe/Warsaw", dniWczesniej = 1, poraPrzypomnienia = "10:00"
            ),
            `is`(27L * 60)
        )
    }
}
