package eu.adamgiergun.wontgoshoppingpl.day;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import eu.adamgiergun.mylibrary.calendarmanagement.*;
import eu.adamgiergun.wontgoshoppingpl.common.*;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.db.*;

import java.util.*;

public class Day implements Comparable<Day> {

    private final int dbId;
    private final String item;
    private final String name;
    private final SimpleDate date;
    private final String time;
    private final String timeDescription;
    private final boolean allDay;
    private final Type type;
    private final int backgroundColorResource;
    private final int backgroundResource;

    private int noOfConsecutiveDays;
    private SimpleReminder reminder;
    private String description;
    private boolean reminderNeeded;
    private String calendarId;
    private long calendarEventId;

    private Day(int id, String item, Type type){
        this.dbId = id;
        this.item = item;
        this.type = type;
        backgroundResource = newBackgroundResource();
        backgroundColorResource = newBackgroundColorResource();

        String[] pieces = item.split(",");
        date = newDate(pieces[0]);
        name = pieces[2];
        time = pieces[1];
        allDay = time.equals("00:00");
        timeDescription = allDay ? LocalizedConst.getWholeDay() : LocalizedConst.getSince() + time;
    }

    private int newBackgroundResource() {
        switch (type) {
            case HOLIDAY:
                return R.drawable.card_bg_red;
            case NON_COMMERCE_DAY:
                return R.drawable.card_bg_orange;
            case COMMERCE_SUNDAY:
                return R.drawable.card_bg_green;
            default:
                return 0;
        }
    }

    private int newBackgroundColorResource() {
        switch (type) {
            case HOLIDAY:
                return R.color.colorHoliday;
            case NON_COMMERCE_DAY:
                return R.color.colorNonCommerceDay;
            case COMMERCE_SUNDAY:
                return R.color.colorCommerceSunday;
            default:
                return 0;
        }
    }

    private SimpleDate newDate(String date) {
        String[] subPieces = date.split("_");
        int year = Integer.valueOf(subPieces[0]);
        int month = Integer.valueOf(subPieces[2]);
        int day = Integer.valueOf(subPieces[1]);
        return SimpleDate.newInstance(year, month, day);
    }

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
        return date.addDays(noOfConsecutiveDays);
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
        Day day = new Day(id, item, type);
        day.description = day.name;
        day.reminderNeeded = true;
        day.noOfConsecutiveDays = 0;
        return day;
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

    private boolean isTomorrowOf(Day otherDay) {
        return date.equals(otherDay.date.addDays(1));
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

        public static int getTypeCode(int idOfArray) {
            switch (idOfArray) {
                case R.array.holidays:
                    return Type.HOLIDAY.code;
                case R.array.non_commerce_days:
                    return Type.NON_COMMERCE_DAY.code;
                case R.array.commerce_sundays:
                    return Type.COMMERCE_SUNDAY.code;
                default:
                    return -1;
            }
        }

        private String getCodeAsString() {
            return String.valueOf(code);
        }

        public static class Set extends TreeSet<Type> {}

        public static Set newSet(boolean addNonCommerceDays, boolean addHolidays, boolean addCommerceSundays) {
            Set types = new Set();
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

        public static List newInstance(Context appContext) {
            AppPreferences appPreferences = AppPreferences.getInstance(appContext);
            Type.Set dayTypes = appPreferences.newDayTypesSet(appContext);
            return List.newInstance(appContext, dayTypes);
        }

        private static List newInstance(Context appContext, @NonNull Type.Set includedDaysTypes) {
            List list = new List();
            Iterator<EventsTableRow> eventsIterator = newEventsIterator(appContext, includedDaysTypes);
            SimpleDate yesterday = SimpleDate.getYesterday();

            while (eventsIterator.hasNext()) {
                Day day = Day.newInstance(eventsIterator.next());
                if (day.compareDateTo(yesterday) > 0) {
                    list.add(day);
                }
            }
            return list;
        }

        private static Iterator<EventsTableRow> newEventsIterator(Context appContext, @NonNull Type.Set includedDayTypes) {
            DatabaseHandler databaseHandler = new DatabaseHandler(appContext);
            Iterator<EventsTableRow> eventsIterator =
                    databaseHandler.newEventsIterator(
                            getSelectionArgs(includedDayTypes));
            databaseHandler.close();
            return eventsIterator;
        }

        private static String[] getSelectionArgs(@NonNull Type.Set includedDayTypes) {
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
                            Type.newSet(true, true, true));
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
                Type.Set dayTypes,
                SimpleReminder reminder,
                String timeZoneId,
                SimpleDate till) {

            List list =
                    List.newInstance(appContext, dayTypes);
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
