package eu.adamgiergun.niekupiszpl;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Collections;

class ListOfCommerceFreeDays extends ArrayList<CommerceFreeDay> {

    private ListOfCommerceFreeDays(Resources resources, int idOfArray) {
        super();
        String[] array = resources.getStringArray(idOfArray);
        for (String item : array) {
            this.add(new CommerceFreeDay(resources, item));
        }
    }

    private void addArray(Resources resources, int idOfArray) {
        ListOfCommerceFreeDays addon = new ListOfCommerceFreeDays(resources, idOfArray);
        this.addAll(addon);
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
}
