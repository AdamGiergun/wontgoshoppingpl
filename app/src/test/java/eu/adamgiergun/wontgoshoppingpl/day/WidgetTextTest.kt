package eu.adamgiergun.wontgoshoppingpl.day

import android.content.Context
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.db.Event
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * Treść widgetu. Wydzielone z [DayTest], bo dotyczy jednej reguły przecinającej
 * wszystkie typy dnia: widget pokazuje **typ**, a nie nazwę własną święta.
 *
 * Powód jest wymiarowy. „Wniebowzięcie Najświętszej Maryi Panny" ma 38 znaków,
 * a „dzień po Święcie Stulecia Niepodległości" 40 — te dwa wyjątki decydowały
 * o szerokości kafelka dla wszystkich 126 wpisów.
 */
internal class WidgetTextTest {

    private fun dzien(typ: Day.Type, wpis: String, context: Context) =
        Day(Event(0, typ.code, wpis, null, null), context)

    @Test
    @DisplayName("Święto pokazuje nazwę typu, nie nazwę własną")
    fun swietoPokazujeTypDnia() {
        val context = mock(Context::class.java)
        `when`(context.getString(R.string.day_name_holiday)).thenReturn("Holiday")

        val dzien = dzien(Day.Type.HOLIDAY, "2026_08_15,00:00,Assumption of Mary", context)

        assertThat(dzien.widgetText, `is`("15 sie 2026\nHoliday"))
    }

    @Test
    @DisplayName("Nazwa własna święta zostaje nietknięta dla reszty aplikacji")
    fun nazwaWlasnaZostajeDostepna() {
        val context = mock(Context::class.java)
        `when`(context.getString(R.string.day_name_holiday)).thenReturn("Holiday")

        val dzien = dzien(Day.Type.HOLIDAY, "2026_08_15,00:00,Assumption of Mary", context)

        assertThat(dzien.name, `is`("Assumption of Mary"))
    }

    /**
     * Skrót dotyczy wyłącznie świąt — niedziele mają nazwy krótkie same z siebie.
     * Sprawdzane razem z tym, że dla nich [Day] **w ogóle nie sięga do zasobów**:
     * na tym założeniu stoi cały [DayTest], budujący dni na atrapie kontekstu.
     */
    @Test
    @DisplayName("Niedziela zachowuje pełną nazwę i nie rusza zasobów")
    fun niedzielaBezSkrotuIBezZasobow() {
        val context = mock(Context::class.java)

        val dzien = dzien(
            Day.Type.NON_COMMERCE_DAY,
            "2026_08_16,00:00,Niedziela niehandlowa",
            context
        )

        assertThat(dzien.widgetText, `is`("16 sie 2026\nNiedziela niehandlowa"))
        verify(context, never()).getString(anyInt())
    }

    @Test
    @DisplayName("Niedziela handlowa też zachowuje pełną nazwę")
    fun niedzielaHandlowaBezSkrotu() {
        val context = mock(Context::class.java)

        val dzien = dzien(
            Day.Type.COMMERCE_SUNDAY,
            "2026_08_30,00:00,Niedziela handlowa",
            context
        )

        assertThat(dzien.widgetText, `is`("30 sie 2026\nNiedziela handlowa"))
        verify(context, never()).getString(anyInt())
    }

    @Test
    @DisplayName("Przedrostek PL: zniknął z treści widgetu")
    fun bezPrzedrostkaPL() {
        val context = mock(Context::class.java)

        val dzien = dzien(
            Day.Type.NON_COMMERCE_DAY,
            "2026_08_16,00:00,Niedziela niehandlowa",
            context
        )

        assertThat(dzien.widgetText.contains("PL:"), `is`(false))
    }
}
