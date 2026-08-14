package eu.adamgiergun.wontgoshoppingpl.main.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import eu.adamgiergun.wontgoshoppingpl.R

/**
 * Motyw wyspy Compose.
 *
 * Kolory czytane są z `values/colors.xml`, a nie zapisane w Kotlinie, i to jest
 * tu decyzja, nie wygoda. Te same role nosi motyw XML całej reszty aplikacji
 * (`Theme.Material3.DayNight` w themes.xml) oraz widget, który jako
 * `RemoteViews` może sięgnąć wyłącznie po zasoby. Zapisanie palety drugi raz
 * w kodzie dałoby dwa źródła prawdy, które rozjechałyby się przy pierwszej
 * zmianie — a paleta dni już raz była poprawiana osobno dla trybu jasnego
 * i ciemnego i wtedy właśnie się rozjechała.
 *
 * Wariant nocny wybiera [colorResource] po konfiguracji, czyli tak samo jak
 * w warstwie widoków. Nie ma tu `isSystemInDarkTheme()` z osobną paletą.
 *
 * Typografia zostaje domyślna z Material 3 — role TitleMedium / BodyMedium /
 * LabelMedium mają te same wartości co style `DayRow*` w styles.xml.
 */
@Composable
internal fun WgsTheme(content: @Composable () -> Unit) {
    val ciemny = androidx.compose.foundation.isSystemInDarkTheme()
    val schemat = (if (ciemny) darkColorScheme() else lightColorScheme()).copy(
        primary = colorResource(R.color.md_primary),
        onPrimary = colorResource(R.color.md_on_primary),
        primaryContainer = colorResource(R.color.md_primary_container),
        onPrimaryContainer = colorResource(R.color.md_on_primary_container),
        secondary = colorResource(R.color.md_secondary),
        onSecondary = colorResource(R.color.md_on_secondary),
        secondaryContainer = colorResource(R.color.md_secondary_container),
        onSecondaryContainer = colorResource(R.color.md_on_secondary_container),
        background = colorResource(R.color.md_background),
        onBackground = colorResource(R.color.md_on_background),
        surface = colorResource(R.color.md_surface),
        onSurface = colorResource(R.color.md_on_surface),
        surfaceVariant = colorResource(R.color.md_surface_variant),
        onSurfaceVariant = colorResource(R.color.md_on_surface_variant),
        outline = colorResource(R.color.md_outline),
        error = colorResource(R.color.md_error),
        onError = colorResource(R.color.md_on_error),
    )
    MaterialTheme(colorScheme = schemat, content = content)
}
