package eu.adamgiergun.wontgoshoppingpl.common

import android.content.Context
import eu.adamgiergun.wontgoshoppingpl.R
import java.util.*

internal object LocalizedConst {

    lateinit var askForLook: String
    lateinit var locale: Locale
    lateinit var since: String
    lateinit var wholeDay: String
    var isSet = false
}

internal fun setLocalizedConst(appContext: Context) {
    if (!LocalizedConst.isSet) {
        LocalizedConst.locale = Locale.getDefault()

        LocalizedConst.wholeDay = appContext.getString(R.string.whole_day)
        LocalizedConst.since = appContext.getString(R.string.since)
        LocalizedConst.askForLook = appContext.getString(R.string.ask_for_look)
        LocalizedConst.isSet = true
    }
}