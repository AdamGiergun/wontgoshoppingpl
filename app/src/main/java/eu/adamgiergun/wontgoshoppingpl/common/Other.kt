package eu.adamgiergun.wontgoshoppingpl.common

import java.util.*

internal val today
    get() =
        StringBuilder().run {
            fun get0ifLessThen10(value: Int): String {
                return if (value < 10) "0" else ""
            }

            val today = Calendar.getInstance()
            val month = today.get(Calendar.MONTH) + 1
            val dayOfMonth = today.get(Calendar.DAY_OF_MONTH)

            append(today.get(Calendar.YEAR))
            append("_")
            append(get0ifLessThen10(month))
            append(month)
            append("_")
            append(get0ifLessThen10(dayOfMonth))
            append(dayOfMonth)
            toString()
        }