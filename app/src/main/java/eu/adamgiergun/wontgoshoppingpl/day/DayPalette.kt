package eu.adamgiergun.wontgoshoppingpl.day

import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences.ColourSet

/**
 * Odwzorowanie (zestaw kolorystyczny, typ dnia) na zasoby.
 *
 * Wydzielone z [Day] świadomie. Dzień jest wpisem z bazy i nie ma powodu wiedzieć,
 * którą paletę wybrał użytkownik — a odczyt preferencji w jego konstruktorze
 * odbierał mu testowalność, bo atrapa kontekstu nie obsłuży `PreferenceManager`.
 * Zestaw czyta więc ten, kto rysuje: wiersz listy albo widget.
 *
 * Zasoby, a nie atrybuty motywu, bo te same wartości musi widzieć widget —
 * a `RemoteViews` motywu nie znają.
 */
internal object DayPalette {

    @ColorRes
    fun container(type: Day.Type, set: ColourSet): Int = when (set) {
        ColourSet.PIGMENTS -> when (type) {
            Day.Type.HOLIDAY -> R.color.p1_day_holiday_container
            Day.Type.NON_COMMERCE_DAY -> R.color.p1_day_non_commerce_container
            Day.Type.COMMERCE_SUNDAY -> R.color.p1_day_commerce_container
        }
        ColourSet.CANDY -> when (type) {
            Day.Type.HOLIDAY -> R.color.p2_day_holiday_container
            Day.Type.NON_COMMERCE_DAY -> R.color.p2_day_non_commerce_container
            Day.Type.COMMERCE_SUNDAY -> R.color.p2_day_commerce_container
        }
    }

    @ColorRes
    fun onContainer(type: Day.Type, set: ColourSet): Int = when (set) {
        ColourSet.PIGMENTS -> when (type) {
            Day.Type.HOLIDAY -> R.color.p1_day_on_holiday_container
            Day.Type.NON_COMMERCE_DAY -> R.color.p1_day_on_non_commerce_container
            Day.Type.COMMERCE_SUNDAY -> R.color.p1_day_on_commerce_container
        }
        ColourSet.CANDY -> when (type) {
            Day.Type.HOLIDAY -> R.color.p2_day_on_holiday_container
            Day.Type.NON_COMMERCE_DAY -> R.color.p2_day_on_non_commerce_container
            Day.Type.COMMERCE_SUNDAY -> R.color.p2_day_on_commerce_container
        }
    }

    /**
     * Layout widgetu. Sześć wariantów to trzy typy dnia razy dwa zestawy —
     * podział konieczny, bo RemoteViews muszą dostać kolory zasobami.
     */
    @LayoutRes
    fun widgetLayout(type: Day.Type, set: ColourSet): Int = when (set) {
        ColourSet.PIGMENTS -> when (type) {
            Day.Type.HOLIDAY -> R.layout.app_widget_p1_holiday
            Day.Type.NON_COMMERCE_DAY -> R.layout.app_widget_p1_non_commerce
            Day.Type.COMMERCE_SUNDAY -> R.layout.app_widget_p1_commerce
        }
        ColourSet.CANDY -> when (type) {
            Day.Type.HOLIDAY -> R.layout.app_widget_p2_holiday
            Day.Type.NON_COMMERCE_DAY -> R.layout.app_widget_p2_non_commerce
            Day.Type.COMMERCE_SUNDAY -> R.layout.app_widget_p2_commerce
        }
    }
}
