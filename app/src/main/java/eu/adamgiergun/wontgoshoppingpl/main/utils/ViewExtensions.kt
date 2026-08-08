package eu.adamgiergun.wontgoshoppingpl.main.utils

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import com.google.android.material.card.MaterialCardView
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day

/**
 * Nadaje wierszowi kolor typu dnia i wybraną gęstość, zwracając kolor treści
 * pasujący do tła.
 *
 * Zastąpiło dawne `setLook`, które przełączało między gradientowym drawable
 * („rich") a płaskim kolorem („mini"), ustawiając zaokrąglenie i cień
 * imperatywnie. Kolor niesie tu jedyną informację, jaka się liczy — czy w danym
 * dniu można zrobić zakupy — więc ma osobne warianty dla trybu jasnego
 * i ciemnego, a nie dwa style dekoracyjne.
 */
internal fun MaterialCardView.applyDayLook(day: Day, isCompact: Boolean): Int {
    setCardBackgroundColor(
        ResourcesCompat.getColor(resources, day.containerColorResource, context.theme)
    )
    val pionowy = resources.getDimensionPixelSize(
        if (isCompact) R.dimen.day_row_padding_vertical_compact
        else R.dimen.day_row_padding_vertical
    )
    val poziomy = resources.getDimensionPixelSize(R.dimen.day_row_padding_horizontal)
    getChildAt(0)?.updatePadding(left = poziomy, top = pionowy, right = poziomy, bottom = pionowy)

    return ResourcesCompat.getColor(resources, day.onContainerColorResource, context.theme)
}

internal fun TextView.setTimeDescription(isAllDay: Boolean, time: String) {
    text = if (isAllDay) context.getString(R.string.whole_day) else context.getString(R.string.since) + time
}

/**
 * Znacznik „dodane do kalendarza". Ikona jest barwiona kolorem treści wiersza,
 * bo leży na kolorowym tle i nie może polegać na atrybucie motywu.
 */
internal fun TextView.setAddedToCalendar(isAddedToCalendar: Boolean, contentColor: Int) {
    setCompoundDrawablesWithIntrinsicBounds(
        if (isAddedToCalendar) R.drawable.ic_event_available else 0, 0, 0, 0
    )
    if (isAddedToCalendar) {
        TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(contentColor))
    }
}
