package eu.adamgiergun.wontgoshoppingpl.settings

import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.TimePicker
import androidx.preference.DialogPreference
import eu.adamgiergun.wontgoshoppingpl.R
import eu.adamgiergun.wontgoshoppingpl.calendar.SimpleTime

@Suppress("unused")
class TimePreference : DialogPreference, OnTimeSetListener {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    init {
        summaryProvider = SummaryProvider<TimePreference> { it.time.toString() }
    }

    val time: SimpleTime
        get() = SimpleTime(
                getPersistedString(
                        context.getString(R.string.default_value_for_time_preference)))

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index) as Any
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue is String) persistString(defaultValue)
    }

    override fun onTimeSet(timePicker: TimePicker, hour: Int, minute: Int) {
        with(SimpleTime(hour, minute).toString()) {
            persistString(this)
            notifyChanged()
        }
    }
}