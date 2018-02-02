package eu.adamgiergun.niekupiszpl;

import android.content.Context;
import android.content.res.Resources;

import java.util.ArrayList;

class ListOfCommerceFreeDays extends ArrayList<CommerceFreeDay> {

    ListOfCommerceFreeDays(Resources resources, int idOfArray) {
        super();
        String[] array = resources.getStringArray(idOfArray);
        for (String item : array
             ) {
            this.add(new CommerceFreeDay(resources, item));
        }
    }
}
