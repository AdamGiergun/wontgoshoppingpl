package eu.adamgiergun.wontgoshoppingpl.main

import android.os.Build
import android.view.View
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
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