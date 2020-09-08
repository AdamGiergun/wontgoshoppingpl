package eu.adamgiergun.wontgoshoppingpl.common

import java.util.*

object LocalizedConst {
    @JvmStatic
    var wholeDay: String? = null
        get() = if (field == null) "" else field

    @JvmStatic
    var since: String? = null
        get() = if (field == null) "" else field

    var askForLook: String? = null
        get() = if (field == null) "" else field

    var isSet = false

    var locale: Locale? = null
}