package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.content.res.Resources;

import java.util.*;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;

class ListOfLimitedCommerceDays extends ArrayList<LimitedCommerceDay> {

    private ListOfLimitedCommerceDays(Resources resources, int idOfArray) {
        super();
        String[] array = resources.getStringArray(idOfArray);
        Calendar yesterday = getYesterday();
        for (String item : array) {
            LimitedCommerceDay day = new LimitedCommerceDay(item);
            if (idOfArray == R.array.holidays)
                day.setHoliday(true);
            else
                day.setHoliday(false);
            if (day.compareTo(yesterday) > 0)
                this.add(day);
        }
    }

    private Calendar getYesterday() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return yesterday;
    }

    static ListOfLimitedCommerceDays getListOfLimitedCommerceDays(Context context, boolean includeHolidays, boolean includeOtherLimitedCommerceDays) {
        Resources resources = context.getResources();
        ListOfLimitedCommerceDays nonCommerceDays;
        if (includeOtherLimitedCommerceDays) {
            nonCommerceDays = new ListOfLimitedCommerceDays(resources, R.array.non_commerce_days);
            if (includeHolidays) {
                nonCommerceDays.addHolidaysArray(resources);
            }
        } else {
            nonCommerceDays = new ListOfLimitedCommerceDays(resources, R.array.holidays);
        }
        Collections.sort(nonCommerceDays);
        return nonCommerceDays;
    }

    private void addHolidaysArray(Resources resources) {
        this.addAll(new ListOfLimitedCommerceDays(resources, R.array.holidays));
    }

    boolean addToCalendar(int reminderDaysBefore, SimpleTime reminderTime, Context context) {
        skipReminderForConsecutiveDays();
        boolean result = true;
        for (LimitedCommerceDay limitedCommerceDay : this) {
            if (limitedCommerceDay.isReminderNeeded()) {
                limitedCommerceDay.setReminderDaysBefore(reminderDaysBefore);
                limitedCommerceDay.setReminderTime(reminderTime);
                result = result && limitedCommerceDay.addToGoogleCalendar(context);
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
                while (nextDay.isDay(previousDay.getTomorrow())) {
                    nextDay.setReminderNeeded(false);
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
