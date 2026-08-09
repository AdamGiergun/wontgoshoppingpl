package eu.adamgiergun.wontgoshoppingpl.day

import android.content.Context
import eu.adamgiergun.wontgoshoppingpl.db.Event
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

/**
 * Reguła sklejania dni następujących po sobie.
 *
 * Ciąg kolejnych dni bez handlu ma dostać w Kalendarzu Google **jedno**
 * wydarzenie i jedno przypomnienie, a nie osobne dla każdego dnia — inaczej
 * użytkownik dostawałby w Boże Narodzenie trzy powiadomienia pod rząd. Pierwszy
 * dzień ciągu zbiera nazwy pozostałych do opisu i zapamiętuje jego długość
 * (z niej bierze się data końcowa wydarzenia), a kolejne tracą przypomnienie.
 *
 * Reguła nie ma wyjścia produkcyjnego innego niż baza i Kalendarz Google, więc
 * testuje się ją przez `@VisibleForTesting`.
 *
 * Czysty JVM. Wpisy trzymają w trzecim polu gotową nazwę zamiast klucza — tak
 * jak wiersze sprzed wersji 6 bazy — więc [DayNames] zwraca tekst bez zmian
 * i nie sięga do zasobów; atrapa kontekstu nigdy nie jest wołana. Ten sam chwyt
 * co w [DayTest].
 */
internal class ConsecutiveDaysTest {

    private val context: Context = mock(Context::class.java)

    private fun dzien(data: String, nazwa: String, typ: Day.Type = Day.Type.NON_COMMERCE_DAY) =
        Day(Event(null, typ.code, "$data,00:00,$nazwa", null, null), context)

    private fun sklej(vararg dni: Day) =
        Day.List.skipReminderForConsecutiveDays(ArrayList(dni.toList()))

    @Test
    fun `pojedynczy dzien zostaje ze swoim przypomnieniem`() {
        val dzien = dzien("2026_08_15", "Wniebowzięcie", Day.Type.HOLIDAY)

        sklej(dzien)

        assertThat(dzien.isReminderNeeded, `is`(true))
        assertThat(dzien.noOfConsecutiveDays, `is`(0))
        assertThat(dzien.description, `is`("Wniebowzięcie"))
    }

    @Test
    fun `dwa dni z rzedu daja jedno przypomnienie na pierwszym`() {
        val pierwszy = dzien("2026_12_25", "Boże Narodzenie", Day.Type.HOLIDAY)
        val drugi = dzien("2026_12_26", "Drugi dzień świąt", Day.Type.HOLIDAY)

        sklej(pierwszy, drugi)

        assertThat(pierwszy.isReminderNeeded, `is`(true))
        assertThat(drugi.isReminderNeeded, `is`(false))
        assertThat(pierwszy.noOfConsecutiveDays, `is`(1))
        assertThat(pierwszy.description, `is`("Boże Narodzenie, Drugi dzień świąt"))
    }

    @Test
    fun `trzy dni z rzedu skladaja sie w jeden ciag`() {
        val pierwszy = dzien("2026_12_25", "Boże Narodzenie", Day.Type.HOLIDAY)
        val drugi = dzien("2026_12_26", "Drugi dzień świąt", Day.Type.HOLIDAY)
        val trzeci = dzien("2026_12_27", "Niedziela niehandlowa")

        sklej(pierwszy, drugi, trzeci)

        assertThat(pierwszy.noOfConsecutiveDays, `is`(2))
        assertThat(pierwszy.description, `is`("Boże Narodzenie, Drugi dzień świąt, Niedziela niehandlowa"))
        assertThat(listOf(drugi, trzeci).map { it.isReminderNeeded }, `is`(listOf(false, false)))
    }

    @Test
    fun `ciag przechodzi przez koniec roku`() {
        val sylwester = dzien("2026_12_31", "Sylwester", Day.Type.HOLIDAY)
        val nowyRok = dzien("2027_01_01", "Nowy Rok", Day.Type.HOLIDAY)

        sklej(sylwester, nowyRok)

        assertThat(sylwester.noOfConsecutiveDays, `is`(1))
        assertThat(nowyRok.isReminderNeeded, `is`(false))
    }

    @Test
    fun `przerwa w datach rozdziela ciagi`() {
        val pierwszy = dzien("2026_08_15", "Wniebowzięcie", Day.Type.HOLIDAY)
        val odlegly = dzien("2026_08_16", "Niedziela niehandlowa")
        val zupelnieOsobny = dzien("2026_09_06", "Niedziela niehandlowa")

        sklej(pierwszy, odlegly, zupelnieOsobny)

        assertThat(pierwszy.noOfConsecutiveDays, `is`(1))
        assertThat(odlegly.isReminderNeeded, `is`(false))
        // Nowy ciąg zaczyna się od zera i zachowuje własne przypomnienie.
        assertThat(zupelnieOsobny.isReminderNeeded, `is`(true))
        assertThat(zupelnieOsobny.noOfConsecutiveDays, `is`(0))
    }

    @Test
    fun `niedziela handlowa przerywa ciag i sama zachowuje przypomnienie`() {
        // Informacja odwrotna: „sklepy otwarte". Nie ma po co doklejać jej do
        // ostrzeżenia o dniach zamkniętych, więc łańcuch się na niej urywa.
        val pierwszy = dzien("2026_12_19", "Dzień bez handlu")
        val handlowa = dzien("2026_12_20", "Niedziela handlowa", Day.Type.COMMERCE_SUNDAY)
        val kolejny = dzien("2026_12_21", "Dzień bez handlu")

        sklej(pierwszy, handlowa, kolejny)

        assertThat(pierwszy.noOfConsecutiveDays, `is`(0))
        assertThat(pierwszy.description, `is`("Dzień bez handlu"))
        assertThat(handlowa.isReminderNeeded, `is`(true))
        assertThat(kolejny.isReminderNeeded, `is`(true))
    }

    @Test
    fun `dzien po niedzieli handlowej zaczyna nowy ciag`() {
        val handlowa = dzien("2026_12_20", "Niedziela handlowa", Day.Type.COMMERCE_SUNDAY)
        val pierwszy = dzien("2026_12_21", "Dzień bez handlu")
        val drugi = dzien("2026_12_22", "Dzień bez handlu")

        sklej(handlowa, pierwszy, drugi)

        assertThat(handlowa.noOfConsecutiveDays, `is`(0))
        assertThat(pierwszy.noOfConsecutiveDays, `is`(1))
        assertThat(drugi.isReminderNeeded, `is`(false))
    }

    @Test
    fun `pusta lista nie wywraca reguly`() {
        assertThat(Day.List.skipReminderForConsecutiveDays(ArrayList()).size, `is`(0))
    }

    @Test
    fun `dwa osobne ciagi w jednej liscie`() {
        val a1 = dzien("2026_11_01", "Wszystkich Świętych", Day.Type.HOLIDAY)
        val a2 = dzien("2026_11_02", "Zaduszki", Day.Type.HOLIDAY)
        val b1 = dzien("2026_12_25", "Boże Narodzenie", Day.Type.HOLIDAY)
        val b2 = dzien("2026_12_26", "Drugi dzień świąt", Day.Type.HOLIDAY)

        sklej(a1, a2, b1, b2)

        assertThat(a1.noOfConsecutiveDays, `is`(1))
        assertThat(b1.noOfConsecutiveDays, `is`(1))
        assertThat(listOf(a1.isReminderNeeded, b1.isReminderNeeded), `is`(listOf(true, true)))
        assertThat(listOf(a2.isReminderNeeded, b2.isReminderNeeded), `is`(listOf(false, false)))
    }
}
