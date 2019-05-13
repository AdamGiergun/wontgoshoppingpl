package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.widget.DatePicker;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate;

class MyDatePicker extends DatePicker {

    private MyDatePicker(Context context) {
        super(context);
    }

    static MyDatePicker newInstance(Context context, SimpleDate date, long maxDate) {
        MyDatePicker picker = new MyDatePicker(context);
        picker.setMaxDate(maxDate);
        picker.updateDate(date.getYear(), date.getMonth(), date.getDay());
        return picker;
    }

    SimpleDate getDate() {
        return SimpleDate.newInstance(getYear(), getMonth()+1, getDayOfMonth());
    }
}
