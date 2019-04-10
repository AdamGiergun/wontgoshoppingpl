package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.widget.DatePicker;

import java.util.Calendar;

import eu.adamgiergun.wontgoshoppingpl.days.ListOfLimitedCommerceDays;

class MyDatePicker extends DatePicker {

    private MyDatePicker(Context context) {
        super(context);
    }

    static MyDatePicker newInstance(Context context, Calendar date) {
        MyDatePicker picker = new MyDatePicker(context);
        picker.setMaxDate(ListOfLimitedCommerceDays.getLastDate(context).getTimeInMillis());
        picker.updateDate(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));
        return picker;
    }

    Calendar getDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getYear());
        cal.set(Calendar.MONTH, getMonth());
        cal.set(Calendar.DAY_OF_MONTH, getDayOfMonth());
        return cal;
    }
}
