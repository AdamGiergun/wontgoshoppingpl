package eu.adamgiergun.wontgoshoppingpl.settings

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.R
import androidx.appcompat.app.AlertDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import eu.adamgiergun.wontgoshoppingpl.databinding.IntegerPickerDialogBinding

/**
 * Wybór liczby całkowitej z zakresu preferencji.
 *
 * Zamiast platformowego `NumberPicker` — rzędem żetonów, po jednym na wartość.
 * Uzasadnienie przy layoucie `integer_picker_dialog.xml`; w skrócie: walec był
 * w stylu sprzed Material Design, pokazywał trzy wartości naraz i wymagał
 * przewijania po to, żeby wybrać jedną z ośmiu.
 *
 * Rozwiązanie zakłada zakres na tyle mały, że mieści się na ekranie — dla
 * preferencji dni przed wydarzeniem to 0-7. Przy zakresie idącym w setki żetony
 * przestałyby mieć sens i trzeba by wrócić do przewijanej listy.
 */
class IntegerPickerDialog(
        context: Context,
        private val onValueSetListener: OnValueSetListener,
        value: Int,
        private val minValue: Int,
        private val maxValue: Int,
        themeResId: Int = 0)
    : AlertDialog(context, resolveDialogTheme(context, themeResId)),
        DialogInterface.OnClickListener {

    interface OnValueSetListener {
        fun onValueSet(value: Int)
    }

    private val chipGroup: ChipGroup

    private val wartoscPoczatkowa = value

    /**
     * Wybrana wartość odczytywana z zaznaczonego żetonu.
     *
     * `ChipGroup` ma `selectionRequired`, więc zaznaczony jest zawsze dokładnie
     * jeden — ale gdyby kiedyś przestał, zapasem jest wartość wyjściowa.
     *
     * Nie ma tu odpowiednika dawnego `numberPicker.clearFocus()` przed odczytem:
     * był konieczny, bo `NumberPicker` w trybie wpisywania trzymał
     * niezatwierdzoną wartość w polu tekstowym. Żeton albo jest zaznaczony,
     * albo nie — nie ma stanu pośredniego, więc odpadła też cała nadpisana
     * metoda `show()`, która to obsługiwała.
     */
    private val wybrana: Int
        get() = chipGroup.checkedChipId
            .takeIf { it != View.NO_ID }
            ?.let { chipGroup.findViewById<Chip>(it).text.toString().toIntOrNull() }
            ?: wartoscPoczatkowa

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            BUTTON_POSITIVE -> onValueSetListener.onValueSet(wybrana)
            BUTTON_NEGATIVE -> cancel()
        }
    }

    override fun onSaveInstanceState(): Bundle {
        return super.onSaveInstanceState().apply { putInt(VALUE, wybrana) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        zaznacz(savedInstanceState.getInt(VALUE))
    }

    private fun zaznacz(wartosc: Int) {
        val indeks = wartosc - minValue
        if (indeks in 0 until chipGroup.childCount) {
            chipGroup.check(chipGroup.getChildAt(indeks).id)
        }
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

        chipGroup = binding.chipGroup
        (minValue..maxValue).forEach { wartosc ->
            val chip = inflater.inflate(
                eu.adamgiergun.wontgoshoppingpl.R.layout.integer_picker_chip,
                chipGroup,
                false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = wartosc.toString()
            chipGroup.addView(chip)
        }
        zaznacz(value)
    }
}
