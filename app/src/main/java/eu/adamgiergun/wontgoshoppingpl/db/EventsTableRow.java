package eu.adamgiergun.wontgoshoppingpl.db;

public class EventsTableRow {
    public int dbId;
    public int typeId;
    public String item;
    public String calendarId;
    public long calendarEventId;

    EventsTableRow(int dbId, String item, int typeId, String calendarId, long calendarEventId) {
        this.dbId = dbId;
        this.typeId = typeId;
        this.item = item;
        this.calendarId = calendarId;
        this.calendarEventId = calendarEventId;
    }

}
