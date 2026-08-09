package eu.adamgiergun.wontgoshoppingpl.calendar

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

/**
 * [SimpleTime] parsuje godzinę zapisaną w drugim polu wpisu (`data,godzina,klucz`),
 * a jego `toString` wraca do tego samego formatu. Obie strony muszą się zgadzać,
 * bo tekst przechodzi przez bazę i przez preferencje.
 */
internal class SimpleTimeTest {

    @Test
    fun `parsuje godzine z tekstu`() {
        val czas = SimpleTime("10:30")

        assertThat(czas.hour, `is`(10))
        assertThat(czas.minute, `is`(30))
    }

    @Test
    fun `polnoc oznacza dzien caly`() {
        // Day.allDay rozpoznaje dzień całodobowy po literalnym „00:00",
        // więc ta wartość musi się parsować na zera.
        val czas = SimpleTime("00:00")

        assertThat(czas.hour, `is`(0))
        assertThat(czas.minute, `is`(0))
    }

    @Test
    fun `godzina jednocyfrowa bez wiodacego zera tez sie parsuje`() {
        val czas = SimpleTime("9:05")

        assertThat(czas.hour, `is`(9))
        assertThat(czas.minute, `is`(5))
    }

    @Test
    fun `toString uzupelnia minuty wiodacym zerem`() {
        assertThat(SimpleTime(10, 5).toString(), `is`("10:05"))
        assertThat(SimpleTime(10, 30).toString(), `is`("10:30"))
    }

    @Test
    fun `toString nie uzupelnia godziny wiodacym zerem`() {
        // Zachowanie asymetryczne i łatwe do przeoczenia: „9:05", nie „09:05".
        // Przypięte, bo ten tekst trafia do preferencji jako wartość domyślna
        // przypomnienia, a PreferenceKeysTest pilnuje formatu „10:00".
        assertThat(SimpleTime(9, 5).toString(), `is`("9:05"))
    }

    @Test
    fun `tekst przechodzi w obie strony bez zmian`() {
        for (tekst in listOf("00:00", "10:00", "23:59", "12:07")) {
            assertThat(SimpleTime(tekst).toString().let(::SimpleTime).toString(), `is`(SimpleTime(tekst).toString()))
        }
    }
}
