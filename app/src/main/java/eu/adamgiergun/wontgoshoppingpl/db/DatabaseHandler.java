package eu.adamgiergun.wontgoshoppingpl.db;

// TODO do sth with fillDbWithEvents()

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.adamgiergun.mylibrary.calendarmanagement.SimpleCalendarEvent;
import eu.adamgiergun.wontgoshoppingpl.day.*;

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
        Iterator<Day> iterator = Day.List.newIterator(resources);
        while (iterator.hasNext()) {
            insertDayInDb(iterator.next(), db);
        }
    }

    private void insertDayInDb(Day day, SQLiteDatabase db) {
        db.insertOrThrow(
                DBContract.Events.TABLE_NAME,
                null,
                getContentValues(day));
    }

    private ContentValues getContentValues(Day day) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(
                DBContract.Events.COLUMN_NAME_ITEM,
                day.getItem());
        contentValues.put(
                DBContract.Events.COLUMN_NAME_TYPE_ID,
                day.getTypeCode());
        return contentValues;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //stub for possible future upgrades
    }

    public Iterator<EventsTableRow> newEventsIterator(String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = newCursor(db, selectionArgs);
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
