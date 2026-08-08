package eu.adamgiergun.wontgoshoppingpl.common

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.util.regex.Pattern

/**
 * Wartości tych zasobów są kluczami w [android.content.SharedPreferences] i wartościami
 * wartowniczymi zapisanymi na urządzeniach użytkowników. Przyjechały z wchłoniętej
 * biblioteki `myresources`, gdzie żyły od lat.
 *
 * Zmiana nazwy zasobu jest bezpieczna. **Zmiana jego wartości kasuje ustawienia
 * użytkownika** — a w przypadku `first_run` uruchamia ponowną inicjalizację bazy
 * i zrywa powiązania z wydarzeniami w kalendarzu Google.
 *
 * Test czyta `strings.xml` wprost, bo nie ma tu Androida i nie da się sięgnąć po `R`.
 */
internal class PreferenceKeysTest {

    private val oczekiwane = mapOf(
        "use_google_calendar_preference_key" to "use_calendar",
        "default_calendar_preference_key" to "default_calendar",
        "default_reminder_time_preference_key" to "default_time_for_reminder",
        "default_reminder_days_before_preference_key" to "Default_number_of_days_before_event_for_reminder",
        "first_run_preference_key" to "first_run",
        "use_localized_digits_key" to "use_localized_digits",
        "id_for_default_calendar_not_set" to "-1",
        "id_for_no_calendars_available" to "-2",
        "default_reminder_time_preference_default_value" to "10:00",
        "default_value_for_time_preference" to "12:00",
    )

    private val stringsXml: String by lazy {
        // Katalogiem roboczym testów jest moduł `app`.
        File("src/main/res/values/strings.xml").readText()
    }

    private fun wartosc(nazwa: String): String? {
        val m = Pattern
            .compile("""<string name="$nazwa"[^>]*>(.*?)</string>""", Pattern.DOTALL)
            .matcher(stringsXml)
        return if (m.find()) m.group(1) else null
    }

    @Test
    @DisplayName("Wartości kluczy SharedPreferences nie zmieniły się")
    fun kluczeMajaNiezmienioneWartosci() {
        oczekiwane.forEach { (nazwa, oczekiwanaWartosc) ->
            assertThat(
                "Zasób $nazwa musi mieć wartość '$oczekiwanaWartosc' — " +
                        "od niej zależą dane zapisane na urządzeniach użytkowników",
                wartosc(nazwa), `is`(oczekiwanaWartosc)
            )
        }
    }
}
