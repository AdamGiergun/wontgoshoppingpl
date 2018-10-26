package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.content.res.Resources;

import java.util.*;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleTime;

import static eu.adamgiergun.wontgoshoppingpl.LimitedCommerceDay.Type.*;

class ListOfLimitedCommerceDays extends ArrayList<LimitedCommerceDay> {

    private ListOfLimitedCommerceDays() {
        super();
    }

    private Calendar getYesterday() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return yesterday;
    }

    static ListOfLimitedCommerceDays getListOfLimitedCommerceDays(Context context, ArrayList<LimitedCommerceDay.Type> includedDaysTypes) {
        Resources resources = context.getResources();
        ListOfLimitedCommerceDays days = new ListOfLimitedCommerceDays();
        Iterator<LimitedCommerceDay.Type> typeIterator = includedDaysTypes.iterator();
        LimitedCommerceDay.Type type;
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

    boolean addToCalendar(int reminderDaysBefore, SimpleTime reminderTime, Context context) {
        skipReminderForConsecutiveDays();
        boolean result = true;
//        int counter = 0; //for testing only
        for (LimitedCommerceDay limitedCommerceDay : this) {
            if (limitedCommerceDay.isReminderNeeded()) {
                limitedCommerceDay.setReminderDaysBefore(reminderDaysBefore);
                limitedCommerceDay.setReminderTime(reminderTime);
                result = result && limitedCommerceDay.addToGoogleCalendar(context);
//                counter++; //for testing only
            }
//            if(counter == 3)
//                break; //for testing only
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
