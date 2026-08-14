package eu.adamgiergun.wontgoshoppingpl.day

import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences.ColourSet
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Odwzorowanie zestawu kolorystycznego na zasoby.
 *
 * Test pilnuje własności, których łatwo nie zauważyć przy dokładaniu trzeciego
 * zestawu: każdy typ dnia musi dostać INNY zasób w obrębie zestawu (inaczej dwa
 * typy dnia wyglądałyby tak samo) i inny między zestawami (inaczej przełącznik
 * nic by nie zmienił). Same wartości kolorów sprawdzane są liczbowo w
 * tools/paleta.py, nie tutaj.
 */
internal class DayPaletteTest {

    private val typy = Day.Type.entries

    @Test
    @DisplayName("W obrębie zestawu każdy typ dnia ma własny kolor")
    fun typyRozniaSieWZestawie() {
        ColourSet.entries.forEach { zestaw ->
            val tla = typy.map { DayPalette.container(it, zestaw) }
            val teksty = typy.map { DayPalette.onContainer(it, zestaw) }
            assertThat("tła w $zestaw", tla.distinct().size, `is`(typy.size))
            assertThat("teksty w $zestaw", teksty.distinct().size, `is`(typy.size))
        }
    }

    @Test
    @DisplayName("Zestawy nie dzielą ani jednego zasobu koloru")
    fun zestawyNieDzielaKolorow() {
        typy.forEach { typ ->
            assertThat(
                "tło dla $typ",
                DayPalette.container(typ, ColourSet.PIGMENTS) ==
                        DayPalette.container(typ, ColourSet.CANDY),
                `is`(false)
            )
            assertThat(
                "tekst dla $typ",
                DayPalette.onContainer(typ, ColourSet.PIGMENTS) ==
                        DayPalette.onContainer(typ, ColourSet.CANDY),
                `is`(false)
            )
        }
    }

    @Test
    @DisplayName("Każda para zestaw-typ ma własny layout widgetu")
    fun layoutyWidgetuSaRozne() {
        val wszystkie = ColourSet.entries.flatMap { z -> typy.map { DayPalette.widgetLayout(it, z) } }
        assertThat(wszystkie.distinct().size, `is`(ColourSet.entries.size * typy.size))
    }

    /**
     * Wartość zapisywana w SharedPreferences. Zmiana skasowałaby wybór
     * użytkownika, więc jest przypięta testem — tak samo jak klucze preferencji.
     */
    @Test
    @DisplayName("Wartości zestawów w preferencjach są stałe")
    fun wartosciZestawowSaStale() {
        assertThat(ColourSet.PIGMENTS.value, `is`("1"))
        assertThat(ColourSet.CANDY.value, `is`("2"))
        assertThat(ColourSet.fromValue("2"), `is`(ColourSet.CANDY))
        assertThat(ColourSet.fromValue(null), `is`(ColourSet.PIGMENTS))
        assertThat(ColourSet.fromValue("nieznane"), `is`(ColourSet.PIGMENTS))
    }
}
