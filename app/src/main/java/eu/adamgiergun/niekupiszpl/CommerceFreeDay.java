package eu.adamgiergun.niekupiszpl;

import android.content.Context;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.util.Calendar;

class CommerceFreeDay implements Comparable<CommerceFreeDay>{
    private static int DATE_STRING_END = 10;

    private String name;
    private Calendar startTime;

    CommerceFreeDay(String item) {
        setDay(item);
        setTime(item);
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

    public String getName() {
        return name;
    }

    boolean addToGoogleCalendar() {
        return true;
    }

    private void setDay(String item) {
        startTime = Calendar.getInstance();

        String date = item.substring(0, DATE_STRING_END);

        int YEAR_STRING_END = 4;
        int year = Integer.valueOf(date.substring(0, YEAR_STRING_END));
        startTime.set(Calendar.YEAR, year);

        int DAY_STRING_END = 7;
        int day = Integer.valueOf(date.substring(YEAR_STRING_END + 1, DAY_STRING_END));
        startTime.set(Calendar.DAY_OF_MONTH, day);

        int month = Integer.valueOf(date.substring(DAY_STRING_END + 1, DATE_STRING_END)) - 1;
        startTime.set(Calendar.MONTH, month);
    }

    private void setTime(String item) {
        startTime.set(Calendar.MINUTE, 0);
        int hour = getHourAndSetName(item);
        startTime.set(Calendar.HOUR_OF_DAY, hour);
    }

    private int getHourAndSetName(String item) {
        String data = item.substring(DATE_STRING_END + 1);
        int hour = 0;
        try {
            hour = Integer.parseInt(data.substring(0,2));
            name = data.substring(3, data.length());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        int cutPoint = data.indexOf(",") + 1;
        name = data.substring(cutPoint);
        return hour;
    }
}
