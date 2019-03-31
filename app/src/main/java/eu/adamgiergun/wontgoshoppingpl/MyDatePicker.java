package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.DatePicker;

import androidx.annotation.RequiresApi;

import java.util.Calendar;

class MyDatePicker extends DatePicker {

    public MyDatePicker(Context context) {
        super(context);
        setMaxDate(getMyMaxDate());
    }

    private long getMyMaxDate() {
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(Calendar.YEAR, 2019);
        maxDate.set(Calendar.MONTH, 11);
        maxDate.set(Calendar.DAY_OF_MONTH, 31);
        return maxDate.getTimeInMillis();
    }

    Calendar getDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getYear());
        cal.set(Calendar.MONTH, getMonth());
        cal.set(Calendar.DAY_OF_MONTH, getDayOfMonth());
        return cal;
    }
}
