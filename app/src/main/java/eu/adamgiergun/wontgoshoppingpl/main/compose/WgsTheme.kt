package eu.adamgiergun.wontgoshoppingpl.main.compose

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.appcompat.R as AppCompatR
import com.google.android.material.R as MaterialR

/**
 * Motyw wyspy Compose.
 *
 * Kolory pochodzą z **ról motywu aktywności**, a nie z zasobów wskazanych po
 * nazwie. To jest zmiana wymuszona przez zestawy kolorystyczne: aktywność
 * podmienia motyw zależnie od preferencji, więc wyspa Compose musi iść za nim,
 * a nie powtarzać u siebie wyboru zestawu. Odczyt z zasobów działałby dopóty,
 * dopóki zestaw był jeden.
 *
 * Kolory dni to osobna sprawa — te idą z zasobów przez [eu.adamgiergun.wontgoshoppingpl.day.Day],
 * bo te same wartości musi widzieć widget, a RemoteViews motywu nie znają.
 *
 * Typografia zostaje domyślna z Material 3: role TitleMedium / BodyMedium /
 * LabelMedium mają te same wartości co style `DayRow*` z warstwy widoków.
 */
@Composable
internal fun WgsTheme(content: @Composable () -> Unit) {
    // colorPrimary i colorError deklaruje appcompat, pozostałe role Material.
    // Podział jest historyczny i nie da się go zgadnąć — sprawdzane w R.txt
    // wewnątrz AAR-ów, nie w dokumentacji.
    val context = LocalContext.current
    val konfiguracja = LocalConfiguration.current
    // Klucz obejmuje konfigurację, bo przy zmianie trybu nocnego te same
    // atrybuty rozwiązują się na inne wartości.
    val schemat = remember(context.theme, konfiguracja) { schematZMotywu(context) }
    MaterialTheme(colorScheme = schemat, content = content)
}

private fun schematZMotywu(context: Context): ColorScheme {
    fun rola(@AttrRes attr: Int, zapas: Color): Color {
        val wartosc = TypedValue()
        if (!context.theme.resolveAttribute(attr, wartosc, true)) return zapas
        return Color(
            if (wartosc.resourceId != 0) androidx.core.content.ContextCompat.getColor(context, wartosc.resourceId)
            else wartosc.data
        )
    }

    val ciemny = (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
    val bazowy = if (ciemny) darkColorScheme() else lightColorScheme()

    return bazowy.copy(
        primary = rola(AppCompatR.attr.colorPrimary, bazowy.primary),
        onPrimary = rola(MaterialR.attr.colorOnPrimary, bazowy.onPrimary),
        primaryContainer = rola(MaterialR.attr.colorPrimaryContainer, bazowy.primaryContainer),
        onPrimaryContainer = rola(MaterialR.attr.colorOnPrimaryContainer, bazowy.onPrimaryContainer),
        secondary = rola(MaterialR.attr.colorSecondary, bazowy.secondary),
        onSecondary = rola(MaterialR.attr.colorOnSecondary, bazowy.onSecondary),
        secondaryContainer = rola(MaterialR.attr.colorSecondaryContainer, bazowy.secondaryContainer),
        onSecondaryContainer = rola(MaterialR.attr.colorOnSecondaryContainer, bazowy.onSecondaryContainer),
        tertiary = rola(MaterialR.attr.colorTertiary, bazowy.tertiary),
        onTertiary = rola(MaterialR.attr.colorOnTertiary, bazowy.onTertiary),
        background = rola(android.R.attr.colorBackground, bazowy.background),
        onBackground = rola(MaterialR.attr.colorOnBackground, bazowy.onBackground),
        surface = rola(MaterialR.attr.colorSurface, bazowy.surface),
        onSurface = rola(MaterialR.attr.colorOnSurface, bazowy.onSurface),
        surfaceVariant = rola(MaterialR.attr.colorSurfaceVariant, bazowy.surfaceVariant),
        onSurfaceVariant = rola(MaterialR.attr.colorOnSurfaceVariant, bazowy.onSurfaceVariant),
        outline = rola(MaterialR.attr.colorOutline, bazowy.outline),
        outlineVariant = rola(MaterialR.attr.colorOutlineVariant, bazowy.outlineVariant),
        error = rola(AppCompatR.attr.colorError, bazowy.error),
        onError = rola(MaterialR.attr.colorOnError, bazowy.onError),
    )
}
