package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day

@BindingAdapter("day", "isLookRich", "density")
internal fun CardView.setLook(day: Day, isLookRich: Boolean, density: Float) {
    if (isLookRich) {
        setBackgroundResource(day.backgroundResource)
    } else {
        val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(day.backgroundColorResource, null)
        } else {
            @Suppress("DEPRECATION")
            resources.getColor(day.backgroundColorResource)
        }
        setCardBackgroundColor(color)
        radius = 8 * density
        cardElevation = 4 * density
    }
}

@BindingAdapter("isLookRich")
internal fun View.setRichBackgroundResource(isLookRich: Boolean) {
    if (isLookRich)
        setBackgroundResource(R.drawable.bg_red)
}

@BindingAdapter("navGraphId")
internal fun FragmentContainerView.bindNavigation(navGraphId: Int) {
    findNavController().setGraph(navGraphId)
}

@BindingAdapter("isAllDay", "time")
internal fun TextView.setTimeDescription(isAllDay: Boolean, time: String) {
    text = if (isAllDay) context.getString(R.string.whole_day) else context.getString(R.string.since) + time
}

@BindingAdapter("isAddedToCalendar")
internal fun TextView.setAddedToCalendar(isAddedToCalendar: Boolean) {
    val drawable = if (isAddedToCalendar) android.R.drawable.ic_menu_today else 0
    setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
}