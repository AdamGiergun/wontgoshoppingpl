package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.preference.DialogPreference
import eu.adamgiergun.wontgoshoppingpl.R

class IntegerDialogPreference : DialogPreference, IntegerPickerDialog.OnValueSetListener {

    var minValue = 0
        private set

    var maxValue = 9999
        private set

    var defValue = 0
        private set

    val value: Int
        get() = getPersistedInt(defValue)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(attrs)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(attrs)
    }

    private fun initialize(attrs: AttributeSet) {
        context.withStyledAttributes(attrs, R.styleable.IntegerDialogPreference) {
            minValue = getInt(R.styleable.IntegerDialogPreference_minValue, 0)
            maxValue = getInt(R.styleable.IntegerDialogPreference_maxValue, 999)
            defValue = getInt(R.styleable.IntegerDialogPreference_defValue, 999)
        }
        summaryProvider = SummaryProvider<IntegerDialogPreference> { it.value.toString() }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index)?.toInt() as Any
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue is Int) persistInt(defaultValue)
    }

    override fun onValueSet(value: Int) {
        if (persistInt(value)) notifyChanged()
    }
}