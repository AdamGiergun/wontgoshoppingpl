package eu.adamgiergun.niekupiszpl;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

class ListOfCommerceFreeDays extends ArrayList<CommerceFreeDay> {

    private ListOfCommerceFreeDays(Resources resources, int idOfArray) {
        super();
        String[] array = resources.getStringArray(idOfArray);
        Calendar yesterday = getYesterday();
        for (String item : array) {
            CommerceFreeDay day = new CommerceFreeDay(item);
            if (day.getStartTime().compareTo(yesterday) > 0)
                this.add(day);
        }
    }

    private Calendar getYesterday() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return yesterday;
    }

    static ListOfCommerceFreeDays getListOfCommerceFreeDays(Context context, boolean includeHolidays) {
        Resources resources = context.getResources();
        ListOfCommerceFreeDays nonCommerceDays = new ListOfCommerceFreeDays(resources, R.array.non_commerce_days);
        if (includeHolidays) {
            nonCommerceDays.addArray(resources, R.array.national_holidays);
        }
        Collections.sort(nonCommerceDays);
        return nonCommerceDays;
    }

    private void addArray(Resources resources, int idOfArray) {
        this.addAll(new ListOfCommerceFreeDays(resources, idOfArray));
    }
}
