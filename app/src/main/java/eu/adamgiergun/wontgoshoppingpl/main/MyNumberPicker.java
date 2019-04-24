package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.widget.NumberPicker;

class MyNumberPicker extends NumberPicker {

    private MyNumberPicker(Context context) {
        super(context);
    }

    static NumberPicker newInstance(Context context, int value) {
        NumberPicker picker = new NumberPicker(context);
        picker.setSaveFromParentEnabled(false);
        picker.setSaveEnabled(false);
        picker.setMinValue(0);
        picker.setMaxValue(9);
        picker.setValue(value);
        return picker;
    }
}
