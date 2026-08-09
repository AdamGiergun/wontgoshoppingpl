package eu.adamgiergun.wontgoshoppingpl.calendar

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * [SimpleDate] jest fundamentem, na którym stoi tożsamość wpisu: `Event.hashCode`
 * sprowadza cały wpis do `SimpleDate.hashCode`, a na `Event.equals` opiera się
 * `DiffUtil` listy i wyszukiwanie dnia przy odświeżaniu danych. Błąd w tej
 * arytmetyce nie objawia się wyjątkiem, tylko dniami znikającymi z listy albo
 * dublującymi się w bazie.
 *
 * Czysty JVM — klasa nie dotyka Androida, stoi na `java.util.Calendar`.
 */
internal class SimpleDateTest {

    private lateinit var domyslnaStrefa: TimeZone
    private lateinit var domyslnaLokalizacja: Locale

    /**
     * Strefa i język są ustawiane jawnie, bo klasa czyta obie z ustawień maszyny:
     * `weekdayName` bierze `Locale.getDefault()`, a `getLastDayOfMonth`
     * i `yesterday` stoją na `Calendar.getInstance()` w strefie lokalnej.
     * Bez tego wynik zależałby od tego, gdzie test jest uruchamiany.
     */
    @BeforeEach
    fun ustawSrodowisko() {
        domyslnaStrefa = TimeZone.getDefault()
        domyslnaLokalizacja = Locale.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"))
        Locale.setDefault(Locale.forLanguageTag("pl-PL"))
    }

    @AfterEach
    fun przywrocSrodowisko() {
        TimeZone.setDefault(domyslnaStrefa)
        Locale.setDefault(domyslnaLokalizacja)
    }

    // ---------- parsowanie z tekstu ----------

    @Test
    @DisplayName("ORDER_YMD czyta rok_miesiąc_dzień")
    fun `parsuje w kolejnosci YMD`() {
        val data = SimpleDate.newInstance("2026_08_15", SimpleDate.ORDER_YMD.toInt())

        assertThat(data.year, `is`(2026))
        assertThat(data.month, `is`(8))
        assertThat(data.day, `is`(15))
    }

    @Test
    @DisplayName("ORDER_YDM czyta rok_dzień_miesiąc")
    fun `parsuje w kolejnosci YDM`() {
        val data = SimpleDate.newInstance("2026_15_08", SimpleDate.ORDER_YDM.toInt())

        assertThat(data.year, `is`(2026))
        assertThat(data.month, `is`(8))
        assertThat(data.day, `is`(15))
    }

    @Test
    fun `wiodace zera nie zmieniaja wyniku`() {
        val data = SimpleDate.newInstance("2026_01_04", SimpleDate.ORDER_YMD.toInt())

        assertThat(data.month, `is`(1))
        assertThat(data.day, `is`(4))
    }

    // ---------- tożsamość ----------

    @Test
    fun `hashCode ma postac RRRRMMDD`() {
        // Wartość jest pilnowana literalnie, bo to ona jest tożsamością wpisu
        // w bazie i kluczem porównania w DiffUtil. Zmiana wzoru rozsypuje
        // rozpoznawanie dni, których nie widać w żadnym wyjątku.
        assertThat(SimpleDate(2026, 8, 15).hashCode(), `is`(20260815))
        assertThat(SimpleDate(2026, 1, 4).hashCode(), `is`(20260104))
    }

    @Test
    fun `dwie te same daty sa rowne i maja ten sam hashCode`() {
        val pierwsza = SimpleDate(2026, 8, 15)
        val druga = SimpleDate.newInstance("2026_08_15", SimpleDate.ORDER_YMD.toInt())

        assertThat(pierwsza == druga, `is`(true))
        assertThat(pierwsza.hashCode() == druga.hashCode(), `is`(true))
    }

    @Test
    fun `rozne daty nie kolidują hashCode w obrebie stulecia`() {
        // Zamiana miesiąca z dniem to najczęstsza pomyłka przy dwóch porządkach
        // parsowania — musi dawać inny hash, inaczej wpisy zlewałyby się w bazie.
        assertThat(SimpleDate(2026, 8, 15).hashCode() == SimpleDate(2026, 15, 8).hashCode(), `is`(false))

        val hashe = buildSet {
            for (miesiac in 1..12) for (dzien in 1..28) add(SimpleDate(2026, miesiac, dzien).hashCode())
        }
        assertThat(hashe.size, `is`(12 * 28))
    }

    @Test
    fun `equals odrzuca obiekt innej klasy`() {
        // Ten sam defekt, który był w Event: bez sprawdzenia typu porównanie
        // z czymkolwiek o zgodnym hashu wychodziło prawdziwe.
        assertThat(SimpleDate(2026, 8, 15).equals(20260815), `is`(false))
    }

    // ---------- porządek ----------

    @Test
    fun `porzadek idzie rok, potem miesiac, potem dzien`() {
        val posortowane = listOf(
            SimpleDate(2026, 12, 31),
            SimpleDate(2027, 1, 1),
            SimpleDate(2026, 1, 4),
            SimpleDate(2026, 1, 11),
        ).sorted()

        assertThat(
            posortowane.map { "${it.year}_${it.month}_${it.day}" },
            `is`(listOf("2026_1_4", "2026_1_11", "2026_12_31", "2027_1_1"))
        )
    }

    @Test
    fun `compareTo zwraca zero dla tej samej daty`() {
        assertThat(SimpleDate(2026, 8, 15).compareTo(SimpleDate(2026, 8, 15)), `is`(0))
    }

    // ---------- arytmetyka ----------

    @Test
    fun `addDays przechodzi przez koniec miesiaca`() {
        val wynik = SimpleDate(2026, 1, 31).addDays(1)

        assertThat(wynik, `is`(SimpleDate(2026, 2, 1)))
    }

    @Test
    fun `addDays przechodzi przez koniec roku`() {
        val wynik = SimpleDate(2026, 12, 31).addDays(1)

        assertThat(wynik, `is`(SimpleDate(2027, 1, 1)))
    }

    @Test
    fun `addDays z wartoscia ujemna cofa date`() {
        val wynik = SimpleDate(2026, 1, 1).addDays(-1)

        assertThat(wynik, `is`(SimpleDate(2025, 12, 31)))
    }

    @Test
    fun `addDays uwzglednia rok przestepny`() {
        assertThat(SimpleDate(2028, 2, 28).addDays(1), `is`(SimpleDate(2028, 2, 29)))
        assertThat(SimpleDate(2026, 2, 28).addDays(1), `is`(SimpleDate(2026, 3, 1)))
    }

    @Test
    fun `addDays zerem zwraca te sama date`() {
        assertThat(SimpleDate(2026, 8, 15).addDays(0), `is`(SimpleDate(2026, 8, 15)))
    }

    @Test
    fun `getLastDayOfMonth zna dlugosci miesiecy i lata przestepne`() {
        assertThat(SimpleDate.getLastDayOfMonth(2026, 2).day, `is`(28))
        assertThat(SimpleDate.getLastDayOfMonth(2028, 2).day, `is`(29))
        assertThat(SimpleDate.getLastDayOfMonth(2026, 4).day, `is`(30))
        assertThat(SimpleDate.getLastDayOfMonth(2026, 12).day, `is`(31))
    }

    @Test
    fun `yesterday jest o dzien wczesniejsze niz dzisiaj`() {
        val dzis = Calendar.getInstance().run {
            SimpleDate(this[Calendar.YEAR], this[Calendar.MONTH] + 1, this[Calendar.DAY_OF_MONTH])
        }

        assertThat(SimpleDate.yesterday.addDays(1), `is`(dzis))
    }

    // ---------- strefa czasowa ----------

    @Test
    fun `timeInMillis to polnoc UTC, niezaleznie od strefy maszyny`() {
        // asCalendar celowo stoi w UTC. Data bez godziny nie ma strefy, a
        // przeliczanie jej w czasie lokalnym przesuwałoby dzień o jeden przy
        // przekazywaniu do Kalendarza Google i do MaterialDatePicker.
        val oczekiwane = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(2026, Calendar.AUGUST, 15)
        }.timeInMillis

        assertThat(SimpleDate(2026, 8, 15).timeInMillis, `is`(oczekiwane))
    }

    @Test
    fun `timeInMillis nie zmienia sie przy zmianie strefy maszyny`() {
        val wWarszawie = SimpleDate(2026, 8, 15).timeInMillis
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"))

        assertThat(SimpleDate(2026, 8, 15).timeInMillis, `is`(wWarszawie))
    }

    @Test
    fun `weekdayName podaje nazwe dnia tygodnia w jezyku ustawien`() {
        assertThat(SimpleDate(2026, 8, 15).weekdayName, `is`("sobota"))
        assertThat(SimpleDate(2026, 8, 9).weekdayName, `is`("niedziela"))
    }
}
