package eu.adamgiergun.wontgoshoppingpl.db

import eu.adamgiergun.wontgoshoppingpl.day.Day
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * [DaysPayload] to jedyne wejście danych o dniach do aplikacji — ten sam parser
 * czyta wbudowany `res/raw/days.json` i parametr `days_data` z Firebase Remote
 * Config. Payload z Remote Config trafia do **wszystkich** użytkowników naraz
 * i bez wydania nowej wersji, więc jego błąd nie ma żadnego bezpiecznika poza
 * tym kodem. Stąd nacisk na przypadki zepsutego wejścia.
 *
 * Robolectric, a nie czysty JVM, bo parser stoi na `org.json` i `android.util.Log`.
 * W teście jednostkowym obie klasy pochodzą z atrapy `android.jar` i każde
 * wywołanie rzuca „not mocked".
 *
 * JUnit 4, bo taki jest runner Robolectrica; wykonuje go silnik vintage.
 */
@RunWith(RobolectricTestRunner::class)
internal class DaysPayloadTest {

    private fun payload(vararg years: Pair<String, String>) =
        years.joinToString(",", "{", "}") { (year, body) -> "\"$year\":{$body}" }

    private fun group(name: String, vararg entries: Triple<String, String, String>) =
        entries.joinToString(",", "\"$name\":[", "]") { (date, time, key) ->
            """{"date":"$date","time":"$time","key":"$key"}"""
        }

    // ---------- poprawne wejście ----------

    @Test
    fun `wpis zamienia sie na Event z polem item zlozonym z daty, godziny i klucza`() {
        val events = DaysPayload.parse(
            payload("2026" to group("holidays", Triple("2026_08_15", "00:00", "assumption")))
        )

        assertThat(events.size, `is`(1))
        assertThat(events[0].item, `is`("2026_08_15,00:00,assumption"))
        assertThat(events[0].typeId, `is`(Day.Type.HOLIDAY.code))
        // Identyfikator nadaje baza, a pola kalendarza wypełnia dopiero
        // dodanie dnia do Kalendarza Google — parser nie ma prawa ich ustawiać.
        assertThat(events[0].id == null, `is`(true))
        assertThat(events[0].calendarId == null, `is`(true))
        assertThat(events[0].calendarEventId == null, `is`(true))
    }

    @Test
    fun `kazda z trzech grup dostaje swoj typ dnia`() {
        val events = DaysPayload.parse(
            payload(
                "2026" to listOf(
                    group("nonCommerceDays", Triple("2026_01_04", "00:00", "non_commerce_sunday")),
                    group("commerceSundays", Triple("2026_01_25", "00:00", "commerce_sunday")),
                    group("holidays", Triple("2026_01_01", "00:00", "new_year")),
                ).joinToString(",")
            )
        )

        val typyWgDaty = events.associate { it.item.substringBefore(",") to it.typeId }
        assertThat(typyWgDaty["2026_01_04"], `is`(Day.Type.NON_COMMERCE_DAY.code))
        assertThat(typyWgDaty["2026_01_25"], `is`(Day.Type.COMMERCE_SUNDAY.code))
        assertThat(typyWgDaty["2026_01_01"], `is`(Day.Type.HOLIDAY.code))
    }

    @Test
    fun `wynik jest posortowany chronologicznie mimo pomieszanych grup i lat`() {
        val events = DaysPayload.parse(
            payload(
                "2027" to group("holidays", Triple("2027_01_01", "00:00", "new_year")),
                "2026" to listOf(
                    group("commerceSundays", Triple("2026_12_20", "00:00", "commerce_sunday")),
                    group("nonCommerceDays", Triple("2026_01_04", "00:00", "non_commerce_sunday")),
                ).joinToString(","),
            )
        )

        assertThat(
            events.map { it.item.substringBefore(",") },
            `is`(listOf("2026_01_04", "2026_12_20", "2027_01_01"))
        )
    }

    @Test
    fun `brakujaca grupa jest pomijana, a pozostale wchodza`() {
        val events = DaysPayload.parse(
            payload("2026" to group("holidays", Triple("2026_05_03", "00:00", "constitution_day")))
        )

        assertThat(events.size, `is`(1))
    }

    @Test
    fun `pusta tablica grupy nie wnosi wpisow`() {
        val events = DaysPayload.parse(payload("2026" to "\"holidays\":[]"))

        assertThat(events, `is`(emptyList()))
    }

    @Test
    fun `wbudowany zasob days_json parsuje sie i daje niepusta liste`() {
        val events = DaysPayload.fromResources(RuntimeEnvironment.getApplication())

        assertThat(events.isNotEmpty(), `is`(true))
        // Ten sam kontrakt, co przy payloadzie zdalnym: posortowane i z trzema polami.
        assertThat(events, `is`(events.sortedBy { it.item }))
        assertThat(events.all { it.item.split(",").size == 3 }, `is`(true))
        assertThat(events.all { it.typeId in 0..2 }, `is`(true))
    }

    // ---------- zepsute wejście ----------
    //
    // Kontrakt brzmi: pusta lista, nigdy wyjątek. DaysRepository traktuje pustą
    // listę jako „nie ruszaj bazy", więc użytkownikowi zostają dane, które już ma.

    @Test
    fun `pusty tekst daje pusta liste zamiast wyjatku`() {
        assertThat(DaysPayload.parse(""), `is`(emptyList()))
    }

    @Test
    fun `tekst nie bedacy JSON-em daje pusta liste`() {
        assertThat(DaysPayload.parse("to nie jest json"), `is`(emptyList()))
    }

    @Test
    fun `obciety JSON daje pusta liste`() {
        assertThat(DaysPayload.parse("""{"2026":{"holidays":[{"date":"2026_01_01""""), `is`(emptyList()))
    }

    @Test
    fun `wpis bez pola key daje pusta liste, a nie wpis okrojony`() {
        val json = """{"2026":{"holidays":[{"date":"2026_01_01","time":"00:00"}]}}"""

        assertThat(DaysPayload.parse(json), `is`(emptyList()))
    }

    @Test
    fun `grupa o nieznanej nazwie jest ignorowana`() {
        val events = DaysPayload.parse(
            payload("2026" to group("wolneDni", Triple("2026_01_01", "00:00", "new_year")))
        )

        assertThat(events, `is`(emptyList()))
    }

    @Test
    fun `jeden zepsuty wpis unieważnia caly payload`() {
        // Świadomy wybór projektowy, nie przeoczenie: parser buduje listę
        // w całości albo wcale. Częściowy wynik trafiłby do upsertAll i podmienił
        // poprawne dane w bazie na niekompletne.
        val json = """
            {"2026":{"holidays":[
                {"date":"2026_01_01","time":"00:00","key":"new_year"},
                {"date":"2026_05_03","time":"00:00"}
            ]}}
        """.trimIndent()

        assertThat(DaysPayload.parse(json), `is`(emptyList()))
    }
}
