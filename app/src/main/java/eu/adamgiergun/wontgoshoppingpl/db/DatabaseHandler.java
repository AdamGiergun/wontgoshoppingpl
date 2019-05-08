package eu.adamgiergun.wontgoshoppingpl.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.*;

import java.util.Iterator;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleCalendarEvent;
import eu.adamgiergun.mylibrary.calendarmanagement.SimpleDate;
import eu.adamgiergun.wontgoshoppingpl.R;
import eu.adamgiergun.wontgoshoppingpl.day.Day;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "events.db";
    private Resources resources;

    public DatabaseHandler(Context appContext) {
        super(appContext, DATABASE_NAME, null, DATABASE_VERSION);
        resources = appContext.getResources();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBContract.SQL_CREATE_EVENTS);
        db.execSQL(DBContract.SQL_CREATE_REMINDERS);
        fillDbWithEvents(db, resources);
    }

    private void fillDbWithEvents(SQLiteDatabase db, Resources resources) {
        Iterator<EventsTableRow> iterator = newEventsIterator(resources);
        while (iterator.hasNext()) {
            insertRowInDb(iterator.next(), db);
        }
    }

    private static Iterator<EventsTableRow> newEventsIterator(Resources resources) {
        EventsTableRow.Set rows = new EventsTableRow.Set();
        addArray(rows, resources, R.array.non_commerce_days);
        addArray(rows, resources, R.array.commerce_sundays);
        addArray(rows, resources, R.array.holidays);
        //Collections.sort(rows);
        return rows.iterator();
    }

    private static void addArray(EventsTableRow.Set rows, Resources resources, int idOfArray) {
        String[] array = resources.getStringArray(idOfArray);
        int yesterdayHash = getYesterdayHash();
        int typeCode = Day.Type.getTypeCode(idOfArray);
        for (String item : array) {
            EventsTableRow row = new EventsTableRow(0, item, typeCode);
            if (row.dateHash > yesterdayHash) {
                rows.add(row);
            }
        }
    }

    private static int getYesterdayHash() {
        SimpleDate yesterday = SimpleDate.getYesterday();
        return EventsTableRow.newDateHash(yesterday.getYear(), yesterday.getMonth(), yesterday.getDay());
    }

    private void insertRowInDb(EventsTableRow row, SQLiteDatabase db) {
        db.insertOrThrow(
                DBContract.Events.TABLE_NAME,
                null,
                getContentValues(row));
    }

    private ContentValues getContentValues(EventsTableRow row) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(
                DBContract.Events.COLUMN_NAME_ITEM,
                row.item);
        contentValues.put(
                DBContract.Events.COLUMN_NAME_TYPE_ID,
                row.typeId);
        return contentValues;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //stub for possible future upgrades
    }

    public Iterator<EventsTableRow> newEventsIterator(String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = newCursor(db, selectionArgs);
        EventsTableRow.Set rows = new EventsTableRow.Set();

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

        return rows.iterator();
    }

    private static Cursor newCursor(SQLiteDatabase db, String[] selectionArgs) {
        return db.query(
                DBContract.Events.TABLE_NAME,
                null,
                DBContract.Events.COLUMN_NAME_TYPE_ID + "  IN (?, ?, ?)",
                selectionArgs,
                null,
                null,
                null);
    }

    public void updateEvent(int id, String calendarId, long calendarEventId) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(DBContract.Events.TABLE_NAME,
                getContentValues(calendarId, calendarEventId),
                DBContract.Events._ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    private ContentValues getContentValues(String calendarId, long calendarEventId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.Events.COLUMN_NAME_CALENDAR_ID, calendarId);
        if (calendarEventId > SimpleCalendarEvent.NON_EXISTING) {
            contentValues.put(DBContract.Events.COLUMN_NAME_CALENDAR_EVENT_ID, calendarEventId);
        } else {
            String nullValue = null;
            contentValues.put(DBContract.Events.COLUMN_NAME_CALENDAR_EVENT_ID, nullValue);
        }
        return contentValues;
    }
}
