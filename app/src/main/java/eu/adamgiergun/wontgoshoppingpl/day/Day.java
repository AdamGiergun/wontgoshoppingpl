package eu.adamgiergun.wontgoshoppingpl.day;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.wontgoshoppingpl.common.*;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.db.*;

import java.util.*;

public class Day implements Comparable<Day> {

    private int dbId;
    private String item;
    private String name;
    private SimpleDate date;
    private int noOfConsecutiveDays;
    private String time;
    private String timeDescription;
    private boolean allDay;
    private SimpleReminder reminder;
    private String description;
    private boolean reminderNeeded;
    private Type type;
    private String calendarId;
    private long calendarEventId;
    private int backgroundColorResource;
    private int backgroundResource;

    private Day(){}

    @Override
    public int compareTo(@NonNull Day another) {
        return this.date.compareTo(another.date);
    }

    public boolean addToGoogleCalendar(Context appContext, String timeZoneId) {
        calendarId = getDefaultCalendarIdFromPreferences(appContext);
        SimpleCalendarEvent calendarEvent = newCalendarEventInstance(appContext, timeZoneId);

        if (calendarEvent.exists(appContext, calendarId)) {
            calendarEvent.writeReminders(appContext);
        } else {
            if (calendarEvent.exists(appContext)) {
                deleteFromGoogleCalendar(calendarEvent, appContext);
            }
            calendarEventId = calendarEvent.write(appContext);
            updateEventInDb(appContext);
        }
        return (calendarEventId > SimpleCalendarEvent.NON_EXISTING);
    }

    private String getDefaultCalendarIdFromPreferences(Context appContext) {
        AppPreferences appPreferences = AppPreferences.getInstance(appContext);
        return appPreferences.getDefaultCalendarId(appContext);
    }

    private SimpleCalendarEvent newCalendarEventInstance(Context appContext, String timeZoneId) {
        return new SimpleCalendarEvent.Builder(calendarId, date, new SimpleTime(time))
                .setCalendarEventId(calendarEventId)
                .setTimeZoneId(timeZoneId)
                .setEndCalendar(getEndDate(), new SimpleTime("23:59"))
                .setAllDay(allDay).setReminders(getReminders())
                .setTitle((type == Type.COMMERCE_SUNDAY)
                        ? description + " - " + appContext.getString(R.string.go_shopping)
                        : description + " - " + appContext.getString(R.string.go_shopping_earlier))
                .build();
    }

    private SimpleDate getEndDate() {
        SimpleDate endDate = (SimpleDate) date.clone();
        if (noOfConsecutiveDays > 0) {
            endDate = endDate.addDays(noOfConsecutiveDays);
        }
        return endDate;
    }

    private ArrayList<SimpleReminder> getReminders() {
        ArrayList<SimpleReminder> reminders = new ArrayList<>();
        reminders.add(reminder);
        return reminders;
    }

    private void deleteFromGoogleCalendar(SimpleCalendarEvent calendarEvent, Context appContext) {
        if (calendarEventId != SimpleCalendarEvent.NON_EXISTING && calendarEvent.delete(appContext)) {
            finishDeleting(appContext);
        }
    }

    private void finishDeleting(Context appContext) {
        Toast.makeText(appContext, appContext.getString(R.string.info_deleted), Toast.LENGTH_SHORT).show();
        calendarEventId = SimpleCalendarEvent.NON_EXISTING;
        updateEventInDb(appContext);
    }

    private void updateEventInDb(Context appContext) {
        DatabaseHandler databaseHandler = new DatabaseHandler(appContext);
        databaseHandler.updateEvent(dbId, calendarId, calendarEventId);
        databaseHandler.close();
    }

    public void deleteFromGoogleCalendar(Context appContext) {
        if (calendarEventId != SimpleCalendarEvent.NON_EXISTING && SimpleCalendarEvent.delete(calendarEventId, appContext, calendarId)) {
            finishDeleting(appContext);
        }
    }

    public int getBackgroundResource() {
        return backgroundResource;
    }

    public int getBackgroundColorResource() {
        return backgroundColorResource;
    }

    public String getDateAndWeekdayString() {
        return date.toString() + "\n" + date.getWeekdayName();
    }

    public String getDescriptionForDialog() {
        return (type == Type.HOLIDAY) ? description : getDateAndWeekdayString();
    }

    public String getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public String getTypeCode() {
        return type.getCodeAsString();
    }

    public String getTimeDescription() {
        return timeDescription;
    }

    public CharSequence getWidgetText() {
        return date.toString() + "\nPL: " + description;
    }

    public boolean isCommerceSunday() {
        return type == Type.COMMERCE_SUNDAY;
    }

    public void setReminder(SimpleReminder reminder) {
        this.reminder = reminder;
    }

    private static Day newInstance(EventsTableRow row) {
        Day day = Day.newInstance(row.dbId, row.item, Type.getType(row.typeId));
        day.calendarId = row.calendarId;
        day.calendarEventId = row.calendarEventId;
        return day;
    }

    private static Day newInstance(int id, String item, Type type) {
        Day day = new Day();
        day.dbId = id;
        day.item = item;
        day.type = type;
        day.setBackgroundResource();
        day.setBackgroundColorResource();

        String[] pieces = item.split(",");
        day.setDate(pieces[0]);
        day.setTime(pieces[1]);
        day.name = pieces[2];

        day.description = day.name;
        day.reminderNeeded = true;
        day.noOfConsecutiveDays = 0;
        return day;
    }

    private void setBackgroundResource() {
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
    }

    private void setBackgroundColorResource() {
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
        backgroundColorResource = colorRes;
    }

    private void setDate(String date) {
        String[] subPieces = date.split("_");
        int year = Integer.valueOf(subPieces[0]);
        int month = Integer.valueOf(subPieces[2]);
        int day = Integer.valueOf(subPieces[1]);
        this.date = SimpleDate.newInstance(year, month, day);
    }

    private void setTime(String string) {
        time = string;
        allDay = string.equals("00:00");
        timeDescription = allDay ? LocalizedConst.getWholeDay() : LocalizedConst.getSince() + time;
    }

    private void appendToDescription(String string) {
        description += ", " + string;
    }

    private int compareDateTo(SimpleDate comparedDate) {
        return date.compareTo(comparedDate);
    }

    private void incrementNoOfConsecutiveDays() {
        noOfConsecutiveDays++;
    }

    private boolean isTomorrowOf(Day day) {
        return date.compareTo(day.date.addDays(1)) == 0;
    }

    private boolean isReminderNeeded() {
        return reminderNeeded;
    }

    private void setReminderNotNeeded() {
        this.reminderNeeded = false;
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

        private String getCodeAsString() {
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

    public static class List extends ArrayList<Day> {

        public static Iterator<Day> getIterator(Resources resources) {
            List days = newInstance(resources);
            //days.removePastDays();
            return days.iterator();
        }

        private static List newInstance(Resources resources) {
            List days = new List();
            days.addArray(resources, R.array.non_commerce_days);
            days.addArray(resources, R.array.commerce_sundays);
            days.addArray(resources, R.array.holidays);
            Collections.sort(days);
            return days;
        }

        private void addArray(Resources resources, int idOfArray) {
            String[] array = resources.getStringArray(idOfArray);
            SimpleDate yesterday = SimpleDate.getYesterday();
            Day day;
            Type type = getType(idOfArray);
            for (String item : array) {
                day = Day.newInstance(0, item, type);
                if (day.compareDateTo(yesterday) > 0) {
                    this.add(day);
                }
            }
        }

        private Type getType(int idOfArray) {
            switch (idOfArray) {
                case R.array.holidays:
                    return Type.HOLIDAY;
                case R.array.non_commerce_days:
                    return Type.NON_COMMERCE_DAY;
                case R.array.commerce_sundays:
                    return Type.COMMERCE_SUNDAY;
                default:
                    return null;
            }
        }

        public static List newInstance(Context appContext) {
            AppPreferences appPreferences = AppPreferences.getInstance(appContext);
            Type.List dayTypes = appPreferences.newDayTypesList(appContext);
            return List.newInstance(appContext, dayTypes);
        }

        private static List newInstance(Context appContext, @NonNull Type.List includedDaysTypes) {
            List list = new List();
            Day day;
            Iterator<EventsTableRow> eventsIterator = getEventsIterator(appContext, includedDaysTypes);
            SimpleDate yesterday = SimpleDate.getYesterday();

            while (eventsIterator.hasNext()) {
                day = Day.newInstance(eventsIterator.next());
                if (day.compareDateTo(yesterday) > 0) {
                    list.add(day);
                }
            }
            return list;
        }

        private static Iterator<EventsTableRow> getEventsIterator(Context appContext, @NonNull Type.List includedDaysTypes) {
            DatabaseHandler databaseHandler = new DatabaseHandler(appContext);
            Iterator<EventsTableRow> eventsIterator =
                    databaseHandler.newEventsIterator(
                            getSelectionArgs(includedDaysTypes));
            databaseHandler.close();
            return eventsIterator;
        }

        private static String[] getSelectionArgs(@NonNull Type.List includedDayTypes) {
            String[] selectionArgs = new String[includedDayTypes.size()];
            Iterator<Type> iterator = includedDayTypes.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                selectionArgs[i++] = iterator.next().getCodeAsString();
            }
            return selectionArgs;
        }

        public static Day getFirst(Context appContext) {
            List days =
                    newInstance(
                            appContext,
                            Type.newList(true, true, true));
            return days.get(0);
        }

        public static long getLastDateInMillis(Context appContext) {
            Iterator<Day> iterator = newInstance(appContext).iterator();
            Day lastDay = null;
            while (iterator.hasNext()) {
                lastDay = iterator.next();
            }
            return (lastDay == null) ? Calendar.getInstance().getTimeInMillis() : lastDay.date.getTimeInMillis();
        }


        public static boolean addToCalendar(
                Context appContext,
                Type.List dayTypesList,
                SimpleReminder reminder,
                String timeZoneId,
                SimpleDate till) {

            List list =
                    List.newInstance(appContext, dayTypesList);
            list.skipReminderForConsecutiveDays();
            boolean result = true;
            for (Day day : list) {
                if ((day.compareDateTo(till) <= 0) && day.isReminderNeeded()) {
                    day.setReminder(reminder);
                    result = result && day.addToGoogleCalendar(appContext, timeZoneId);
                }
            }
            return result;
        }

        private void skipReminderForConsecutiveDays() {
            ListIterator<Day> iterator = listIterator();
            Day day, nextDay, previousDay;
            if (iterator.hasNext()) {
                day = iterator.next();
                nextDay = day;
                while (iterator.hasNext()) {
                    day = nextDay;
                    previousDay = day;
                    nextDay = iterator.next();
                    while (!(nextDay.isCommerceSunday())
                            && nextDay.isTomorrowOf(previousDay)) {
                        nextDay.setReminderNotNeeded();
                        day.incrementNoOfConsecutiveDays();
                        day.appendToDescription(nextDay.description);
                        if (iterator.hasNext()) {
                            previousDay = nextDay;
                            nextDay = iterator.next();
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

//        private void removePastDays() {
//            Iterator<Day> iterator = this.iterator();
//            Calendar yesterday = getYesterday();
//            Day day;
//            while (iterator.hasNext()) {
//                day = iterator.next();
//                if (yesterday.compareTo(day.getCalendar()) >= 0) {
//                    this.remove(day);
//                } else {
//                    break;
//                }
//            }
//        }
}
