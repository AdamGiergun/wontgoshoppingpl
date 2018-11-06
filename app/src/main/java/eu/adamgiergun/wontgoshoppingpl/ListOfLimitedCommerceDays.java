package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;

import static eu.adamgiergun.wontgoshoppingpl.LimitedCommerceDayType.*;

class ListOfLimitedCommerceDays extends ArrayList<LimitedCommerceDay> {

    private ListOfLimitedCommerceDays() {
        super();
    }

    static Iterator<LimitedCommerceDay> getIteratorFromResources(Context context) {
        Resources resources = context.getResources();
        ListOfLimitedCommerceDays days = new ListOfLimitedCommerceDays();
        days.addArray(resources, R.array.non_commerce_days);
        days.addArray(resources, R.array.commerce_sundays);
        days.addArray(resources, R.array.holidays);
        Collections.sort(days);
        Iterator<LimitedCommerceDay> iterator = days.iterator();
        Calendar yesterday = getYesterday();
        LimitedCommerceDay day;
        while (iterator.hasNext()) {
            day = iterator.next();
            if (yesterday.compareTo(day.getCalendar()) >= 0) {
                days.remove(day);
            } else {
                break;
            }
        }
        return days.iterator();
    }

    private static Calendar getYesterday() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return yesterday;
    }

    static ListOfLimitedCommerceDays getList(Context context, TypesList includedDaysTypes) {
        //DatabaseHandler databaseHandler = new DatabaseHandler(context);
        //SQLiteDatabase db = databaseHandler.getReadableDatabase();

        Resources resources = context.getResources();
        ListOfLimitedCommerceDays days = new ListOfLimitedCommerceDays();
        Iterator<LimitedCommerceDayType> typeIterator = includedDaysTypes.iterator();
        LimitedCommerceDayType type;
        while (typeIterator.hasNext()) {
            type = typeIterator.next();
            switch (type) {
                case NON_COMMERCE_DAY:
                    days.addArray(resources, R.array.non_commerce_days);
                    break;
                case COMMERCE_SUNDAY:
                    days.addArray(resources, R.array.commerce_sundays);
                    break;
                case HOLIDAY:
                    days.addArray(resources, R.array.holidays);
                    break;
                default:
                    break;
            }
        }
        Collections.sort(days);
        return days;
    }

    private void addArray(Resources resources, int idOfArray) {
        String[] array = resources.getStringArray(idOfArray);
        Calendar yesterday = getYesterday();
        LimitedCommerceDay day;
        for (String item : array) {
            day = new LimitedCommerceDay(item);
            if (day.compareTo(yesterday) > 0) {
                switch (idOfArray) {
                    case R.array.holidays:
                        day.setType(HOLIDAY);
                        break;
                    case R.array.non_commerce_days:
                        day.setType(NON_COMMERCE_DAY);
                        break;
                    case R.array.commerce_sundays:
                        day.setType(COMMERCE_SUNDAY);
                        break;
                }
                this.add(day);
            }
        }
    }

    boolean addToCalendar(Context context, SimpleReminder reminder, Calendar till) {
        skipReminderForConsecutiveDays();
        boolean result = true;
        for (LimitedCommerceDay day : this) {
            if (day.compareTo(till) <= 0) {
                if (day.isReminderNeeded()) {
                    day.setReminder(reminder);
                    result = result && day.addToGoogleCalendar(context);
                }
            }
        }
        return result;
    }

    private void skipReminderForConsecutiveDays() {
        ListIterator<LimitedCommerceDay> iterator = listIterator();
        LimitedCommerceDay day, nextDay, previousDay;
        if (iterator.hasNext()) {
            day = iterator.next();
            nextDay = day;
            while (iterator.hasNext()) {
                day = nextDay;
                previousDay = day;
                nextDay = iterator.next();
                while ((nextDay.getType() != COMMERCE_SUNDAY)
                        && nextDay.isDay(previousDay.getTomorrow())) {
                    nextDay.setReminderNotNeeded();
                    day.incrementNoOfConsecutiveDays();
                    day.addToDescription(nextDay.getDescription());
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
