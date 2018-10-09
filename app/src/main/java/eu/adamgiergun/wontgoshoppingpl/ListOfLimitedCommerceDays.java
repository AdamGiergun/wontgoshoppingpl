package eu.adamgiergun.wontgoshoppingpl;

import android.content.Context;
import android.content.res.Resources;

import java.util.*;

class ListOfLimitedCommerceDays extends ArrayList<LimitedCommerceDay> {

    private ListOfLimitedCommerceDays(Resources resources, int idOfArray) {
        super();
        String[] array = resources.getStringArray(idOfArray);
        Calendar yesterday = getYesterday();
        for (String item : array) {
            LimitedCommerceDay day = new LimitedCommerceDay(item);
            if (day.getStartTime().compareTo(yesterday) > 0)
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
}
