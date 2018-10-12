package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;

import androidx.annotation.NonNull;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleCalendarEvent;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;

import java.text.DateFormat;
import java.util.Calendar;

class LimitedCommerceDay implements Comparable<LimitedCommerceDay>{
    private static int DATE_STRING_END = 10;

    private String name;
    private Calendar calendar;
    private SimpleTime time;
    private int reminderDaysBefore;
    private SimpleTime reminderTime;
    private boolean isHoliday;

    LimitedCommerceDay(String item) {
        String[] pieces = item.split(",");
        time = new SimpleTime(pieces[1]);
        setDay(pieces[0]);
        name = pieces[2];
    }

    private void setDay(String date) {
        calendar = Calendar.getInstance();
        String[] subpieces = date.split("_");

        int year = Integer.valueOf(subpieces[0]);
        calendar.set(Calendar.YEAR, year);

        int day = Integer.valueOf(subpieces[1]);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        int month = Integer.valueOf(subpieces[2]) - 1;
        calendar.set(Calendar.MONTH, month);

        calendar.set(Calendar.HOUR, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public int compareTo(@NonNull LimitedCommerceDay another) {
        return this.calendar.compareTo(another.calendar);
    }

    Calendar getCalendar() {
        return calendar;
    }

    String getDayString() {
        return DateFormat.getDateInstance().format(calendar.getTime());
    }

    String getTimeString(Context context) {
        if (time.getHour() == 0 && time.getMinute() == 0)
            return context.getString(R.string.whole_day);
        else
            return context.getString(R.string.since) + time.toString();
    }

    String getName() {
        return name;
    }

    void setReminderDaysBefore (int daysBefore) {
        reminderDaysBefore = daysBefore;
    }

    void setReminderTime (SimpleTime time) {
        reminderTime = time;
    }

    boolean addToGoogleCalendar(Context context) {
        SimpleCalendarEvent calendarEvent = new SimpleCalendarEvent(getDefaultCalendarIdFromPreferences(context));
        calendarEvent.setTitle(name + " - " + context.getString(R.string.make_purchases_earlier));
        //calendarEvent.addReminder();
        return true;
    }

    private String getDefaultCalendarIdFromPreferences(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        return appPreferences.getDefaultCalendarId(context);
    }

    void setHoliday(boolean isHoliday) {
        this.isHoliday = isHoliday;
    }

    boolean isHoliday() {
        return isHoliday;
    }
}
