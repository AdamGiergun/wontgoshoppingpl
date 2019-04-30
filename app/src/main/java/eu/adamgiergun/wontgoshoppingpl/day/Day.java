package eu.adamgiergun.wontgoshoppingpl.day;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        int month = Integer.valueOf(subPieces[2]) - 1;
        int day = Integer.valueOf(subPieces[1]);
        calendar.set(year, month, day);
    }

    private void setTime(String string) {
        time = new SimpleTime(string);
        allDay = string.equals("00:00");
    }

    @Override
    public int compareTo(@NonNull Day another) {
        return this.calendar.compareTo(another.calendar);
    }

    int compareDateTo(Calendar comparedCalendar) {
        return this.calendar.compareTo(comparedCalendar);
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
        return (type == Type.HOLIDAY) ? description : getDateAndWeekdayString();
    }

    public String getDateAndWeekdayString() {
        String nameOfWeekday = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        return getDateString() + "\n" + nameOfWeekday;
    }

    private String getDateString() {
        return DateFormat.getDateInstance().format(calendar.getTime());
    }

    public String getTimeString() {
        return (time.getHour() == 0 && time.getMinute() == 0)
                ? LocalizedConst.getWholeDay()
                : LocalizedConst.getSince() + time.toString();
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

    public boolean addToGoogleCalendar(Context appContext, String timeZoneId) {
        String defaultCalendarId = getDefaultCalendarIdFromPreferences(appContext);
        if (!defaultCalendarId.equals(calendarId)) {
            calendarId = defaultCalendarId;
        }

        Calendar endCalendar = (Calendar) calendar.clone();
        if (noOfConsecutiveDays > 0) {
            endCalendar.add(Calendar.DAY_OF_YEAR, noOfConsecutiveDays);
        }

        ArrayList<SimpleReminder> reminders = new ArrayList<>();
        reminders.add(reminder);

        SimpleCalendarEvent calendarEvent =
                new SimpleCalendarEvent.Builder(calendarId, calendar, time)
                        .setCalendarEventId(calendarEventId)
                        .setTimeZoneId(timeZoneId)
                        .setEndCalendar(endCalendar, new SimpleTime("23:59"))
                        .setAllDay(allDay).setReminders(reminders)
                        .setTitle((type == Type.COMMERCE_SUNDAY)
                                ? description + " - " + appContext.getString(R.string.go_shopping)
                                : description + " - " + appContext.getString(R.string.go_shopping_earlier))
                .build();


        if (calendarEvent.exists(appContext, calendarId)) {
            calendarEvent.writeReminders(appContext);
        } else {
            if (calendarEvent.exists(appContext)) {
                deleteFromGoogleCalendar(calendarEvent, appContext);
            }
            calendarEventId = calendarEvent.write(appContext);
            updateEventIdInDb(appContext);
        }

        return (calendarEventId > SimpleCalendarEvent.NON_EXISTING);
    }

    private void deleteFromGoogleCalendar(SimpleCalendarEvent calendarEvent, Context appContext) {
        if (calendarEventId != SimpleCalendarEvent.NON_EXISTING && calendarEvent.delete(appContext)) {
            calendarEventId = SimpleCalendarEvent.NON_EXISTING;
            updateEventIdInDb(appContext);
            Toast.makeText(appContext, appContext.getString(R.string.info_deleted), Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteFromGoogleCalendar(Context appContext) {
        if (calendarEventId != SimpleCalendarEvent.NON_EXISTING) {
            int count = SimpleCalendarEvent.delete(calendarEventId, appContext, calendarId);
            calendarEventId = SimpleCalendarEvent.NON_EXISTING;
            updateEventIdInDb(appContext);
            if (count > 0) {
                Toast.makeText(appContext, appContext.getString(R.string.info_deleted), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getDefaultCalendarIdFromPreferences(Context appContext) {
        AppPreferences appPreferences = AppPreferences.getInstance(appContext);
        return appPreferences.getDefaultCalendarId(appContext);
    }

    private void updateEventIdInDb(Context appContext) {
        DatabaseHandler databaseHandler = new DatabaseHandler(appContext);
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

        db.close();
        databaseHandler.close();
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

    public int getBackgroundColorResource() {
        int colorRes;
        switch (type) {
            case HOLIDAY:
                colorRes = R.color.colorHoliday;
                break;
            case NON_COMMERCE_DAY:
                colorRes = R.color.colorNonCommerceDay;
                break;
            case COMMERCE_SUNDAY:
                colorRes = R.color.colorCommerceSunday;
                break;
            default:
                colorRes = 0;
                break;
        }
        return colorRes;
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

            public boolean equals(@Nullable List anotherList) {
                if (anotherList == null) {
                    return false;
                } else {
                    if (this.size() != anotherList.size()) {
                        return false;
                    } else {
                        boolean[] thisArray = this.getArray();
                        boolean[] anotherArray = anotherList.getArray();
                        boolean result = true;
                        for (int i = 0; i < this.size(); i++)
                        {
                            result = result && (thisArray[i] == anotherArray[i]);
                        }
                        return result;
                    }
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
