package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.widget.NumberPicker;

class MyNumberPicker extends NumberPicker {

    private MyNumberPicker(Context context) {
        super(context);
    }

    static MyNumberPicker newInstance(Context context, int value) {
        MyNumberPicker picker = new MyNumberPicker(context);
        picker.setMinValue(0);
        picker.setMaxValue(9);
        picker.setValue(value);
        return picker;
    }
}
