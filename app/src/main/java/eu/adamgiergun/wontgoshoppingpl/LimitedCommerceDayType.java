package eu.adamgiergun.wontgoshoppingpl;

import java.util.ArrayList;

enum LimitedCommerceDayType {
    NON_COMMERCE_DAY,
    HOLIDAY,
    COMMERCE_SUNDAY;

    static class TypesList extends ArrayList<LimitedCommerceDayType> {
        TypesList() {
            super();
        }
    }

    static TypesList getTypes(boolean addNonCommerceDays, boolean addHolidays, boolean addCommerceSundays) {
        TypesList types = new TypesList();
        if (addNonCommerceDays)
            types.add(NON_COMMERCE_DAY);
        if (addHolidays)
            types.add(HOLIDAY);
        if (addCommerceSundays)
            types.add(COMMERCE_SUNDAY);
        return types;
    }
}
