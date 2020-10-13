package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.day.Day

@BindingAdapter("day", "isLookRich", "density")
internal fun CardView.setLook(item: Day?, isLookRich: Boolean, density: Float) {
    item?.let {
        fun getColor(): Int {
            val colorRes = item.backgroundColorResource
            val resources = context.resources
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                resources.getColor(colorRes, null)
            } else {
                @Suppress("DEPRECATION")
                resources.getColor(colorRes)
            }
        }

        if (isLookRich) {
            setBackgroundResource(item.backgroundResource)
        } else {
            setCardBackgroundColor(getColor())
            radius = 8 * density
            cardElevation = 4 * density
        }
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
    text = if (isAllDay) this.context.getString(R.string.whole_day) else this.context.getString(R.string.since) + time
}

@BindingAdapter("isAddedToCalendar")
internal fun TextView.setAddedToCalendar(isAddedToCalendar: Boolean) {
    if (isAddedToCalendar)
        setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_today, 0, 0, 0)
    else
        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
}

@BindingAdapter("fragmentManager")
internal fun TextView.showDialog(fragmentManager: FragmentManager) {
    setOnClickListener {
        when (id) {
            R.id.numberOfDaysBeforeView -> ReminderDaysBeforeDialog()
            R.id.timeView -> ReminderTimeDialog()
            else -> TillDateDialog()
        }.show(fragmentManager, "")
    }

}