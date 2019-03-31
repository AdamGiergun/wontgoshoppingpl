package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.os.Build;
import android.widget.TimePicker;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;

class MyTimePicker extends TimePicker {

    public MyTimePicker(Context context) {
        super(context);
    }

    void initialize(SimpleTime time) {
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
