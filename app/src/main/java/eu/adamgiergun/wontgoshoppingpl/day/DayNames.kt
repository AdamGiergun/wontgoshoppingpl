package eu.adamgiergun.wontgoshoppingpl.day

import android.content.Context
import eu.adamgiergun.wontgoshoppingpl.R

/**
 * Rozwiązuje nazwę dnia z klucza zapisanego w trzecim polu
 * [eu.adamgiergun.wontgoshoppingpl.db.Event.item].
 *
 * Do wersji 5 bazy trzymaliśmy tam gotową, przetłumaczoną nazwę — przez co baza
 * zapamiętywała język wybrany w chwili instalacji i nie reagowała na jego zmianę.
 * Od wersji 6 zapisujemy klucz, a tekst bierzemy z zasobów przy każdym odczycie.
 *
 * Mapowanie jest jawne zamiast [android.content.res.Resources.getIdentifier]:
 * tamto jest przestarzałe od API 29 i nie przeżywa usuwania nieużywanych
 * zasobów przez R8 w buildzie release.
 */
internal object DayNames {

    private val resourceIds = mapOf(
        "non_commerce_sunday" to R.string.day_name_non_commerce_sunday,
        "commerce_sunday" to R.string.day_name_commerce_sunday,
        "new_year" to R.string.day_name_new_year,
        "epiphany" to R.string.day_name_epiphany,
        "holy_saturday" to R.string.day_name_holy_saturday,
        "easter_sunday" to R.string.day_name_easter_sunday,
        "easter_monday" to R.string.day_name_easter_monday,
        "labour_day" to R.string.day_name_labour_day,
        "constitution_day" to R.string.day_name_constitution_day,
        "whit_sunday" to R.string.day_name_whit_sunday,
        "corpus_christi" to R.string.day_name_corpus_christi,
        "assumption" to R.string.day_name_assumption,
        "all_saints" to R.string.day_name_all_saints,
        "independence_day" to R.string.day_name_independence_day,
        "day_after_independence_day" to R.string.day_name_day_after_independence_day,
        "christmas_eve" to R.string.day_name_christmas_eve,
        "christmas_day" to R.string.day_name_christmas_day,
        "second_day_of_christmas" to R.string.day_name_second_day_of_christmas,
    )

    /**
     * Wiersze zapisane przed wersją 6 mają w tym polu przetłumaczoną nazwę, a nie
     * klucz — wtedy zwracamy je bez zmian. W praktyce dotyczy to wyłącznie dat
     * z lat 2020-2023, których i tak nie widać, bo zapytania filtrują `item >= dziś`.
     */
    fun resolve(context: Context, token: String): String =
        resourceIds[token]?.let(context::getString) ?: token
}
