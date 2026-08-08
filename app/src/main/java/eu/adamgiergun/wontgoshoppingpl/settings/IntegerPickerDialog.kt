package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.appcompat.R
import androidx.appcompat.app.AlertDialog
import eu.adamgiergun.wontgoshoppingpl.databinding.IntegerPickerDialogBinding

class IntegerPickerDialog(
        context: Context,
        private val onValueSetListener: OnValueSetListener,
        value: Int,
        minValue: Int,
        maxValue: Int,
        themeResId: Int = 0)
    : AlertDialog(context, resolveDialogTheme(context, themeResId)),
        DialogInterface.OnClickListener {

    interface OnValueSetListener {
        fun onValueSet(value: Int)
    }

    private val numberPicker: NumberPicker

    override fun show() {
        super.show()
        getButton(BUTTON_POSITIVE).setOnClickListener {
            onClick(this@IntegerPickerDialog, BUTTON_POSITIVE)
            numberPicker.clearFocus()
            dismiss()
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            BUTTON_POSITIVE -> onValueSetListener.onValueSet(numberPicker.value)
            BUTTON_NEGATIVE -> cancel()
        }
    }

    override fun onSaveInstanceState(): Bundle {
        return super.onSaveInstanceState().apply {
            putInt(VALUE, numberPicker.value)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        numberPicker.value = savedInstanceState.getInt(VALUE)
    }

    companion object {
        private const val VALUE = "value"

        private fun resolveDialogTheme(context: Context, resId: Int): Int {
            return if (resId == 0) {
                TypedValue().run {
                    context.theme.resolveAttribute(R.attr.alertDialogTheme, this, true)
                    resourceId
                }
            } else {
                resId
            }
        }
    }

    init {
        val inflater = LayoutInflater.from(context)
        val binding = IntegerPickerDialogBinding.inflate(inflater)
        setView(binding.root)
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this)
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this)
        numberPicker = binding.numberPicker.apply {
            isSaveFromParentEnabled = false
            isSaveEnabled = false
            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
            this.maxValue = maxValue
            this.minValue = minValue
            this.value = value
        }
    }
}