package eu.adamgiergun.wontgoshoppingpl.main;

import android.content.Context;
import android.os.Build;
import android.widget.TimePicker;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;

class MyTimePicker extends TimePicker {

    private MyTimePicker(Context context) {
        super(context);
    }

    static MyTimePicker newInstance(Context context, SimpleTime time) {
        MyTimePicker picker = new MyTimePicker(context);
        picker.initialize(time);
        return picker;
    }

    private void initialize(SimpleTime time) {
        setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setHour(time.getHour());
            setMinute(time.getMinute());
        } else {
            setCurrentHour(time.getHour());
            setCurrentMinute(time.getMinute());
        }
    }

    SimpleTime getTime() {
        SimpleTime time;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            time = new SimpleTime(getHour(), getMinute());
        } else {
            time = new SimpleTime(getCurrentHour(), getCurrentMinute());
        }
        return time;
    }
}
