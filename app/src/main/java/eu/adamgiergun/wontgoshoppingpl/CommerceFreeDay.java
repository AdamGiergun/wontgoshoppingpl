package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.util.Calendar;

class CommerceFreeDay implements Comparable<CommerceFreeDay>{
    private static int DATE_STRING_END = 10;

    private String name;
    private Calendar startTime;
    private int reminderDaysBefore;
    private String reminderTime;

    CommerceFreeDay(String item) {
        String[] pieces = item.split(",");
        setDay(pieces[0]);
        setTimeAndName(pieces[1], pieces[2]);
    }

    @Override
    public int compareTo(@NonNull CommerceFreeDay another) {
        return this.startTime.compareTo(another.startTime);
    }

    Calendar getStartTime() {
        return startTime;
    }

    String getDayString() {
        return DateFormat.getDateInstance().format(startTime.getTime());
    }

    String getTimeString(Context context) {
        int hour = startTime.get(Calendar.HOUR_OF_DAY);
        if (hour == 0)
            return context.getString(R.string.whole_day);
        else
            return context.getString(R.string.since) + hour + ":00";
    }

    String getName() {
        return name;
    }

    void setReminderDaysBefore (int daysBefore) {
        reminderDaysBefore = daysBefore;
    }

    void setReminderTime (String time) {
        reminderTime = time;
    }

    boolean addToGoogleCalendar() {
        return true;
    }

    private void setDay(String date) {
        startTime = Calendar.getInstance();
        String[] pieces = date.split("_");

        int year = Integer.valueOf(pieces[0]);
        startTime.set(Calendar.YEAR, year);

        int day = Integer.valueOf(pieces[1]);
        startTime.set(Calendar.DAY_OF_MONTH, day);

        int month = Integer.valueOf(pieces[2]) - 1;
        startTime.set(Calendar.MONTH, month);
    }

    private void setTimeAndName(String time, String name) {
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.HOUR_OF_DAY, getHour(time));

        this.name = name;
    }

    private int getHour(String time) {
        int hour;
        try {
            hour = Integer.parseInt(time);
        } catch (NumberFormatException e) {
            hour = 0;
        }
        return hour;
    }
}
