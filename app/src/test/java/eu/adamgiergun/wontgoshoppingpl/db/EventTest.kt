package eu.adamgiergun.wontgoshoppingpl.db

import eu.adamgiergun.wontgoshoppingpl.day.Day
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

/**
 * Tożsamość wpisu w bazie.
 *
 * `Event` łamie domyślne `equals` wygenerowane dla `data class`: wpisy porównują
 * się **wyłącznie po dacie**, z pominięciem typu dnia, identyfikatora i pól
 * kalendarza. To świadoma decyzja — w jednym dniu jest najwyżej jedno wydarzenie,
 * a odświeżenie danych ma rozpoznać „ten sam dzień" nawet gdy zmienił się jego
 * typ. Ale ponieważ jest to zachowanie zaskakujące, musi być przypięte testem:
 * na tym porównaniu stoi `DiffUtil` listy i `upsertAll` przy imporcie
 * z Remote Config.
 *
 * Czysty JVM — `Event` to encja Room, ale te trzy metody nie dotykają Androida.
 */
internal class EventTest {

    private fun wpis(
        item: String,
        typ: Day.Type = Day.Type.NON_COMMERCE_DAY,
        id: Int? = null,
        calendarId: String? = null,
        calendarEventId: Long? = null,
    ) = Event(id, typ.code, item, calendarId, calendarEventId)

    @Test
    fun `wpisy o tej samej dacie sa rowne mimo roznego typu dnia`() {
        val niedziela = wpis("2026_08_15,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY)
        val swieto = wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY)

        assertThat(niedziela == swieto, `is`(true))
        assertThat(niedziela.hashCode() == swieto.hashCode(), `is`(true))
    }

    @Test
    fun `identyfikator i powiazanie z kalendarzem nie wplywaja na tozsamosc`() {
        val swiezy = wpis("2026_08_15,00:00,assumption")
        val zapisany = wpis("2026_08_15,00:00,assumption", id = 7, calendarId = "3", calendarEventId = 99L)

        assertThat(swiezy == zapisany, `is`(true))
    }

    @Test
    fun `wpisy o roznych datach nie sa rowne`() {
        val pierwszy = wpis("2026_08_15,00:00,assumption")
        val drugi = wpis("2026_08_16,00:00,non_commerce_sunday")

        assertThat(pierwszy == drugi, `is`(false))
    }

    @Test
    fun `equals odrzuca obiekt innej klasy o zgodnym hashu`() {
        // Regresja z Etapu 0: bez sprawdzenia typu `other.hashCode()`
        // rozwiązywało się do rozszerzenia `Any?.hashCode()` i porównanie
        // z dowolnym obiektem o tym samym hashu wychodziło prawdziwe.
        val wpis = wpis("2026_08_15,00:00,assumption")

        assertThat(wpis.equals(20260815), `is`(false))
        assertThat(wpis.equals(null), `is`(false))
    }

    @Test
    fun `porzadek wpisow jest chronologiczny`() {
        val posortowane = listOf(
            wpis("2027_01_01,00:00,new_year"),
            wpis("2026_01_04,00:00,non_commerce_sunday"),
            wpis("2026_12_20,00:00,commerce_sunday"),
        ).sorted()

        assertThat(
            posortowane.map { it.item.substringBefore(",") },
            `is`(listOf("2026_01_04", "2026_12_20", "2027_01_01"))
        )
    }

    @Test
    fun `compareTo zwraca zero dla wpisow o tej samej dacie`() {
        val pierwszy = wpis("2026_08_15,00:00,assumption", Day.Type.HOLIDAY)
        val drugi = wpis("2026_08_15,00:00,non_commerce_sunday", Day.Type.NON_COMMERCE_DAY)

        assertThat(pierwszy.compareTo(drugi), `is`(0))
    }

    @Test
    fun `porzadek jest spojny z equals`() {
        // Kontrakt Comparable: compareTo == 0 wtedy i tylko wtedy, gdy equals.
        // Room i DiffUtil zakładają jedno i drugie, a tu obie metody liczą się
        // z tego samego hashu, więc rozjazd byłby cichy.
        val daty = listOf("2026_01_04", "2026_08_15", "2026_12_20", "2027_01_01")
            .map { wpis("$it,00:00,klucz") }

        for (a in daty) for (b in daty) {
            assertThat("$a vs $b", (a.compareTo(b) == 0), `is`(a == b))
        }
    }

    @Test
    fun `trzecie pole nie wchodzi do tozsamosci, wiec stare wiersze z nazwa zamiast klucza pasuja`() {
        // Baza użytkownika sprzed wersji 6 trzyma w trzecim polu gotową nazwę,
        // a nie klucz. Import z Remote Config musi rozpoznać taki wiersz jako
        // ten sam dzień, inaczej upsertAll zdublowałby go zamiast zaktualizować.
        val stary = wpis("2026_08_15,00:00,Wniebowzięcie Najświętszej Maryi Panny")
        val nowy = wpis("2026_08_15,00:00,assumption")

        assertThat(stary == nowy, `is`(true))
    }
}
