package eu.adamgiergun.wontgoshoppingpl.db;

import java.util.TreeSet;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate;

public class EventsTableRow implements Comparable<EventsTableRow> {
    public final int dbId;
    public final int typeId;
    public final String item;
    public final String calendarId;
    public final long calendarEventId;
    final int dateHash;

    EventsTableRow(int dbId, String item, int typeId) {
        this(dbId, item, typeId, null, -1);
    }

    EventsTableRow(int dbId, String item, int typeId, String calendarId, long calendarEventId) {
        this.dbId = dbId;
        this.typeId = typeId;
        this.item = item;
        this.calendarId = calendarId;
        this.calendarEventId = calendarEventId;
        dateHash = newDateHash();
    }

    private int newDateHash() {
        String[] pieces = item.split(",");
        return SimpleDate.newInstance(pieces[0], SimpleDate.ORDER_YDM).hashCode();
    }

    @Override
    public int compareTo(EventsTableRow otherRow) {
        return dateHash - otherRow.dateHash;
    }

    static class Set extends TreeSet<EventsTableRow> {
    }
}
