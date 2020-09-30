package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import java.util.*

internal object LocalizedConst {
    lateinit var locale: Locale
    var isSet = false
}

internal fun setLocalizedConst(appContext: Context) {
    if (!LocalizedConst.isSet) {
        LocalizedConst.locale = Locale.getDefault()
        LocalizedConst.isSet = true
    }
}