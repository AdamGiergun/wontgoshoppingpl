package eu.adamgiergun.wontgoshoppingpl;

import java.util.ArrayList;
import java.util.Iterator;

enum LimitedCommerceDayType {
    NON_COMMERCE_DAY(0),
    HOLIDAY(1),
    COMMERCE_SUNDAY(2);

    private int code;

    LimitedCommerceDayType(int code) {
        this.code = code;
    }

    static LimitedCommerceDayType getType(int code) {
        switch (code) {
            case 0:
                return NON_COMMERCE_DAY;
            case 1:
                return HOLIDAY;
            case 2:
                return COMMERCE_SUNDAY;
            default:
                return null;
        }
    }

    String getCodeAsString() {
        return String.valueOf(code);
    }

    static class TypesList extends ArrayList<LimitedCommerceDayType> {
        TypesList() {
            super();
        }

        boolean equals(TypesList anotherList) {
            if (this.size() != anotherList.size()) {
                return false;
            } else {
                boolean[] thisArray = this.getArray();
                boolean[] anotherArray = anotherList.getArray();
                return (thisArray[0] == anotherArray[0]) &&
                        (thisArray[1] == anotherArray[1]) &&
                        (thisArray[2] == anotherArray[2]);
            }
        }

        private boolean[] getArray() {
            boolean[] array = new boolean[]{false, false, false};
            Iterator<LimitedCommerceDayType> iterator = this.iterator();
            LimitedCommerceDayType type;

            while (iterator.hasNext()) {
                type = iterator.next();
                array[type.code] = true;
            }
            return array;
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
