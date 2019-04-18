package eu.adamgiergun.wontgoshoppingpl.days;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleCalendarEvent;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences;
import eu.adamgiergun.wontgoshoppingpl.common.LocalizedConst;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.db.DBContract;
import eu.adamgiergun.wontgoshoppingpl.db.DatabaseHandler;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class LimitedCommerceDay implements Comparable<LimitedCommerceDay> {

    private int dbId;
    private String item;
    private String name;
    private Calendar calendar;
    private int noOfConsecutiveDays;
    private SimpleTime time;
    private boolean allDay;
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
        setTime(pieces[1]);
        setDay(pieces[0]);
        name = pieces[2];

        description = name;
        reminderNeeded = true;
        noOfConsecutiveDays = 0;
    }

    private void setDay(String date) {
        String[] subPieces = date.split("_");

        calendar = SimpleCalendarEvent.getClearedCalendarInstance();

        int year = Integer.valueOf(subPieces[0]);
        calendar.set(Calendar.YEAR, year);

        int day = Integer.valueOf(subPieces[1]);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        int month = Integer.valueOf(subPieces[2]) - 1;
        calendar.set(Calendar.MONTH, month);
    }

    private void setTime(String string) {
        time = new SimpleTime(string);
        allDay = string.equals("00:00");
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

    public String getDescriptionForDialog() {
        if (type == LimitedCommerceDayType.HOLIDAY) {
            return description;
        } else {
            return getDateAndWeekdayString();
        }
    }

    public String getDateAndWeekdayString() {
        String nameOfWeekday = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        return getDateString() + "\n" + nameOfWeekday;
    }

    private String getDateString() {
        return DateFormat.getDateInstance().format(calendar.getTime());
    }

    public String getTimeString() {
        if (time.getHour() == 0 && time.getMinute() == 0)
            return LocalizedConst.getWholeDay();
        else
            return LocalizedConst.getSince() + time.toString();
    }

    public String getName() {
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

    public void setReminder(SimpleReminder reminder) {
        this.reminder = reminder;
    }

    public boolean addToGoogleCalendar(Context context) {
        String defaultCalendarId = getDefaultCalendarIdFromPreferences(context);
        if (calendarId != null) {
            if (!defaultCalendarId.equals(calendarId)) {
                deleteFromGoogleCalendar(context);
                calendarId = defaultCalendarId;
            }
        } else {
            calendarId = defaultCalendarId;
        }

        SimpleCalendarEvent calendarEvent = new SimpleCalendarEvent(calendarId);

        if (type == LimitedCommerceDayType.COMMERCE_SUNDAY) {
            calendarEvent.setTitle(description + " - " + context.getString(R.string.go_shopping));
        } else {
            calendarEvent.setTitle(description + " - " + context.getString(R.string.go_shopping_earlier));
        }

        calendarEvent.setStartCalendar(calendar, time);

        setEndCalendarOrAllDay(calendarEvent);

        calendarEvent.addReminder(reminder);

        if (SimpleCalendarEvent.doesExist(calendarEventId, context, calendarId)) {
            calendarEvent.setEventID(calendarEventId);
            calendarEvent.writeReminders(context);
        } else {
            calendarEventId = calendarEvent.write(context);
            updateEventIdInDb(context);
        }

        return (calendarEventId > SimpleCalendarEvent.NON_EXISTING);
    }

    public void deleteFromGoogleCalendar(Context context) {
        if (calendarEventId != SimpleCalendarEvent.NON_EXISTING) {
            int count = SimpleCalendarEvent.delete(calendarEventId, context, calendarId);
            calendarEventId = SimpleCalendarEvent.NON_EXISTING;
            updateEventIdInDb(context);
            if (count > 0) {
                Toast.makeText(context, context.getString(R.string.info_deleted), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setEndCalendarOrAllDay(SimpleCalendarEvent calendarEvent) {
        Calendar endCalendar = (Calendar) calendar.clone();
        if (noOfConsecutiveDays > 0) {
            endCalendar.add(Calendar.DAY_OF_YEAR, noOfConsecutiveDays);
        } else {
            calendarEvent.setAllDay(allDay);
        }
        calendarEvent.setEndCalendar(endCalendar, new SimpleTime("23:59"));
    }

    private String getDefaultCalendarIdFromPreferences(Context context) {
        AppPreferences appPreferences = AppPreferences.getInstance(context);
        return appPreferences.getDefaultCalendarId(context);
    }

    private void updateEventIdInDb(Context context) {
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        SQLiteDatabase db = databaseHandler.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.Events.COLUMN_NAME_CALENDAR_ID, calendarId);
        if (calendarEventId > SimpleCalendarEvent.NON_EXISTING) {
            contentValues.put(DBContract.Events.COLUMN_NAME_CALENDAR_EVENT_ID, calendarEventId);
        } else {
            String nullValue = null;
            contentValues.put(DBContract.Events.COLUMN_NAME_CALENDAR_EVENT_ID, nullValue);
        }
        db.update(DBContract.Events.TABLE_NAME,
                contentValues,
                DBContract.Events._ID + "=?",
                new String[]{String.valueOf(dbId)});
    }

    boolean isReminderNeeded() {
        return reminderNeeded;
    }

    void setReminderNotNeeded() {
        this.reminderNeeded = false;
    }

    public LimitedCommerceDayType getType() {
        return type;
    }

    public String getTypeCode() {
        return type.getCodeAsString();
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

    public int getBackgroundResource() {
        int backgroundResource;
        switch (getType()) {
            case HOLIDAY:
                backgroundResource = R.drawable.card_bg_red;
                break;
            case NON_COMMERCE_DAY:
                backgroundResource = R.drawable.card_bg_orange;
                break;
            case COMMERCE_SUNDAY:
                backgroundResource = R.drawable.card_bg_green;
                break;
            default:
                backgroundResource = 0;
                break;
        }
        return backgroundResource;
    }

    public int getBackgroundColor(Context context) {
        int color;
        Resources resources = context.getResources();
        switch (getType()) {
            case HOLIDAY:
                color = getColor(resources, R.color.colorHoliday);
                break;
            case NON_COMMERCE_DAY:
                color = getColor(resources, R.color.colorNonCommerceDay);
                break;
            case COMMERCE_SUNDAY:
                color = getColor(resources, R.color.colorCommerceSunday);
                break;
            default:
                color = 0;
                break;
        }
        return color;
    }

    @SuppressWarnings("deprecation")
    private int getColor(Resources resources, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return resources.getColor(id, null);
        } else {
            return resources.getColor(id);
        }
    }

    public CharSequence getWidgetText() {
        return getDateString() + "\nPL: " + description;
    }
}
