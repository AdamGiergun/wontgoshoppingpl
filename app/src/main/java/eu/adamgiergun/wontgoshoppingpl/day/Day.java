package eu.adamgiergun.wontgoshoppingpl.day;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.wontgoshoppingpl.common.*;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.db.*;

import java.text.DateFormat;
import java.util.*;

public class Day implements Comparable<Day> {

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
    private Type type;
    private String calendarId;
    private long calendarEventId;

    private Day(){}

    static Day newInstance(EventsTableRow row) {
        Day day = Day.newInstance(row.dbId, row.item);
        day.setType(Type.getType(row.typeId));
        day.setCalendarId(row.calendarId);
        day.setCalendarEventId(row.calendarEventId);
        return day;
    }

    static Day newInstance(int id, String item) {
        Day day = new Day();
        day.dbId = id;
        day.item = item;

        String[] pieces = item.split(",");
        day.setTime(pieces[1]);
        day.setDate(pieces[0]);
        day.name = pieces[2];

        day.description = day.name;
        day.reminderNeeded = true;
        day.noOfConsecutiveDays = 0;

        return day;
    }

    private void setDate(String date) {
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
    public int compareTo(@NonNull Day another) {
        return this.calendar.compareTo(another.calendar);
    }

    int compareDateTo(Calendar calendar) {
        Calendar dayCalendar = getCalendar();
        return dayCalendar.compareTo(calendar);
    }

    Calendar getCalendar() {
        return calendar;
    }

    void appendToDescription(String string) {
        description += ", " + string;
    }

    String getDescription() {
        return description;
    }

    public String getDescriptionForDialog() {
        if (type == Type.HOLIDAY) {
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

    boolean dateIs(Calendar calendar) {
        return calendar.compareTo(this.calendar) == 0;
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

        if (type == Type.COMMERCE_SUNDAY) {
            calendarEvent.setTitle(description + " - " + context.getString(R.string.go_shopping));
        } else {
            calendarEvent.setTitle(description + " - " + context.getString(R.string.go_shopping_earlier));
        }

        calendarEvent.setStartCalendar(calendar, time);

        setEventEndCalendarOrAllDay(calendarEvent);

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

    private void setEventEndCalendarOrAllDay(SimpleCalendarEvent calendarEvent) {
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

    public Type getType() {
        return type;
    }

    public String getTypeCode() {
        return type.getCodeAsString();
    }

    void setType(Type type) {
        this.type = type;
    }

    private void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    private void setCalendarEventId(long calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public String getItem() {
        return item;
    }

    public int getBackgroundResource() {
        int backgroundResource;
        switch (type) {
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
        switch (type) {
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

    private int getColor(Resources resources, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return resources.getColor(id, null);
        } else {
            //noinspection deprecation
            return resources.getColor(id);
        }
    }

    public CharSequence getWidgetText() {
        return getDateString() + "\nPL: " + description;
    }

    public enum Type {
        NON_COMMERCE_DAY(0),
        HOLIDAY(1),
        COMMERCE_SUNDAY(2);

        private int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Type getType(int code) {
            switch (code) {
                case 0:
                    return NON_COMMERCE_DAY;
                case 1:
                    return HOLIDAY;
                case 2:
                    return COMMERCE_SUNDAY;
                default:
                    return null;
            }
        }

        String getCodeAsString() {
            return String.valueOf(code);
        }

        public static class List extends ArrayList<Type> {

            public boolean equals(List anotherList) {
                if (this.size() != anotherList.size()) {
                    return false;
                } else {
                    boolean[] thisArray = this.getArray();
                    boolean[] anotherArray = anotherList.getArray();
                    return (thisArray[0] == anotherArray[0]) &&
                            (thisArray[1] == anotherArray[1]) &&
                            (thisArray[2] == anotherArray[2]);
                }
            }

            private boolean[] getArray() {
                boolean[] array = new boolean[]{false, false, false};
                Iterator<Type> iterator = this.iterator();
                Type type;

                while (iterator.hasNext()) {
                    type = iterator.next();
                    array[type.code] = true;
                }
                return array;
            }
        }

        public static List newList(boolean addNonCommerceDays, boolean addHolidays, boolean addCommerceSundays) {
            List types = new List();
            if (addNonCommerceDays)
                types.add(NON_COMMERCE_DAY);
            if (addHolidays)
                types.add(HOLIDAY);
            if (addCommerceSundays)
                types.add(COMMERCE_SUNDAY);
            return types;
        }
    }
}
