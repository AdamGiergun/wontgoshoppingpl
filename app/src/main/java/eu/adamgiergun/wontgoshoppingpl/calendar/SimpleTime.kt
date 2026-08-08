package eu.adamgiergun.wontgoshoppingpl.calendar

class SimpleTime {
    val hour: Int
    val minute: Int

    constructor(time: String) {
        val pieces = time.split(":".toRegex()).toTypedArray()
        hour = pieces[0].toInt()
        minute = pieces[1].toInt()
    }

    constructor(hour: Int, minute: Int) {
        this.hour = hour
        this.minute = minute
    }

    override fun toString(): String {
        return StringBuilder().run {
            append(hour.toString())
            append(":")
            if (minute < 10) append("0")
            append(minute.toString())
            toString()
        }
    }
}