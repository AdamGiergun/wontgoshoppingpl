package eu.adamgiergun.wontgoshoppingpl.day;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;

import androidx.annotation.NonNull;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.db.*;

import static eu.adamgiergun.wontgoshoppingpl.day.Day.Type.*;

public class ListOfDays extends ArrayList<Day> {

    public static Iterator<Day> getIteratorFromResources(Resources resources) {
        ListOfDays days = getFromResources(resources);
        removePastDays(days);
        return days.iterator();
    }

    private static ListOfDays getFromResources(Resources resources) {
        ListOfDays days = new ListOfDays();
        days.addArray(resources, R.array.non_commerce_days);
        days.addArray(resources, R.array.commerce_sundays);
        days.addArray(resources, R.array.holidays);
        Collections.sort(days);
        return days;
    }

    private static void removePastDays(ListOfDays days) {
        Iterator<Day> iterator = days.iterator();
        Calendar yesterday = getYesterday();
        Day day;
        while (iterator.hasNext()) {
            day = iterator.next();
            if (yesterday.compareTo(day.getCalendar()) >= 0) {
                days.remove(day);
            } else {
                break;
            }
        }
    }

    public static Long getLastDateInMillis(Context appContext) {
        Iterator<Day> iterator = newInstance(appContext).iterator();
        Day lastDay = null;

        while (iterator.hasNext()) {
            lastDay = iterator.next();
        }

        return (lastDay == null) ? Calendar.getInstance().getTimeInMillis() : lastDay.getCalendar().getTimeInMillis();
    }

    private static Calendar getYesterday() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return yesterday;
    }

    public static ListOfDays newInstance(Context appContext) {
        AppPreferences appPreferences = AppPreferences.getInstance(appContext);
        List dayTypes = appPreferences.newDayTypesList(appContext);
        return ListOfDays.newInstance(appContext, dayTypes);
    }

    private static ListOfDays newInstance(Context appContext, @NonNull List includedDaysTypes) {
        ListOfDays list = new ListOfDays();

        Day day;
        Iterator<EventsTableRow> rowIterator =
                EventsTableRow.newIterator(
                        appContext,
                        getSelectionArgs(includedDaysTypes));
        Calendar yesterday = getYesterday();
        while (rowIterator.hasNext()) {
            day = Day.newInstance(rowIterator.next());
            if (day.compareDateTo(yesterday) > 0) {
                list.add(day);
            }
        }

        return list;
    }

    private static String[] getSelectionArgs(@NonNull List includedDayTypes) {
        String[] selectionArgs = new String[includedDayTypes.size()];
        Iterator<Day.Type> iterator = includedDayTypes.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            selectionArgs[i++] = iterator.next().getCodeAsString();
        }
        return selectionArgs;
    }

    public static Day getFirst(Context appContext) {
        ListOfDays days =
                newInstance(
                        appContext,
                        Day.Type.newList(true, true, true));
        return days.get(0);
    }

    private void addArray(Resources resources, int idOfArray) {
        String[] array = resources.getStringArray(idOfArray);
        Calendar yesterday = getYesterday();
        Day day;
        for (String item : array) {
            day = Day.newInstance(0, item);
            if (day.compareDateTo(yesterday) > 0) {
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

    public static boolean addToCalendar(
            Context appContext,
            List dayTypesList,
            SimpleReminder reminder,
            String timeZoneId,
            Calendar till) {

        ListOfDays list =
                ListOfDays.newInstance(appContext, dayTypesList);
        list.skipReminderForConsecutiveDays();
        boolean result = true;
        for (Day day : list) {
            if (day.compareDateTo(till) <= 0) {
                if (day.isReminderNeeded()) {
                    day.setReminder(reminder);
                    result = result && day.addToGoogleCalendar(appContext, timeZoneId);
                }
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
                while ((nextDay.getType() != COMMERCE_SUNDAY)
                        && nextDay.dateIs(previousDay.getTomorrow())) {
                    nextDay.setReminderNotNeeded();
                    day.incrementNoOfConsecutiveDays();
                    day.appendToDescription(nextDay.getDescription());
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
