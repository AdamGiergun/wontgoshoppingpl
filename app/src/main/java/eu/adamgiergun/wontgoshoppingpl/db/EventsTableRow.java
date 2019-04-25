package eu.adamgiergun.wontgoshoppingpl.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.*;

public class EventsTableRow {
    public int dbId;
    public int typeId;
    public String item;
    public String calendarId;
    public long calendarEventId;

    private EventsTableRow(int dbId, String item, int typeId, String calendarId, long calendarEventId) {
        this.dbId = dbId;
        this.typeId = typeId;
        this.item = item;
        this.calendarId = calendarId;
        this.calendarEventId = calendarEventId;
    }

    public static Iterator<EventsTableRow> newIterator(Context appContext, String[] selectionArgs) {
        DatabaseHandler databaseHandler = new DatabaseHandler(appContext);
        SQLiteDatabase db = databaseHandler.getReadableDatabase();
        Cursor cursor = DatabaseHandler.newCursor(db, selectionArgs);
        List<EventsTableRow> rows = new ArrayList<>();

        if (cursor!=null && cursor.getCount()>0) {
            cursor.moveToFirst();
            EventsTableRow row;

            while (!cursor.isAfterLast()) {
                row = new EventsTableRow(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        cursor.getLong(4)
                );
                rows.add(row);
                cursor.moveToNext();
            }
        }

        if (cursor != null)
            cursor.close();
        db.close();
        databaseHandler.close();

        return rows.iterator();
    }
}
