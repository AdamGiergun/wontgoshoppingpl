package eu.adamgiergun.wontgoshoppingpl.db;

import java.util.TreeSet;

public class EventsTableRow implements Comparable<EventsTableRow>{
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
        dateHash = newDateHash(item);
    }

    private static int newDateHash(String item) {
        String[] pieces = item.split(",");
        String[] subPieces = pieces[0].split("_");
        int year = Integer.valueOf(subPieces[0]);
        int month = Integer.valueOf(subPieces[2]);
        int day = Integer.valueOf(subPieces[1]);
        return newDateHash(year, month, day);
    }

    static int newDateHash(int year, int month, int day) {
        return year*10000 + month*100 + day;
    }

    @Override
    public int compareTo(EventsTableRow otherRow) {
        return dateHash - otherRow.dateHash;
    }

    static class Set extends TreeSet<EventsTableRow> {}
}
