package eu.adamgiergun.wontgoshoppingpl.days;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;

import androidx.annotation.NonNull;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleReminder;
import eu.adamgiergun.wontgoshoppingpl.common.AppPreferences;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.db.DBContract;
import eu.adamgiergun.wontgoshoppingpl.db.DatabaseHandler;

import static eu.adamgiergun.wontgoshoppingpl.days.LimitedCommerceDayType.*;

public class ListOfLimitedCommerceDays extends ArrayList<LimitedCommerceDay> {

    public static Iterator<LimitedCommerceDay> getIterator(Context context) {
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

    public static Calendar getLastDate(Context context) {
        Iterator<LimitedCommerceDay> iterator = newInstance(context).iterator();
        LimitedCommerceDay lastDay = null;

        while (iterator.hasNext()) {
            lastDay = iterator.next();
        }

        if (lastDay == null) {
            return Calendar.getInstance();
        } else {
            return lastDay.getCalendar();
        }
    }

    private static Calendar getYesterday() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return yesterday;
    }

    public static ListOfLimitedCommerceDays newInstance(Context context) {
        AppPreferences appPreferences = AppPreferences.getInstance(context);
        LimitedCommerceDayType.TypesList types = appPreferences.getLimitedCommerceDayTypesList(context);
        return ListOfLimitedCommerceDays.newInstance(context, types);
    }

    private static ListOfLimitedCommerceDays newInstance(Context context, @NonNull TypesList includedDaysTypes) {
        DatabaseHandler databaseHandler = new DatabaseHandler(context);
        SQLiteDatabase db = databaseHandler.getReadableDatabase();
        String[] selectionArgs = new String[includedDaysTypes.size()];
        Iterator<LimitedCommerceDayType> iterator = includedDaysTypes.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            selectionArgs[i++] = iterator.next().getCodeAsString();
        }
        ListOfLimitedCommerceDays list = new ListOfLimitedCommerceDays();
        LimitedCommerceDay day;
        Cursor cursor = db.query(DBContract.Events.TABLE_NAME, null, DBContract.Events.COLUMN_NAME_TYPE_ID + "  IN (?, ?, ?)", selectionArgs, null, null, null);
        if (cursor!=null && cursor.getCount()>0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                day = new LimitedCommerceDay(
                        cursor.getInt(0),
                        cursor.getString(1));
                LimitedCommerceDayType type =
                        LimitedCommerceDayType.getType(
                                cursor.getInt(2));
                day.setType(type);

                String calendarId = cursor.getString(3);
                day.setCalendarId(calendarId);

                long eventId = cursor.getLong(4);
                day.setCalendarEventId(eventId);
                if (day.compareTo(getYesterday()) > 0)
                    list.add(day);
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
        }
        return list;
    }

    public static LimitedCommerceDay getFirst(Context context) {
        ListOfLimitedCommerceDays days =
                newInstance(
                        context,
                        LimitedCommerceDayType.getTypesList(true, true, true));
        return days.get(0);
    }

    private void addArray(Resources resources, int idOfArray) {
        String[] array = resources.getStringArray(idOfArray);
        Calendar yesterday = getYesterday();
        LimitedCommerceDay day;
        for (String item : array) {
            day = new LimitedCommerceDay(0, item);
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

    public static boolean addToCalendar(
            Context context,
            LimitedCommerceDayType.TypesList typesList,
            SimpleReminder reminder,
            Calendar till) {

        ListOfLimitedCommerceDays list =
                ListOfLimitedCommerceDays.newInstance(context, typesList);
        list.skipReminderForConsecutiveDays();
        boolean result = true;
        for (LimitedCommerceDay day : list) {
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
