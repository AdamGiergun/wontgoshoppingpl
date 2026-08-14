package eu.adamgiergun.wontgoshoppingpl.main.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day

/**
 * Rozmiary tekstu wiersza.
 *
 * Zastępują dwanaście wartości `day_text_*` rozsypanych po `values/dimens.xml`
 * i `values-w360dp/dimens.xml`. Powód nie jest oszczędnościowy: `dimensionResource`
 * zwraca `Dp`, a te wartości są w `sp`, więc czytanie ich z zasobów w Compose
 * wymagałoby konwersji przez gęstość — czyli dokładnie tej samej pułapki, przez
 * którą w warstwie widoków trzeba było podawać `TypedValue.COMPLEX_UNIT_PX`
 * zamiast `SP`. Jako `TextUnit` w kodzie problem nie istnieje.
 *
 * Wartości bez zmian: role Material 3 (TitleMedium 16, BodyMedium 14,
 * LabelMedium 12), a warianty wąski i zwarty schodzą o stopień niżej.
 */
private data class RozmiaryTekstu(val nazwa: TextUnit, val data: TextUnit, val godzina: TextUnit)

private fun rozmiary(waski: Boolean, zwarty: Boolean): RozmiaryTekstu = when {
    waski && zwarty -> RozmiaryTekstu(13.sp, 11.sp, 10.sp)
    waski -> RozmiaryTekstu(14.sp, 12.sp, 11.sp)
    zwarty -> RozmiaryTekstu(14.sp, 12.sp, 11.sp)
    else -> RozmiaryTekstu(16.sp, 14.sp, 12.sp)
}

/**
 * Wiersz listy dni.
 *
 * Wybór między układem trzykolumnowym a piętrowym robi [BoxWithConstraints] na
 * podstawie szerokości, którą wiersz **faktycznie dostał** — a nie kwalifikatora
 * katalogu `layout-w360dp`, który patrzy na szerokość ekranu. To różnica, która
 * ma znaczenie przy wężu: tam kolumna ma około 350 dp niezależnie od tego, czy
 * ekran ma 360 dp czy 1280 dp, więc kwalifikator dawał odpowiedź na inne pytanie
 * niż zadane.
 *
 * Powód samego podziału zostaje ten sam co w XML-u: przy około 90 dp na kolumnę
 * nazwy `TextView` łamał słowo w środku („Niedziela n / iehandlowa").
 */
@Composable
internal fun DayRow(
    day: Day,
    zwarty: Boolean,
    wypelniaWysokosc: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tlo = colorResource(day.containerColorResource)
    val tresc = colorResource(day.onContainerColorResource)

    Card(
        modifier = modifier,
        // Kształt z MaterialTheme.shapes.medium to te same 12 dp co dawne
        // `day_card_corner`. Cień 3 dp musi zmieścić się w odstępie między
        // kartami, inaczej lista go przycina — dlatego odstępy w DaysScreen
        // nie schodzą poniżej 12 dp w pionie.
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = tlo, contentColor = tresc),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        onClick = onClick,
    ) {
        BoxWithConstraints(
            modifier = if (wypelniaWysokosc) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
        ) {
            // Próg dotyczy szerokości KARTY, nie ekranu — stąd 320, a nie 360 dp
            // z dawnego kwalifikatora layout-w360dp. Na ekranie 360 dp karta ma
            // około 344 dp (minus wypełnienie listy), a na 320 dp — około 304 dp,
            // więc ten próg odtwarza dotychczasowy podział co do przypadku.
            // Przy wężu kolumna ma około 220 dp i też wypada piętrowo, co jest
            // poprawną odpowiedzią na pytanie, którego kwalifikator nie zadawał.
            val waski = maxWidth < 320.dp
            val r = rozmiary(waski, zwarty)
            // Wypełnienie pionowe i rozmiar tekstu to DWIE różne rzeczy, choć
            // w warstwie widoków sterowała nimi jedna flaga. Kiedy wysokość
            // komórki narzuca układ (wąż), duże wypełnienie niczego nie podnosi —
            // tylko wypycha tekst z karty. Ale rozmiar tekstu ma wtedy nadal iść
            // za preferencją gęstości, inaczej w trybie standardowym wychodzą
            // wysokie karty z małym napisem i pustką dookoła.
            val pionowo = if (zwarty || wypelniaWysokosc) 6.dp else 14.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = pionowo),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (waski) WierszPietrowy(day, r, tresc) else WierszTrzykolumnowy(day, r, tresc)
            }
        }
    }
}

/** Data i godzina w jednej linii, nazwa dnia pod spodem na pełnej szerokości. */
@Composable
private fun WierszPietrowy(day: Day, r: RozmiaryTekstu, tresc: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Data(day, r.data, tresc, Modifier.weight(1f, fill = false))
            Text(
                text = opisGodziny(day),
                style = MaterialTheme.typography.labelMedium,
                fontSize = r.godzina,
                color = tresc,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text(
            text = day.name,
            style = MaterialTheme.typography.titleMedium,
            fontSize = r.nazwa,
            color = tresc,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}

/**
 * Data, godzina i nazwa w jednej linii. Nazwa bierze resztę szerokości i to ona
 * łamie się jako ostatnia — tak jak w XML-u miała jedyna `layout_width="0dp"`.
 */
@Composable
private fun WierszTrzykolumnowy(day: Day, r: RozmiaryTekstu, tresc: Color) {
    // Środkowanie, nie wspólna linia pisma.
    //
    // Data ma dwa wiersze (`dateAndWeekdayString` niesie twardy znak nowej linii),
    // godzina i nazwa po jednym — więc żadne wyrównanie nie jest tu oczywiste.
    // Próba z `alignByBaseline` sadzała wszystko na linii PIERWSZEGO wiersza daty
    // i wychodził wiersz przechylony do góry, z pustym dołem po prawej stronie.
    //
    // Przy środkowaniu kluczowe jest, żeby dwuwierszowa data czytała się jako
    // JEDEN blok, a nie dwie osobne linijki, między którymi coś wisi. Stąd
    // ściśnięta interlinia w [Data]: bliżej siebie oba wiersze tworzą jedną bryłę
    // i środek wypada tam, gdzie oko go szuka.
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Data(day, r.data, tresc, Modifier.widthIn(min = 112.dp))
        Text(
            text = opisGodziny(day),
            style = MaterialTheme.typography.labelMedium,
            fontSize = r.godzina,
            color = tresc,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Text(
            text = day.name,
            style = MaterialTheme.typography.titleMedium,
            fontSize = r.nazwa,
            color = tresc,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Data z dniem tygodnia, poprzedzona znacznikiem „dodane do kalendarza".
 * Ikona jest barwiona kolorem treści wiersza, bo leży na kolorowym tle i nie
 * może polegać na roli motywu.
 */
@Composable
private fun Data(day: Day, rozmiar: TextUnit, tresc: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (day.isAddedToCalendar) {
            Icon(
                painter = painterResource(R.drawable.ic_event_available),
                contentDescription = null,
                tint = tresc,
                modifier = Modifier.padding(end = 4.dp),
            )
        }
        Text(
            text = day.dateAndWeekdayString,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = rozmiar,
            // Interlinia ściśnięta względem domyślnej dla BodyMedium: data i dzień
            // tygodnia mają się czytać jako jeden blok, bo tak działa środkowanie
            // pozostałych kolumn względem nich.
            lineHeight = rozmiar * 1.18f,
            color = tresc,
        )
    }
}

@Composable
private fun opisGodziny(day: Day): String =
    if (day.allDay) stringResource(R.string.whole_day)
    else stringResource(R.string.since) + day.time
