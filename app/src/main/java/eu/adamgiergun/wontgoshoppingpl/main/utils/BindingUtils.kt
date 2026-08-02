package eu.adamgiergun.wontgoshoppingpl.main.utils

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day

internal fun CardView.setLook(day: Day, isLookRich: Boolean, density: Float) {
    if (isLookRich) {
        setBackgroundResource(day.backgroundResource)
    } else {
        setCardBackgroundColor(ResourcesCompat.getColor(resources, day.backgroundColorResource, null))
        radius = 8 * density
        cardElevation = 4 * density
    }
}

internal fun View.setRichBackgroundResource(isLookRich: Boolean) {
    if (isLookRich)
        setBackgroundResource(R.drawable.bg_red)
}

internal fun TextView.setTimeDescription(isAllDay: Boolean, time: String) {
    text = if (isAllDay) context.getString(R.string.whole_day) else context.getString(R.string.since) + time
}

internal fun TextView.setAddedToCalendar(isAddedToCalendar: Boolean) {
    val drawable = if (isAddedToCalendar) android.R.drawable.ic_menu_today else 0
    setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
}
