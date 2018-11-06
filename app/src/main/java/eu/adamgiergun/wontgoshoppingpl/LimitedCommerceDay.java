package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;

import androidx.annotation.NonNull;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleCalendarEvent;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

class LimitedCommerceDay implements Comparable<LimitedCommerceDay>{

    private int dbId;
    private String item;
    private String name;
    private Calendar calendar;
    private int noOfConsecutiveDays;
    private SimpleTime time;
    private SimpleReminder reminder;
    private String description;
    private boolean reminderNeeded;
    private LimitedCommerceDayType type;
    private String calendarId;
    private long calendarEventId;

    LimitedCommerceDay(int id, String item) {
        this.dbId = id;
        this.item = item;
        String[] pieces = item.split(",");
        time = new SimpleTime(pieces[1]);
        setDay(pieces[0]);
        name = pieces[2];
        description = name;
        reminderNeeded = true;
        noOfConsecutiveDays = 0;
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

        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    @Override
    public int compareTo(@NonNull LimitedCommerceDay another) {
        return this.calendar.compareTo(another.calendar);
    }

    int compareTo(Calendar calendar) {
        Calendar dayCalendar = getCalendar();
        return dayCalendar.compareTo(calendar);
    }

    Calendar getCalendar() {
        return calendar;
    }

    void addToDescription(String string) {
        description += ", " + string;
    }

    String getDescription() {
        return description;
    }

    String getDescriptionForDialog() {
        if (type == LimitedCommerceDayType.HOLIDAY) {
            return description;
        } else {
            return getDateString();
        }
    }

    String getDateString() {
        String date = DateFormat.getDateInstance().format(calendar.getTime());
        String nameOfWeekday = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        return date + "\n" + nameOfWeekday;
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

    Calendar getTomorrow() {
        Calendar tomorrow = (Calendar) this.calendar.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        return tomorrow;
    }

    void incrementNoOfConsecutiveDays() {
        noOfConsecutiveDays++;
    }

    boolean isDay(Calendar day) {
        return day.compareTo(this.calendar) == 0;
    }

    void setReminder(SimpleReminder reminder) {
        this.reminder = reminder;
    }

    boolean addToGoogleCalendar(Context context) {
        if (calendarEventId == SimpleCalendarEvent.NON_EXISTING) {
            calendarId = getDefaultCalendarIdFromPreferences(context);
            SimpleCalendarEvent calendarEvent = new SimpleCalendarEvent(calendarId);
            if (type == LimitedCommerceDayType.COMMERCE_SUNDAY) {
                calendarEvent.setTitle(description + " - " + context.getString(R.string.go_shopping));
            } else {
                calendarEvent.setTitle(description + " - " + context.getString(R.string.go_shopping_earlier));
            }
            calendarEvent.setStartCalendar(calendar, time);
            setEndCalendar(calendarEvent);
            calendarEvent.addReminder(reminder);
            calendarEventId = calendarEvent.write(context);
        }
        return (calendarEventId > 0L);
    }

    private void setEndCalendar(SimpleCalendarEvent calendarEvent) {
        Calendar endCalendar = (Calendar) calendar.clone();
        if (noOfConsecutiveDays > 0)
            endCalendar.add(Calendar.DAY_OF_YEAR, noOfConsecutiveDays);
        calendarEvent.setEndCalendar(endCalendar, new SimpleTime("24:00"));
    }

    private String getDefaultCalendarIdFromPreferences(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        return appPreferences.getDefaultCalendarId(context);
    }

    boolean isReminderNeeded() {
        return reminderNeeded;
    }

    void setReminderNotNeeded() {
        this.reminderNeeded = false;
    }

    LimitedCommerceDayType getType() {
        return type;
    }

    void setType(LimitedCommerceDayType type) {
        this.type = type;
    }

    String getCalendarId() {
        return calendarId;
    }

    void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    long getCalendarEventId() {
        return calendarEventId;
    }

    void setCalendarEventId(long calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public String getItem() {
        return item;
    }
}
